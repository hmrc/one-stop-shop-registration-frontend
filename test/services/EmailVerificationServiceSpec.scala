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

package services

import base.SpecBase
import config.FrontendAppConfig
import connectors.EmailVerificationConnector
import models.NormalMode
import models.emailVerification.{EmailStatus, EmailVerificationResponse, VerificationStatus}
import models.responses.UnexpectedResponseStatus
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.{BAD_GATEWAY, BAD_REQUEST, INTERNAL_SERVER_ERROR, UNAUTHORIZED}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmailVerificationServiceSpec extends SpecBase {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val config = mock[FrontendAppConfig]
  private val mockEmailVerificationConnector = mock[EmailVerificationConnector]
  private val emailVerificationService = new EmailVerificationService(config, mockEmailVerificationConnector)

  private val journeyId = "98fe3788-2d39-409c-b400-8f86ed1634ea"
  private val emailVerificationResponse = EmailVerificationResponse(redirectUri = journeyId)


  ".createEmailVerificationRequest" - {

    "must return a verification code when email verification connector verifyEmail method called with valid payload" in {

      val continueUrl: String = "/continueUrl"

      when(config.origin) thenReturn "OSS"
      when(mockEmailVerificationConnector.verifyEmail(any())(any(), any())) thenReturn Future.successful(Right(emailVerificationResponse))

      val result = emailVerificationService.createEmailVerificationRequest(
        NormalMode,
        userAnswersId,
        contactDetails.emailAddress,
        Some("Page title"),
        continueUrl
      )

      result.futureValue mustBe journeyId
    }
  }

  ".isEmailVerified" - {

    "must return true if email is verified" in {

      val verifiedEmail = EmailStatus(emailAddress = contactDetails.emailAddress, verified = true, locked = false)
      val verificationStatus: VerificationStatus = VerificationStatus(Seq(verifiedEmail))

      when(mockEmailVerificationConnector.getStatus(userAnswersId)) thenReturn Future.successful(Right(Some(verificationStatus)))

      val result = emailVerificationService.isEmailVerified(contactDetails.emailAddress, userAnswersId).futureValue

      result mustBe true

    }

    "must return false if email is not verified" in {

      val verifiedEmail = EmailStatus(emailAddress = contactDetails.emailAddress, verified = false, locked = false)
      val verificationStatus: VerificationStatus = VerificationStatus(Seq(verifiedEmail))

      when(mockEmailVerificationConnector.getStatus(userAnswersId)) thenReturn Future.successful(Right(Some(verificationStatus)))

      val result = emailVerificationService.isEmailVerified(contactDetails.emailAddress, userAnswersId).futureValue

      result mustBe false

    }

    "must return false if verification not found" in {

      when(mockEmailVerificationConnector.getStatus(userAnswersId)) thenReturn Future.successful(Right(None))

      val result = emailVerificationService.isEmailVerified(contactDetails.emailAddress, userAnswersId).futureValue

      result mustBe false

    }

    "must return false when an error is received" in {

      val errorCode = Gen.oneOf(BAD_REQUEST, UNAUTHORIZED, INTERNAL_SERVER_ERROR, BAD_GATEWAY).sample.value

      when(mockEmailVerificationConnector.getStatus(userAnswersId)) thenReturn
        Future.successful(Left(UnexpectedResponseStatus(errorCode, "error")))

      val result = Future.successful(false).futureValue

      result mustBe false

    }

  }

}

