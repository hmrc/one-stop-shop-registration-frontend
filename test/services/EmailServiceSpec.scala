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

package services

import base.SpecBase
import config.FrontendAppConfig
import connectors.EmailConnector
import models.Quarter.{Q1, Q4}
import models.emails.EmailSendingResult.EMAIL_ACCEPTED
import models.emails.{AmendRegistrationConfirmation, EmailToSendRequest, RegistrationConfirmation}
import models.{AmendMode, NormalMode, Period}
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import play.api.i18n.Messages
import play.api.test.Helpers
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmailServiceSpec extends SpecBase with BeforeAndAfterEach {

  implicit val messages: Messages = Helpers.stubMessages(
    Helpers.stubMessagesApi(Map("en" -> Map("site.to" -> "to"))))
  private val config = mock[FrontendAppConfig]
    when(config.ossCompleteReturnUrl) thenReturn("url")
  private val connector = mock[EmailConnector]
  private val periodService = mock[PeriodService]
  private val emailService = new EmailService(connector, periodService, config, stubClockAtArbitraryDate)
  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "EmailService.sendConfirmationEmail" - {

    "call sendConfirmationEmail with oss_registration_confirmation with the correct parameters" in {
      val maxLengthContactName = 105
      val maxLengthBusiness = 160
      val commencementDate = LocalDate.of(2022, 10, 1)
      val firstDayOfNextPeriod = LocalDate.of(2023, 1, 1)
      val redirectLink = config.ossCompleteReturnUrl

      forAll(
        validEmails,
        safeInputsWithMaxLength(maxLengthContactName),
        safeInputsWithMaxLength(maxLengthBusiness)
      ) {
        (email: String, businessName: String, contactName: String) =>

          val expectedCommencementDate = commencementDate.format(formatter)

          val formattedFirstDayOfNextPeriod = firstDayOfNextPeriod.format(formatter)

          val expectedEmailToSendRequest = EmailToSendRequest(
            List(email),
            "oss_registration_confirmation",
            RegistrationConfirmation(
              contactName,
              businessName,
              "October to December 2022",
              formattedFirstDayOfNextPeriod,
              expectedCommencementDate,
              redirectLink
            )
          )

          when(connector.send(any())(any(), any())).thenReturn(Future.successful(EMAIL_ACCEPTED))
          when(periodService.getFirstReturnPeriod(any())) thenReturn Period(2022, Q4)
          when(periodService.getNextPeriod(any())) thenReturn Period(2023, Q1)

          emailService.sendConfirmationEmail(
            contactName,
            businessName,
            commencementDate,
            email,
            NormalMode
          ).futureValue mustBe EMAIL_ACCEPTED
          verify(connector, times(1)).send(refEq(expectedEmailToSendRequest))(any(), any())
      }
    }

    "call sendConfirmationEmail with oss_registration_amendment_confirmation with the correct parameters" in {
      val maxLengthContactName = 105
      val maxLengthBusiness = 160
      val commencementDate = LocalDate.of(2022, 10, 1)

      forAll(
        validEmails,
        safeInputsWithMaxLength(maxLengthContactName),
        safeInputsWithMaxLength(maxLengthBusiness)
      ) {
        (email: String, businessName: String, contactName: String) =>

          val expectedAmendmentDateDate = LocalDate.now(stubClockAtArbitraryDate).format(formatter)

          val expectedEmailToSendRequest = EmailToSendRequest(
            List(email),
            "oss_registration_amendment_confirmation",
            AmendRegistrationConfirmation(
              contactName,
              expectedAmendmentDateDate
            )
          )

          when(connector.send(any())(any(), any())).thenReturn(Future.successful(EMAIL_ACCEPTED))

          emailService.sendConfirmationEmail(
            contactName,
            businessName,
            commencementDate,
            email,
            AmendMode
          ).futureValue mustBe EMAIL_ACCEPTED
          verify(connector, times(1)).send(refEq(expectedEmailToSendRequest))(any(), any())
      }
    }

  }
}