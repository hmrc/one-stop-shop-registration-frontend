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
import models.{AmendMode, Mode, RejoinMode}
import pages.DateOfFirstSalePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{DateService, RegistrationService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DateOfFirstSaleView

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DateOfFirstSaleController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           cc: AuthenticatedControllerComponents,
                                           formProvider: DateOfFirstSaleFormProvider,
                                           view: DateOfFirstSaleView,
                                           dateService: DateService,
                                           registrationService: RegistrationService,
                                           clock: Clock
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def createFutureForm()(implicit request: AuthenticatedDataRequest[AnyContent]): Future[Form[LocalDate]] = {
    registrationService.getLastPossibleDateOfFirstSale(request.registration).map {
      case Some(lastPossibleDateOfFirstSale) =>
        formProvider(lastPossibleDateOfFirstSale)
      case _ =>
        formProvider()
    }
  }

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = (cc.authAndGetData(Some(mode)) andThen cc.checkEligibleSalesAmendable(Some(mode))).async {
    implicit request =>

      createFutureForm().map { form =>
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

      createFutureForm().flatMap { form =>
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
            } yield Redirect(DateOfFirstSalePage.navigate(mode, updatedAnswers))
        )
      }
  }

  private def getEarliestDateAllowed(mode: Mode, dateTimeFormatter: DateTimeFormatter)(implicit request: AuthenticatedDataRequest[AnyContent]) = {
    if (mode == AmendMode || mode == RejoinMode) {
      request.registration.flatMap(_.submissionReceived) match {
        case Some(submissionReceived) =>
          dateService.earliestSaleAllowed(submissionReceived.atZone(clock.getZone).toLocalDate).format(dateTimeFormatter)
        case _ => dateService.earliestSaleAllowed().format(dateTimeFormatter)
      }
    } else {
      dateService.earliestSaleAllowed().format(dateTimeFormatter)
    }
  }

}
