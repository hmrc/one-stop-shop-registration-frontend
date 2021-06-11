/*
 * Copyright 2021 HM Revenue & Customs
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
import connectors.EmailConnector
import models.emails.EmailSendingResult.EMAIL_ACCEPTED
import models.emails.{EmailToSendRequest, RegistrationConfirmationEmailParameters}
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmailServiceSpec extends SpecBase {

  private val connector = mock[EmailConnector]
  private val emailService = new EmailService(connector)
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "EmailService.sendConfirmationEmail" - {

    "call sendConfirmationEmail with the correct parameters" in {
      forAll(validVRNs, validEmails) {
        (vatNum: String, validEmail: String) =>
            val emailParams = RegistrationConfirmationEmailParameters(vatNum)
            val emailToSendRequest = EmailToSendRequest(List(validEmail), "oss_registration_confirmation", emailParams)

            when(connector.send(any())(any(), any())).thenReturn(Future.successful(EMAIL_ACCEPTED))

            emailService.sendConfirmationEmail(vatNum, validEmail).futureValue mustBe EMAIL_ACCEPTED

            verify(connector, times(1)).send(refEq(emailToSendRequest))(any(), any())
      }
    }
  }
}