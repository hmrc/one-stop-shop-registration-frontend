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

package controllers.actions

import config.FrontendAppConfig
import logging.Logging
import models.{AmendMode, BusinessContactDetails, Mode, NormalMode, RejoinMode}
import models.emailVerification.PasscodeAttemptsStatus.{LockedPasscodeForSingleEmail, LockedTooManyLockedEmails, NotVerified, Verified}
import models.requests.AuthenticatedDataRequest
import pages.BusinessContactDetailsPage
import play.api.mvc.{ActionFilter, Call, Result}
import play.api.mvc.Results.Redirect
import services.{EmailVerificationService, SaveForLaterService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpVerbs.GET
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.FutureSyntax.FutureOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckEmailVerificationFilterImpl(mode: Option[Mode],
                                       frontendAppConfig: FrontendAppConfig,
                                       emailVerificationService: EmailVerificationService,
                                       saveForLaterService: SaveForLaterService,
                                      )(implicit val executionContext: ExecutionContext)
  extends ActionFilter[AuthenticatedDataRequest] with Logging {

  override protected def filter[A](request: AuthenticatedDataRequest[A]): Future[Option[Result]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    if (frontendAppConfig.emailVerificationEnabled) {
      request.userAnswers.get(BusinessContactDetailsPage) match {
        case Some(contactDetails) =>
          if (mode.contains(AmendMode) || mode.contains(RejoinMode)) {
            request.registration match {
              case Some(registration) =>
                if (registration.contactDetails.emailAddress != contactDetails.emailAddress) {
                  checkVerificationStatusAndGetRedirect(request, contactDetails)
                } else {
                  None.toFuture
                }
              case None =>
                val exception = new Exception(s"Error when getting registration during email verification check on an amend journey")
                logger.error(exception.getMessage, exception)
                throw exception
            }
          } else {
            checkVerificationStatusAndGetRedirect(request, contactDetails)
          }
        case None => Future.successful(Option.empty[Result])
      }
    } else {
      Future.successful(Option.empty[Result])
    }
  }

  private def checkVerificationStatusAndGetRedirect(request: AuthenticatedDataRequest[_],
                                                    contactDetails: BusinessContactDetails
                                                   )(implicit hc: HeaderCarrier): Future[Option[Result]] = {
    emailVerificationService.isEmailVerified(contactDetails.emailAddress, request.userId).flatMap {
      case Verified =>
        logger.info("CheckEmailVerificationFilter - Verified")
        Future(Option.empty[Result])

      case LockedTooManyLockedEmails =>
        logger.info("CheckEmailVerificationFilter - LockedTooManyLockedEmails")
        Future.successful(Option(Redirect(controllers.routes.EmailVerificationCodesAndEmailsExceededController.onPageLoad().url)))

      case LockedPasscodeForSingleEmail =>
        logger.info("CheckEmailVerificationFilter - LockedPasscodeForSingleEmail")
        saveForLaterService.saveAnswers(
          controllers.routes.EmailVerificationCodesExceededController.onPageLoad(),
          Call(GET, request.uri)
        )(request, executionContext, hc).map(Option(_))

      case NotVerified =>
        logger.info("CheckEmailVerificationFilter - Not Verified")
        if (mode.contains(AmendMode)) {
          Future.successful(Option(Redirect(controllers.routes.BusinessContactDetailsController.onPageLoad(AmendMode).url)))
        } else if (mode.contains(RejoinMode)) {
          Future.successful(Option(Redirect(controllers.routes.BusinessContactDetailsController.onPageLoad(RejoinMode).url)))
        } else {
        Future.successful(Option(Redirect(controllers.routes.BusinessContactDetailsController.onPageLoad(NormalMode).url)))
      }
    }
  }
}

class CheckEmailVerificationFilterProvider @Inject()(
                                                      frontendAppConfig: FrontendAppConfig,
                                                      emailVerificationService: EmailVerificationService,
                                                      saveForLaterService: SaveForLaterService,
                                                    )(implicit val executionContext: ExecutionContext) {
  def apply(mode: Option[Mode]): CheckEmailVerificationFilterImpl = {
    new CheckEmailVerificationFilterImpl(mode, frontendAppConfig, emailVerificationService, saveForLaterService)
  }
}

