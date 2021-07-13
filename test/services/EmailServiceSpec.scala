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

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmailServiceSpec extends SpecBase {

  private val connector = mock[EmailConnector]
  private val emailService = new EmailService(connector)
  private val dateService = new DateService(stubClockAtArbitraryDate)
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "EmailService.sendConfirmationEmail" - {

    "call sendConfirmationEmail with the correct parameters" in {

      val maxLengthBusiness = 160
      val maxLengthContactName = 105
      val validStartDate = LocalDate.of(2010, 1, 1)
      val validEndDate = LocalDate.now()
      val lastDayOfCalendarQuarter = dateService.startOfNextPeriod.minusDays(1)
      val lastDayOfMonthAfterCalendarQuarter = dateService.startOfNextPeriod.withDayOfMonth(dateService.startOfNextPeriod.lengthOfMonth)

      forAll(
        validVRNs,
        validEmails,
        safeInputsWithMaxLength(maxLengthBusiness),
        safeInputsWithMaxLength(maxLengthContactName),
        datesBetween(validStartDate, validEndDate),
      ) {
        (vatNum: String, email: String, businessName: String, contactName: String, startDate: LocalDate) =>
          val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
          val expectedDate = startDate.format(formatter)
          val formattedLastDateOfCalendarQuarter = lastDayOfCalendarQuarter.format(formatter)
          val formattedLastDayOfMonthAfterCalendarQuarter = lastDayOfMonthAfterCalendarQuarter.format(formatter)

          val expectedEmailToSendRequest = EmailToSendRequest(
            List(email),
            "oss_registration_confirmation",
            RegistrationConfirmationEmailParameters(
              contactName,
              businessName,
              expectedDate,
              vatNum,
              formattedLastDateOfCalendarQuarter,
              formattedLastDayOfMonthAfterCalendarQuarter)
          )

          when(connector.send(any())(any(), any())).thenReturn(Future.successful(EMAIL_ACCEPTED))

          emailService.sendConfirmationEmail(
            contactName, businessName, vatNum, startDate, email, lastDayOfCalendarQuarter, lastDayOfMonthAfterCalendarQuarter
          ).futureValue mustBe EMAIL_ACCEPTED

          verify(connector, times(1)).send(refEq(expectedEmailToSendRequest))(any(), any())
      }
    }
  }
}