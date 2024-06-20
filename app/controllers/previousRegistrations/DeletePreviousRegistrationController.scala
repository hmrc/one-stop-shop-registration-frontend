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

package controllers.previousRegistrations

import controllers.actions._
import forms.previousRegistrations.DeletePreviousRegistrationFormProvider
import models.previousRegistrations.PreviousRegistrationDetailsWithOptionalVatNumber
import models.requests.AuthenticatedDataRequest
import models.{AmendMode, Index, Mode, RejoinMode}
import pages.previousRegistrations.DeletePreviousRegistrationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.previousRegistration._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckExistingRegistrations.{checkExistingRegistration, existingPreviousRegistration}
import utils.CheckJourneyRecovery.determineJourneyRecovery
import utils.FutureSyntax.FutureOps
import views.html.previousRegistrations.DeletePreviousRegistrationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeletePreviousRegistrationController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              cc: AuthenticatedControllerComponents,
                                              formProvider: DeletePreviousRegistrationFormProvider,
                                              view: DeletePreviousRegistrationView
                                            )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      getPreviousRegistration(mode, index) {
        details =>

          if (mode == AmendMode || mode == RejoinMode) {
            val existingPreviousRegistrations = checkExistingRegistration().previousRegistrations

            if (existingPreviousRegistration(details.previousEuCountry, existingPreviousRegistrations)) {
              Future.successful(Redirect(routes.CannotRemoveExistingPreviousRegistrationsController.onPageLoad()))
            } else {
              Future.successful(Ok(view(form, mode, index, details.previousEuCountry.name)))
            }
          } else {
            Future.successful(Ok(view(form, mode, index, details.previousEuCountry.name)))
          }
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      getPreviousRegistration(mode, index) {
        details =>

          if (mode == AmendMode || mode == RejoinMode) {
            val existingPreviousRegistrations = checkExistingRegistration().previousRegistrations

            if (existingPreviousRegistration(details.previousEuCountry, existingPreviousRegistrations)) {
              Future.successful(Redirect(routes.CannotRemoveExistingPreviousRegistrationsController.onPageLoad()))
            } else {
              saveAndRedirect(mode, index, details.previousEuCountry.name)
            }
          } else {
            saveAndRedirect(mode, index, details.previousEuCountry.name)
          }
      }
  }


  private def saveAndRedirect(mode: Mode, index: Index, countryName: String)(implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] = {
    form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors, mode, index, countryName))),

      value =>
        if (value) {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.remove(PreviousRegistrationQuery(index)))
            _ <- cc.sessionRepository.set(updatedAnswers)
          } yield Redirect(DeletePreviousRegistrationPage(index).navigate(mode, updatedAnswers))
        } else {
          Future.successful(Redirect(DeletePreviousRegistrationPage(index).navigate(mode, request.userAnswers)))
        }
    )
  }

  private def getPreviousRegistration(mode: Mode, index: Index)
                                     (block: PreviousRegistrationDetailsWithOptionalVatNumber => Future[Result])
                                     (implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(PreviousRegistrationWithOptionalVatNumberQuery(index)).map {
      details =>
        block(details)
    }.getOrElse(Redirect(determineJourneyRecovery(Some(mode))).toFuture)
}
