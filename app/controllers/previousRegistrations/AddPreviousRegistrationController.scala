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
import forms.previousRegistrations.AddPreviousRegistrationFormProvider
import logging.Logging
import models.domain.Registration
import models.previousRegistrations.PreviousRegistrationDetailsWithOptionalFields
import models.requests.AuthenticatedDataRequest
import models.{AmendMode, Country, Mode, RejoinMode}
import pages.previousRegistrations.{AddPreviousRegistrationPage, PreviouslyRegisteredPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.previousRegistration.DeriveNumberOfPreviousRegistrations
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckExistingRegistrations.checkExistingRegistration
import utils.CheckJourneyRecovery.determineJourneyRecovery
import utils.CompletionChecks
import utils.FutureSyntax.FutureOps
import viewmodels.checkAnswers.previousRegistrations.PreviousRegistrationSummary
import views.html.previousRegistrations.AddPreviousRegistrationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddPreviousRegistrationController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   cc: AuthenticatedControllerComponents,
                                                   formProvider: AddPreviousRegistrationFormProvider,
                                                   view: AddPreviousRegistrationView
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with CompletionChecks with I18nSupport with Logging {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>

      getNumberOfPreviousRegistrations(mode) {
        number =>

          val canAddCountries = number < Country.euCountries.size

          val previousRegistrations = if (mode == AmendMode || mode == RejoinMode) {
            val registration: Registration = checkExistingRegistration()
            PreviousRegistrationSummary.addToListRows(request.userAnswers, registration.previousRegistrations, mode)
          } else {
            PreviousRegistrationSummary.addToListRows(request.userAnswers, Seq.empty, mode)
          }

          withCompleteDataAsync[PreviousRegistrationDetailsWithOptionalFields](
            data = getAllIncompleteDeregisteredDetails _,
            onFailure = (incomplete: Seq[PreviousRegistrationDetailsWithOptionalFields]) => {
              Future.successful(Ok(view(form, mode, previousRegistrations, canAddCountries, incomplete)))
            }) {
            Future.successful(Ok(view(form, mode, previousRegistrations, canAddCountries)))
          }
      }
  }

  def onSubmit(mode: Mode, incompletePromptShown: Boolean): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      withCompleteDataAsync[PreviousRegistrationDetailsWithOptionalFields](
        data = getAllIncompleteDeregisteredDetails _,
        onFailure = (_: Seq[PreviousRegistrationDetailsWithOptionalFields]) => {
          if (incompletePromptShown) {
            incompletePreviousRegistrationRedirect(mode).map(
              redirectIncompletePage => redirectIncompletePage.toFuture
            ).getOrElse(Redirect(determineJourneyRecovery(Some(mode))).toFuture)
          } else {
            Future.successful(Redirect(routes.AddPreviousRegistrationController.onPageLoad(mode)))
          }
        }) {
        getNumberOfPreviousRegistrations(mode) {
          number =>

            val canAddCountries = number < Country.euCountries.size

            val previousRegistrations = if (mode == AmendMode || mode == RejoinMode) {
              val registration: Registration = checkExistingRegistration()
              PreviousRegistrationSummary.addToListRows(request.userAnswers, registration.previousRegistrations, mode)
            } else {
              PreviousRegistrationSummary.addToListRows(request.userAnswers, Seq.empty, mode)
            }

            form.bindFromRequest().fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, previousRegistrations, canAddCountries))),

              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(AddPreviousRegistrationPage, value))
                  updatedAnswers2 <- if(number == 0 && !value) {
                    Future.fromTry(
                      updatedAnswers.set(PreviouslyRegisteredPage, false)
                    )
                  } else {
                    Future.successful(updatedAnswers)
                  }
                  _ <- cc.sessionRepository.set(updatedAnswers2)
                } yield Redirect(AddPreviousRegistrationPage.navigate(mode, updatedAnswers2))
            )
        }
      }
  }

  private def getNumberOfPreviousRegistrations(mode: Mode)(block: Int => Future[Result])
                                              (implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(DeriveNumberOfPreviousRegistrations).map {
      number =>
        block(number)
    }.getOrElse(Redirect(determineJourneyRecovery(Some(mode))).toFuture)
}
