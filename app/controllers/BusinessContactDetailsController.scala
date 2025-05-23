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

import config.FrontendAppConfig
import controllers.actions.*
import forms.BusinessContactDetailsFormProvider
import logging.Logging
import models.emailVerification.PasscodeAttemptsStatus.{LockedPasscodeForSingleEmail, LockedTooManyLockedEmails, NotVerified, Verified}
import models.requests.AuthenticatedDataRequest
import models.{AmendMode, BusinessContactDetails, CheckMode, Mode, NormalMode, RejoinMode}
import pages.BusinessContactDetailsPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{EmailVerificationService, SaveForLaterService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps
import views.html.BusinessContactDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessContactDetailsController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  cc: AuthenticatedControllerComponents,
                                                  saveForLaterService: SaveForLaterService,
                                                  emailVerificationService: EmailVerificationService,
                                                  formProvider: BusinessContactDetailsFormProvider,
                                                  config: FrontendAppConfig,
                                                  view: BusinessContactDetailsView
                                                )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData(Some(mode)) {
    implicit request =>

      val preparedForm = request.userAnswers.get(BusinessContactDetailsPage) match {
        case None => fillIossBusinessContactDetailsForm(request)
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, config.enrolmentsEnabled, request.latestIossRegistration, request.numberOfIossRegistrations))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>

      val messages = messagesApi.preferred(request)

      form.bindFromRequest().fold(
        formWithErrors =>
          BadRequest(view(formWithErrors, mode, config.enrolmentsEnabled, request.latestIossRegistration, request.numberOfIossRegistrations)).toFuture,

        value => {

          val continueUrl = if (mode == CheckMode) {
            s"${config.loginContinueUrl}${routes.CheckYourAnswersController.onPageLoad().url}"
          } else if (mode == AmendMode) {
            s"${config.loginContinueUrl}${controllers.amend.routes.ChangeYourRegistrationController.onPageLoad().url}"
          } else if (mode == RejoinMode) {
            s"${config.loginContinueUrl}${controllers.rejoin.routes.RejoinRegistrationController.onPageLoad().url}"
          } else {
            s"${config.loginContinueUrl}${routes.BankDetailsController.onPageLoad(NormalMode).url}"
          }

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
            case _ => Redirect(routes.BusinessContactDetailsController.onPageLoad(mode).url).toFuture
          }
    }
  }

  private def fillIossBusinessContactDetailsForm(request: AuthenticatedDataRequest[_]): Form[BusinessContactDetails] = {
    request.latestIossRegistration match {
      case Some(iossEtmpDisplayRegistration) =>
        form.fill(
          BusinessContactDetails(
            fullName = iossEtmpDisplayRegistration.schemeDetails.contactName,
            telephoneNumber = iossEtmpDisplayRegistration.schemeDetails.businessTelephoneNumber,
            emailAddress = iossEtmpDisplayRegistration.schemeDetails.businessEmailId
          )
        )

      case _ => form
    }
  }
}
