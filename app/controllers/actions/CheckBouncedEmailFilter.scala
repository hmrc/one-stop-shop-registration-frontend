/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.actions

import config.FrontendAppConfig
import controllers.routes
import logging.Logging
import models.emailVerification.PasscodeAttemptsStatus.{LockedPasscodeForSingleEmail, LockedTooManyLockedEmails, NotVerified, Verified}
import models.requests.AuthenticatedMandatoryDataRequest
import models.{AmendMode, Mode, RejoinMode}
import pages.BusinessContactDetailsPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import services.EmailVerificationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.FutureSyntax.FutureOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckBouncedEmailFilterImpl(
                                   mode: Option[Mode],
                                   frontendAppConfig: FrontendAppConfig,
                                   emailVerificationService: EmailVerificationService
                                 )(implicit val executionContext: ExecutionContext)
  extends ActionFilter[AuthenticatedMandatoryDataRequest] with Logging {

  override protected def filter[A](request: AuthenticatedMandatoryDataRequest[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    val emailAddress: String = request.registration.contactDetails.emailAddress
    val isEmailMatched: Boolean = request.userAnswers.get(BusinessContactDetailsPage).exists(_.emailAddress == emailAddress)

    if (request.registration.unusableStatus.getOrElse(false) && isEmailMatched && frontendAppConfig.emailVerificationEnabled) {
      checkVerificationStatusAndGetRedirect(mode, request.userId, emailAddress)
    } else {
      None.toFuture
    }
  }

  private def checkVerificationStatusAndGetRedirect(
                                                     mode: Option[Mode],
                                                     userId: String,
                                                     emailAddress: String
                                                   )(implicit hc: HeaderCarrier): Future[Option[Result]] = {

    emailVerificationService.isEmailVerified(emailAddress, userId).flatMap {
      case Verified =>
        logger.info("CheckBouncedEmailFilter - Verified")
        None.toFuture

      case LockedTooManyLockedEmails =>
        logger.info("CheckBouncedEmailFilter - LockedTooManyLockedEmails")
        Some(Redirect(routes.EmailVerificationCodesAndEmailsExceededController.onPageLoad().url)).toFuture

      case LockedPasscodeForSingleEmail =>
        logger.info("CheckBouncedEmailFilter - LockedPasscodeForSingleEmail")
        Some(Redirect(routes.EmailVerificationCodesExceededController.onPageLoad().url)).toFuture

      case NotVerified =>
        logger.info("CheckBouncedEmailFilter - NotVerified")
        mode match {
          case Some(otherMode) if otherMode == AmendMode || otherMode == RejoinMode =>
            Some(Redirect(routes.BusinessContactDetailsController.onPageLoad(otherMode).url)).toFuture

          case _ =>
            None.toFuture
        }
    }
  }
}

class CheckBouncedEmailFilterProvider @Inject()(
                                                 frontendAppConfig: FrontendAppConfig,
                                                 emailVerificationService: EmailVerificationService
                                               )(implicit executionContext: ExecutionContext) {

  def apply(mode: Option[Mode]): CheckBouncedEmailFilterImpl =
    new CheckBouncedEmailFilterImpl(mode, frontendAppConfig, emailVerificationService)
}
