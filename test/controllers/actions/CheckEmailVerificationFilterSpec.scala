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

import base.SpecBase
import config.FrontendAppConfig
import models.NormalMode
import models.emailVerification.PasscodeAttemptsStatus.{LockedPasscodeForSingleEmail, LockedTooManyLockedEmails, NotVerified, Verified}
import models.requests.AuthenticatedDataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.EitherValues
import org.scalatestplus.mockito.MockitoSugar
import pages.BusinessContactDetailsPage
import play.api.inject.bind
import play.api.mvc.{Call, Result}
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import services.{EmailVerificationService, SaveForLaterService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpVerbs.GET
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class CheckEmailVerificationFilterSpec extends SpecBase with MockitoSugar with EitherValues {

  class Harness(
                 frontendAppConfig: FrontendAppConfig,
                 emailVerificationService: EmailVerificationService,
                 saveForLaterService: SaveForLaterService
               )
    extends CheckEmailVerificationFilterImpl(None, frontendAppConfig, emailVerificationService, saveForLaterService) {
    def callFilter(request: AuthenticatedDataRequest[_]): Future[Option[Result]] = filter(request)
  }

  private val mockEmailVerificationService = mock[EmailVerificationService]
  private val validEmailAddressUserAnswers = basicUserAnswersWithVatInfo.set(BusinessContactDetailsPage, contactDetails).success.value
  private val saveForLaterService: SaveForLaterService = mock[SaveForLaterService]

  private lazy val globalController = (frontendAppConfig: FrontendAppConfig) =>
    new Harness(frontendAppConfig, mockEmailVerificationService, saveForLaterService)

  ".filter" - {

    "when email verification enabled" - {

      "must return None if no email address is present" in {

        val app = applicationBuilder(None)
          .overrides(bind[EmailVerificationService].toInstance(mockEmailVerificationService))
          .build()

        running(app) {

          val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, None, basicUserAnswersWithVatInfo)
          val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
          val controller: Harness = globalController(frontendAppConfig)

          val result = controller.callFilter(request).futureValue

          result must not be defined
        }
      }

      "must return None when an email address is verified" in {

        val app = applicationBuilder(None)
          .overrides(bind[EmailVerificationService].toInstance(mockEmailVerificationService))
          .build()

        running(app) {

          when(mockEmailVerificationService.isEmailVerified(
            eqTo(contactDetails.emailAddress), eqTo(userAnswersId))(any())) thenReturn
            Future.successful(Verified)

          val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, None, validEmailAddressUserAnswers)
          val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
          val controller: Harness = globalController(frontendAppConfig)

          val result = controller.callFilter(request).futureValue

          result must not be defined
        }
      }

      "must redirect to Business Contact Details page when an email address is not verified" in {

        val app = applicationBuilder(None)
          .configure("features.email-verification-enabled" -> "true")
          .overrides(bind[EmailVerificationService].toInstance(mockEmailVerificationService))
          .build()

        running(app) {

          when(mockEmailVerificationService.isEmailVerified(
            eqTo(contactDetails.emailAddress), eqTo(userAnswersId))(any())) thenReturn
            Future.successful(NotVerified)


          val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, None, validEmailAddressUserAnswers)
          val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
          val controller: Harness = globalController(frontendAppConfig)

          val result = controller.callFilter(request).futureValue

          result mustBe Some(Redirect(controllers.routes.BusinessContactDetailsController.onPageLoad(NormalMode).url))
        }
      }

      "must redirect to Email Verification Codes Exceeded page and save for later when verification attempts on a single email are exceeded" in {

        val app = applicationBuilder(None)
          .configure("features.email-verification-enabled" -> "true")
          .overrides(bind[EmailVerificationService].toInstance(mockEmailVerificationService))
          .build()

        val redirectLocation = controllers.routes.EmailVerificationCodesExceededController.onPageLoad()

        running(app) {

          when(mockEmailVerificationService.isEmailVerified(
            eqTo(contactDetails.emailAddress), eqTo(userAnswersId)) (any())) thenReturn
            Future.successful(LockedPasscodeForSingleEmail)

          val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, None, validEmailAddressUserAnswers)
          val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

          (ExecutionContext.global, request, hc)
          when(saveForLaterService.saveAnswers(
            eqTo(redirectLocation),
            eqTo(Call(GET, request.uri))) (any(), any(), any())
          ) thenReturn Future(Redirect(redirectLocation.url))



          val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
          val controller: Harness = globalController(frontendAppConfig)

          val result = controller.callFilter(request).futureValue

          result mustBe Some(Redirect(controllers.routes.EmailVerificationCodesExceededController.onPageLoad().url))
        }
      }

      "must redirect to Email Verification Codes and Emails Exceeded page when verification attempts on maximum email addresses are exceeded" in {

        val app = applicationBuilder(None)
          .configure("features.email-verification-enabled" -> "true")
          .overrides(bind[EmailVerificationService].toInstance(mockEmailVerificationService))
          .build()

        running(app) {

          when(mockEmailVerificationService.isEmailVerified(
            eqTo(contactDetails.emailAddress), eqTo(userAnswersId))(any())) thenReturn
            Future.successful(LockedTooManyLockedEmails)


          val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, None, validEmailAddressUserAnswers)
          val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
          val controller: Harness = globalController(frontendAppConfig)

          val result = controller.callFilter(request).futureValue

          result mustBe Some(Redirect(controllers.routes.EmailVerificationCodesAndEmailsExceededController.onPageLoad().url))
        }
      }
    }

    "when email verification disabled" - {

      "must return None if no email address is present" in {

        val app = applicationBuilder(None)
          .configure("features.email-verification-enabled" -> "false")
          .build()

        running(app) {

          val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, None, basicUserAnswersWithVatInfo)
          val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
          val controller: Harness = globalController(frontendAppConfig)

          val result = controller.callFilter(request).futureValue

          result must not be defined
        }
      }
    }
  }

}
