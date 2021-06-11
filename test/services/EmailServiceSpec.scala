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
import models.emails.{EmailTemplate, RegistrationConfirmationEmail, RegistrationConfirmationEmailParameters}
import org.mockito.BDDMockito.`given`
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.verify
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.libs.json.Format
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future.successful

class EmailServiceSpec extends SpecBase {

  private val connector = mock[EmailConnector]
  private val emailService = new EmailService(connector)
  private val template = mock[EmailTemplate]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "EmailService.sendConfirmationEmail" - {

    "should generate the correct email and call connector.generate with correct email and parameters" in {
      forAll(validVRNs, validEmails) {
        (vatNum: String, validEmail: String) =>
            val emailParams = RegistrationConfirmationEmailParameters(vatNum)
            val email = RegistrationConfirmationEmail(Seq(validEmail), emailParams)

            given(connector.generate(any[RegistrationConfirmationEmail])(any[HeaderCarrier], any[Format[RegistrationConfirmationEmailParameters]]))
              .willReturn(successful(template))

            emailService.sendConfirmationEmail(vatNum, validEmail).futureValue mustBe template

            verify(connector)
              .generate(refEq(email))(any[HeaderCarrier], any[Format[RegistrationConfirmationEmailParameters]])
      }
    }
  }
}