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

import base.SpecBase
import config.FrontendAppConfig
import controllers.routes
import models.domain.Registration
import models.emailVerification.PasscodeAttemptsStatus
import models.requests.AuthenticatedMandatoryDataRequest
import models.{AmendLoopMode, AmendMode, BusinessContactDetails, CheckLoopMode, CheckMode, Mode, NormalMode, RejoinLoopMode, RejoinMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.BusinessContactDetailsPage
import play.api.inject.bind
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import services.EmailVerificationService
import testutils.RegistrationData
import utils.FutureSyntax.FutureOps

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckBouncedEmailFilterImplSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  class Harness(mode: Option[Mode], frontendAppConfig: FrontendAppConfig, emailVerificationService: EmailVerificationService)
    extends CheckBouncedEmailFilterImpl(mode, frontendAppConfig, emailVerificationService) {

    def callFilter[A](request: AuthenticatedMandatoryDataRequest[A]): Future[Option[Result]] = {
      filter(request)
    }
  }

  private val mockEmailVerificationService: EmailVerificationService = mock[EmailVerificationService]

  private val registration: Registration = RegistrationData.registration.copy(unusableStatus = Some(true))

  private val businessContactDetails: BusinessContactDetails = {
    BusinessContactDetails(
      fullName = registration.contactDetails.fullName,
      telephoneNumber = registration.contactDetails.telephoneNumber,
      emailAddress = registration.contactDetails.emailAddress
    )
  }

  private val updatedUserAnswers: UserAnswers = emptyUserAnswersWithVatInfo
    .set(BusinessContactDetailsPage, businessContactDetails).success.value

  override def beforeEach(): Unit = {
    Mockito.reset(mockEmailVerificationService)
    super.beforeEach()
  }

  "Check Bounced Email Filter" - {

    Seq(AmendMode, RejoinMode).foreach { mode =>

      "when unusable status is true and email address is the same" - {

        s"must return None when email passcode attempt status is Verified in $mode mode" in {

          when(mockEmailVerificationService.isEmailVerified(any(), any())(any())) thenReturn
            PasscodeAttemptsStatus.Verified.toFuture

          val application = applicationBuilder(mode = Some(mode), userAnswers = Some(updatedUserAnswers))
            .overrides(bind[EmailVerificationService].toInstance(mockEmailVerificationService))
            .build()

          running(application) {
            val request = AuthenticatedMandatoryDataRequest(FakeRequest(), testCredentials, vrn, registration, updatedUserAnswers)

            val config = application.injector.instanceOf[FrontendAppConfig]

            val action = new Harness(Some(mode), config, mockEmailVerificationService)

            val result = action.callFilter(request).futureValue

            result mustBe None
          }
        }

        s"must redirect to Email Verification Codes And Emails Exceeded Controller when email passcode attempt status is" +
          s"LockedTooManyLockedEmails in $mode mode" in {

          when(mockEmailVerificationService.isEmailVerified(any(), any())(any())) thenReturn
            PasscodeAttemptsStatus.LockedTooManyLockedEmails.toFuture

          val application = applicationBuilder(mode = Some(mode), userAnswers = Some(updatedUserAnswers))
            .overrides(bind[EmailVerificationService].toInstance(mockEmailVerificationService))
            .build()

          running(application) {
            val request = AuthenticatedMandatoryDataRequest(FakeRequest(), testCredentials, vrn, registration, updatedUserAnswers)

            val config = application.injector.instanceOf[FrontendAppConfig]

            val action = new Harness(Some(mode), config, mockEmailVerificationService)

            val result = action.callFilter(request).futureValue

            result mustBe Some(Redirect(routes.EmailVerificationCodesAndEmailsExceededController.onPageLoad().url))
          }
        }

        s"must redirect to Email Verification Codes Exceeded Controller when email passcode attempt status is" +
          s"LockedPasscodeForSingleEmail in $mode mode" in {

          when(mockEmailVerificationService.isEmailVerified(any(), any())(any())) thenReturn
            PasscodeAttemptsStatus.LockedTooManyLockedEmails.toFuture

          val application = applicationBuilder(mode = Some(mode), userAnswers = Some(updatedUserAnswers))
            .overrides(bind[EmailVerificationService].toInstance(mockEmailVerificationService))
            .build()

          running(application) {
            val request = AuthenticatedMandatoryDataRequest(FakeRequest(), testCredentials, vrn, registration, updatedUserAnswers)

            val config = application.injector.instanceOf[FrontendAppConfig]

            val action = new Harness(Some(mode), config, mockEmailVerificationService)

            val result = action.callFilter(request).futureValue

            result mustBe Some(Redirect(routes.EmailVerificationCodesAndEmailsExceededController.onPageLoad().url))
          }
        }
      }

      "when email address is not the same" - {

        val updatedUserAnswersWithDifferentEmail: UserAnswers = updatedUserAnswers
          .set(BusinessContactDetailsPage, businessContactDetails
            .copy(emailAddress = arbitrary[String].sample.value)).success.value

        s"must return None when unusable status is true in $mode mode" in {

          val application = applicationBuilder(mode = Some(mode), userAnswers = Some(updatedUserAnswersWithDifferentEmail))
            .build()

          running(application) {
            val request = AuthenticatedMandatoryDataRequest(FakeRequest(), testCredentials, vrn, registration, emptyUserAnswers)

            val config = application.injector.instanceOf[FrontendAppConfig]

            val action = new Harness(Some(AmendMode), config, mockEmailVerificationService)

            val result = action.callFilter(request).futureValue

            result mustBe None
          }
        }

        s"must return None when unusable status is false in $mode mode" in {

          val registrationWithUnusableStatusFalse: Registration = registration.copy(unusableStatus = Some(false))

          val application = applicationBuilder(mode = Some(mode), userAnswers = Some(updatedUserAnswersWithDifferentEmail))
            .build()

          running(application) {
            val request = AuthenticatedMandatoryDataRequest(FakeRequest(), testCredentials, vrn, registrationWithUnusableStatusFalse, emptyUserAnswers)

            val config = application.injector.instanceOf[FrontendAppConfig]

            val action = new Harness(Some(AmendMode), config, mockEmailVerificationService)

            val result = action.callFilter(request).futureValue

            result mustBe None
          }
        }
      }

      s"must return None when email verification flag is false when in mode $mode" in {

        when(mockEmailVerificationService.isEmailVerified(any(), any())(any())) thenReturn
          PasscodeAttemptsStatus.LockedTooManyLockedEmails.toFuture

        val application = applicationBuilder(mode = Some(mode), userAnswers = Some(updatedUserAnswers))
          .configure("features.email-verification-enabled" -> false)
          .overrides(bind[EmailVerificationService].toInstance(mockEmailVerificationService))
          .build()

        running(application) {
          val request = AuthenticatedMandatoryDataRequest(FakeRequest(), testCredentials, vrn, registration, updatedUserAnswers)

          val config = application.injector.instanceOf[FrontendAppConfig]

          val action = new Harness(Some(mode), config, mockEmailVerificationService)

          val result = action.callFilter(request).futureValue

          result mustBe None
        }
      }
    }

    Seq(NormalMode, CheckMode, CheckLoopMode, AmendLoopMode, RejoinLoopMode).foreach { mode =>

      s"must return None when mode is $mode" in {

        when(mockEmailVerificationService.isEmailVerified(any(), any())(any())) thenReturn
          PasscodeAttemptsStatus.Verified.toFuture

        val application = applicationBuilder(mode = Some(mode), userAnswers = Some(updatedUserAnswers))
          .overrides(bind[EmailVerificationService].toInstance(mockEmailVerificationService))
          .build()

        running(application) {
          val request = AuthenticatedMandatoryDataRequest(FakeRequest(), testCredentials, vrn, registration, updatedUserAnswers)

          val config = application.injector.instanceOf[FrontendAppConfig]

          val action = new Harness(Some(mode), config, mockEmailVerificationService)

          val result = action.callFilter(request).futureValue

          result mustBe None
        }
      }
    }
  }
}
