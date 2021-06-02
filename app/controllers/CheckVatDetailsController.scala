/*
 * Copyright 2021 HM Revenue & Customs
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

import connectors.RegistrationConnector
import controllers.actions._
import forms.CheckVatDetailsFormProvider
import models.{NormalMode, UserAnswers, responses}
import navigation.Navigator
import pages.CheckVatDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.CheckVatDetailsViewModel
import views.html.{CheckVatDetailsView, CheckVatNumberView}

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckVatDetailsController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           cc: AuthenticatedControllerComponents,
                                           navigator: Navigator,
                                           formProvider: CheckVatDetailsFormProvider,
                                           connector: RegistrationConnector,
                                           clock: Clock,
                                           detailsView: CheckVatDetailsView,
                                           numberOnlyView: CheckVatNumberView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(): Action[AnyContent] = (cc.identify andThen cc.checkRegistration andThen cc.getData).async {
    implicit request =>

      request.userAnswers match {
        case Some(answers) =>
          val preparedForm = answers.get(CheckVatDetailsPage).map(form.fill).getOrElse(form)

          answers.vatInfo.map {
            vatInfo =>
              Future.successful(Ok(detailsView(preparedForm, CheckVatDetailsViewModel(request.vrn, vatInfo))))
          }.getOrElse {
            Future.successful(Ok(numberOnlyView(preparedForm, request.vrn)))
          }

        case None =>
          connector.getVatCustomerInfo() flatMap {
            case Right(vatInfo) =>
              val answers = UserAnswers(request.userId, vatInfo = Some(vatInfo), lastUpdated = Instant.now(clock))
              cc.sessionRepository.set(answers).map {
                _ =>
                  Ok(detailsView(form, CheckVatDetailsViewModel(request.vrn, vatInfo)))
              }

            case Left(responses.NotFound) =>
              val answers = UserAnswers(request.userId, vatInfo = None, lastUpdated = Instant.now(clock))
              cc.sessionRepository.set(answers).map {
                _ => Ok(numberOnlyView(form, request.vrn))
              }

            case Left(_) =>
              Future.successful(InternalServerError)
          }
      }
  }

  def onSubmit(): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors => {
          request.userAnswers.vatInfo.map {
            vatInfo =>
              Future.successful(BadRequest(detailsView(formWithErrors, CheckVatDetailsViewModel(request.vrn, vatInfo))))
          }.getOrElse(Future.successful(BadRequest(numberOnlyView(formWithErrors, request.vrn))))
        },

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(CheckVatDetailsPage, value))
            _              <- cc.sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(CheckVatDetailsPage, NormalMode, updatedAnswers))
      )
  }
}
