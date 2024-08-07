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
import formats.Format.dateFormatter
import forms.HasMadeSalesFormProvider
import models.SalesChannels.Mixed
import models.{Mode, UserAnswers}
import pages.{BusinessBasedInNiPage, HasFixedEstablishmentInNiPage, HasMadeSalesPage, SalesChannelsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.DateService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.DateOfFirstSaleUtil
import views.html.HasMadeSalesView

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HasMadeSalesController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        cc: AuthenticatedControllerComponents,
                                        formProvider: HasMadeSalesFormProvider,
                                        view: HasMadeSalesView,
                                        val dateService: DateService,
                                        val clock: Clock
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController
  with I18nSupport with DateOfFirstSaleUtil {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = (cc.authAndGetData(Some(mode)) andThen cc.checkEligibleSalesAmendable(Some(mode))) {
    implicit request =>

      val preparedForm = request.userAnswers.get(HasMadeSalesPage) match {
        case Some(answer) => form.fill(answer)
        case None => form
      }

      Ok(view(preparedForm, mode, showHintText(request.userAnswers), getEarliestDateAllowed(mode, dateFormatter)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (cc.authAndGetData(Some(mode)) andThen cc.checkEligibleSalesAmendable(Some(mode))).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(
            formWithErrors, mode, showHintText(request.userAnswers),
            getEarliestDateAllowed(mode, dateFormatter)
          ))),

        value => {
          val updatedAnswersFuture = for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(HasMadeSalesPage, value))
            _ <- cc.sessionRepository.set(updatedAnswers)
          } yield updatedAnswers

          updatedAnswersFuture.map { updatedAnswers =>
            Redirect(HasMadeSalesPage.navigate(mode, updatedAnswers))
          }
        }
      )
  }

  private def showHintText(answers: UserAnswers): Boolean = {
    (answers.get(BusinessBasedInNiPage), answers.get(HasFixedEstablishmentInNiPage), answers.get(SalesChannelsPage)) match {
      case (Some(false), Some(false), Some(Mixed)) => true
      case _ => false
    }
  }
}
