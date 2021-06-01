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

package controllers.previousRegistrations

import controllers.actions._
import forms.previousRegistrations.AddPreviousRegistrationFormProvider
import models.{Country, Mode}
import models.requests.DataRequest
import navigation.Navigator
import pages.previousRegistrations.AddPreviousRegistrationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.DeriveNumberOfPreviousRegistrations
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.previousRegistrations.PreviousRegistrationSummary
import views.html.previousRegistrations.AddPreviousRegistrationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddPreviousRegistrationController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         cc: AuthenticatedControllerComponents,
                                         navigator: Navigator,
                                         formProvider: AddPreviousRegistrationFormProvider,
                                         view: AddPreviousRegistrationView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getNumberOfPreviousRegistrations {
        number =>

          val canAddCountries = number < Country.euCountries.size
          val previousRegistrations = PreviousRegistrationSummary.addToListRows(request.userAnswers)

          Future.successful(Ok(view(form, mode, previousRegistrations, canAddCountries)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getNumberOfPreviousRegistrations {
        number =>

          val canAddCountries = number < Country.euCountries.size
          val previousRegistrations = PreviousRegistrationSummary.addToListRows(request.userAnswers)

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, previousRegistrations, canAddCountries))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(AddPreviousRegistrationPage, value))
                _              <- cc.sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(AddPreviousRegistrationPage, mode, updatedAnswers))
          )
      }
  }

  private def getNumberOfPreviousRegistrations(block: Int => Future[Result])
                                              (implicit request: DataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(DeriveNumberOfPreviousRegistrations).map {
      number =>
        block(number)
    }.getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
}
