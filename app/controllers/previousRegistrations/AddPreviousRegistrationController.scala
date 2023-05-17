/*
 * Copyright 2023 HM Revenue & Customs
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
import models.domain.{PreviousRegistrationLegacy, PreviousRegistrationNew, Registration}
import models.{AmendMode, Country, Mode}
import models.previousRegistrations.PreviousRegistrationDetailsWithOptionalVatNumber
import models.requests.AuthenticatedDataRequest
import pages.previousRegistrations.AddPreviousRegistrationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.previousRegistration.DeriveNumberOfPreviousRegistrations
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
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

  private val exception = new IllegalStateException("Can't amend a non-existent registration")

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      getNumberOfPreviousRegistrations {
        number =>

          val canAddCountries = number < Country.euCountries.size

          val previousRegistrations = if (mode == AmendMode) {
            val registration: Registration = request.registration match {
              case Some(registration) => registration
              case None =>
                logger.error(exception.getMessage, exception)
                throw exception
            }

            registration.previousRegistrations.map {
              case previousRegistrationNew: PreviousRegistrationNew =>
                previousRegistrationNew.country
              case previousRegistrationLegacy: PreviousRegistrationLegacy =>
                previousRegistrationLegacy.country
            }
            PreviousRegistrationSummary.addToListRows(request.userAnswers, Some(registration.previousRegistrations), mode)
          } else {
            PreviousRegistrationSummary.addToListRows(request.userAnswers, None, mode)
          }

          withCompleteDataAsync[PreviousRegistrationDetailsWithOptionalVatNumber](
            data = getAllIncompleteDeregisteredDetails,
            onFailure = (incomplete: Seq[PreviousRegistrationDetailsWithOptionalVatNumber]) => {
              Future.successful(Ok(view(form, mode, previousRegistrations, canAddCountries, incomplete)))
            }) {
            Future.successful(Ok(view(form, mode, previousRegistrations, canAddCountries)))
          }
      }
  }

  def onSubmit(mode: Mode, incompletePromptShown: Boolean): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      withCompleteDataAsync[PreviousRegistrationDetailsWithOptionalVatNumber](
        data = getAllIncompleteDeregisteredDetails,
        onFailure = (incomplete: Seq[PreviousRegistrationDetailsWithOptionalVatNumber]) => {
          if (incompletePromptShown) {
            incompletePreviousRegistrationRedirect(mode).map(
              redirectIncompletePage => redirectIncompletePage.toFuture
            ).getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
          } else {
            Future.successful(Redirect(routes.AddPreviousRegistrationController.onPageLoad(mode)))
          }
        }) {
        getNumberOfPreviousRegistrations {
          number =>

            val canAddCountries = number < Country.euCountries.size

            val previousRegistrations = if (mode == AmendMode) {
              val registration: Registration = request.registration match {
                case Some(registration) => registration
                case None => logger.error(exception.getMessage, exception)
                  throw exception
              }

              registration.previousRegistrations.map {
                case previousRegistrationNew: PreviousRegistrationNew =>
                  previousRegistrationNew.country
                case previousRegistrationLegacy: PreviousRegistrationLegacy =>
                  previousRegistrationLegacy.country
              }
              PreviousRegistrationSummary.addToListRows(request.userAnswers, Some(registration.previousRegistrations), mode)
            } else {
              PreviousRegistrationSummary.addToListRows(request.userAnswers, None, mode)
            }

            form.bindFromRequest().fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, mode, previousRegistrations, canAddCountries))),

              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(AddPreviousRegistrationPage, value))
                  _ <- cc.sessionRepository.set(updatedAnswers)
                } yield Redirect(AddPreviousRegistrationPage.navigate(mode, updatedAnswers))
            )
        }
      }
  }

  private def getNumberOfPreviousRegistrations(block: Int => Future[Result])
                                              (implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(DeriveNumberOfPreviousRegistrations).map {
      number =>
        block(number)
    }.getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
}
