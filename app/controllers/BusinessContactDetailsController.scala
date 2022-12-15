/*
 * Copyright 2022 HM Revenue & Customs
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

import config.FrontendAppConfig
import connectors.SaveForLaterConnector
import controllers.actions._
import forms.BusinessContactDetailsFormProvider
import logging.Logging
import models.emailVerification.PasscodeAttemptsStatus.{LockedPasscodeForSingleEmail, LockedTooManyLockedEmails, NotVerified, Verified}
import models.requests.AuthenticatedDataRequest
import models.{BusinessContactDetails, CheckMode, Mode, NormalMode}
import pages.BusinessContactDetailsPage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{EmailVerificationService, SaveForLaterService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.BusinessContactDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessContactDetailsController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  cc: AuthenticatedControllerComponents,
                                                  saveForLaterConnector: SaveForLaterConnector,
                                                  saveForLaterService: SaveForLaterService,
                                                  emailVerificationService: EmailVerificationService,
                                                  formProvider: BusinessContactDetailsFormProvider,
                                                  config: FrontendAppConfig,
                                                  view: BusinessContactDetailsView
                                                )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData() {
    implicit request =>

      val preparedForm = request.userAnswers.get(BusinessContactDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, config.enrolmentsEnabled))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>

      val messages = messagesApi.preferred(request)

      val continueUrl = if (mode == CheckMode) {
        routes.CheckYourAnswersController.onPageLoad().url
      } else {
        routes.BankDetailsController.onPageLoad(NormalMode).url
      }

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, config.enrolmentsEnabled))),

        value => {

          if (config.emailVerificationEnabled) {
            verifyEmailAndRedirect(mode, messages, continueUrl, value)
          } else {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(BusinessContactDetailsPage, value))
              _ <- cc.sessionRepository.set(updatedAnswers)
            } yield Redirect(BusinessContactDetailsPage.navigate(mode, updatedAnswers))
          }
        }
      )
  }

  private def verifyEmailAndRedirect(
                                      mode: Mode,
                                      messages: Messages,
                                      continueUrl: String,
                                      value: BusinessContactDetails
                                    )(implicit hc: HeaderCarrier, request: AuthenticatedDataRequest[AnyContent]): Future[Result] = {
    lazy val emailVerificationRequest = emailVerificationService.createEmailVerificationRequest(
      mode,
      request.userId,
      value.emailAddress,
      Some(messages("service.name")),
      continueUrl
    )

    emailVerificationService.isEmailVerified(value.emailAddress, request.userId).flatMap {
      case Verified =>
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.set(BusinessContactDetailsPage, value))
          _ <- cc.sessionRepository.set(updatedAnswers)
        } yield Redirect(BusinessContactDetailsPage.navigate(mode, updatedAnswers))

      case LockedPasscodeForSingleEmail =>
        saveForLaterService.saveAnswers(
          routes.EmailVerificationCodesExceededController.onPageLoad(),
          routes.BusinessContactDetailsController.onPageLoad(mode)
        )

      case LockedTooManyLockedEmails =>
        saveForLaterService.saveAnswers(
          routes.EmailVerificationCodesAndEmailsExceededController.onPageLoad(),
          routes.BusinessContactDetailsController.onPageLoad(mode)
        )

      case NotVerified =>
        emailVerificationRequest
          .flatMap {
            case Right(validResponse) =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(BusinessContactDetailsPage, value))
                _ <- cc.sessionRepository.set(updatedAnswers)
              } yield Redirect(s"${config.emailVerificationUrl}${validResponse.redirectUri}")
            case _ => Future.successful(Redirect(routes.BusinessContactDetailsController.onPageLoad(NormalMode).url))
          }
    }
  }

}
