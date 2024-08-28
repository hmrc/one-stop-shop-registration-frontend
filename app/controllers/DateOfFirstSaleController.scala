/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import controllers.actions._
import formats.Format.{dateFormatter, dateHintFormatter}
import forms.DateOfFirstSaleFormProvider
import models.requests.AuthenticatedDataRequest
import models.{AmendMode, Mode}
import pages.DateOfFirstSalePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import services.{DateService, RegistrationService, RejoinRegistrationService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateOfFirstSaleUtil
import utils.FutureSyntax.FutureOps
import views.html.DateOfFirstSaleView

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DateOfFirstSaleController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           cc: AuthenticatedControllerComponents,
                                           formProvider: DateOfFirstSaleFormProvider,
                                           view: DateOfFirstSaleView,
                                           val dateService: DateService,
                                           registrationService: RegistrationService,
                                           rejoinRegistrationService: RejoinRegistrationService,
                                           val clock: Clock
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController
  with I18nSupport with DateOfFirstSaleUtil {

  private def createFutureForm(mode: Mode)(implicit request: AuthenticatedDataRequest[AnyContent]): Future[Form[LocalDate]] = {
    if (mode == AmendMode) {
      registrationService.getLastPossibleDateOfFirstSale(request.registration).map {
        case Some(lastPossibleDateOfFirstSale) =>
          formProvider(lastPossibleDateOfFirstSale)
        case _ =>
          formProvider()
      }
    } else {
      formProvider().toFuture
    }
  }

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = (cc.authAndGetData(Some(mode)) andThen cc.checkEligibleSalesAmendable(Some(mode))).async {
    implicit request =>

      createFutureForm(mode).map { form =>
        val preparedForm = request.userAnswers.get(DateOfFirstSalePage) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        val earliestDateFormatted = getEarliestDateAllowed(mode, dateFormatter)
        val earliestDateHintFormatted = getEarliestDateAllowed(mode, dateHintFormatter)

        Ok(view(preparedForm, mode, earliestDateFormatted, earliestDateHintFormatted))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (cc.authAndGetData(Some(mode)) andThen cc.checkEligibleSalesAmendable(Some(mode))).async {
    implicit request =>

      createFutureForm(mode).flatMap { form =>
        form.bindFromRequest().fold(
          formWithErrors => {

            val earliestDateFormatted = getEarliestDateAllowed(mode, dateFormatter)
            val earliestDateHintFormatted = getEarliestDateAllowed(mode, dateHintFormatter)

            Future.successful(BadRequest(view(formWithErrors, mode, earliestDateFormatted, earliestDateHintFormatted)))
          },

          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(DateOfFirstSalePage, value))
              _ <- cc.sessionRepository.set(updatedAnswers)
            } yield {
              Redirect {
                checkReverseEligible(value).getOrElse(
                  DateOfFirstSalePage.navigate(mode, updatedAnswers)
                )
              }
            }
        )
      }
  }

  private def checkReverseEligible(dateOfFirstSale: LocalDate)(implicit request: AuthenticatedDataRequest[_]): Option[Call] = {
    val canReverse = rejoinRegistrationService.canReverse(dateOfFirstSale, request.registration.flatMap(_.excludedTrader))

    if (canReverse) {
      Some(controllers.rejoin.routes.HybridReversalController.onPageLoad())
    } else {
      None
    }
  }

}
