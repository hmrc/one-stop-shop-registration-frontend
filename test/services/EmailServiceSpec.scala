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
import connectors.EmailConnector
import models.emails.EmailSendingResult.EMAIL_ACCEPTED
import models.emails.{EmailToSendRequest, RegistrationConfirmationEmailPre10thParameters, RegistrationConfirmationEmailPost10thParameters}
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
  private val dateService = new DateService(stubClockAtArbitraryDate)
  private val emailService = new EmailService(connector, dateService)
  private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "EmailService.sendConfirmationEmail" - {

    "call sendConfirmationEmail with oss_registration_confirmation_pre_10th_of_month with the correct parameters" in {
      val maxLengthBusiness = 160
      val maxLengthContactName = 105
      val commencementDate = LocalDate.of(2010, 1, 1)
      val lastDayOfCalendarQuarterForPeriod = dateService.getVatReturnEndDate(commencementDate)
      val lastDayOfMonthAfterCalendarQuarterForPeriod = dateService.getVatReturnDeadline(lastDayOfCalendarQuarterForPeriod)
      val startDate = commencementDate

      forAll(
        validEmails,
        safeInputsWithMaxLength(maxLengthBusiness),
        safeInputsWithMaxLength(maxLengthContactName),
      ) {
        (email: String, businessName: String, contactName: String) =>
          val expectedDate = commencementDate.format(formatter)
          val formattedLastDayOfCalendarQuarterForPeriod = lastDayOfCalendarQuarterForPeriod.format(formatter)
          val formattedLastDayOfMonthAfterCalendarQuarterForPeriod = lastDayOfMonthAfterCalendarQuarterForPeriod.format(formatter)

          val expectedEmailToSendRequest = EmailToSendRequest(
            List(email),
            "oss_registration_confirmation_pre_10th_of_month",
            RegistrationConfirmationEmailPre10thParameters(
              contactName,
              businessName,
              expectedDate,
              formattedLastDayOfCalendarQuarterForPeriod,
              formattedLastDayOfMonthAfterCalendarQuarterForPeriod)
          )

          when(connector.send(any())(any(), any())).thenReturn(Future.successful(EMAIL_ACCEPTED))

          emailService.sendConfirmationEmail(
            contactName,
            businessName,
            commencementDate,
            email,
            Some(startDate)
          ).futureValue mustBe EMAIL_ACCEPTED

          verify(connector, times(1)).send(refEq(expectedEmailToSendRequest))(any(), any())
      }
    }

    "call sendConfirmationEmail with oss_registration_confirmation_post_10th_of_month with the correct parameters" in {

      val maxLengthBusiness = 160
      val maxLengthContactName = 105
      val commencementDate = LocalDate.of(2010, 1, 1)
      val lastDayOfCalendarQuarter = dateService.lastDayOfCalendarQuarter
      val firstDayOfNextCalendarQuarter = dateService.startOfNextQuarter
      val lastDayOfMonthAfterNextCalendarQuarter = dateService.lastDayOfMonthAfterNextCalendarQuarter
      val lastDayOfNextCalendarQuarter = dateService.lastDayOfNextCalendarQuarter
      val startDate = commencementDate.plusDays(1)

      forAll(
        validEmails,
        safeInputsWithMaxLength(maxLengthBusiness),
        safeInputsWithMaxLength(maxLengthContactName),
      ) {
        (email: String, businessName: String, contactName: String) =>
          val expectedDate = commencementDate.format(formatter)
          val formattedLastDateOfCalendarQuarter = lastDayOfCalendarQuarter.format(formatter)
          val formattedLastDayOfMonthAfterCalendarQuarter = lastDayOfNextCalendarQuarter.format(formatter)
          val formattedFirstDayOfNextCalendarQuarter = firstDayOfNextCalendarQuarter.format(formatter)
          val formattedLastDayOfMonthAfterNextCalendarQuarter = lastDayOfMonthAfterNextCalendarQuarter.format(formatter)

          val expectedEmailToSendRequest = EmailToSendRequest(
            List(email),
            "oss_registration_confirmation_post_10th_of_month",
            RegistrationConfirmationEmailPost10thParameters(
              contactName,
              businessName,
              expectedDate,
              formattedLastDateOfCalendarQuarter,
              formattedLastDayOfMonthAfterNextCalendarQuarter,
              formattedFirstDayOfNextCalendarQuarter,
              formattedLastDayOfMonthAfterCalendarQuarter)
          )

          when(connector.send(any())(any(), any())).thenReturn(Future.successful(EMAIL_ACCEPTED))

          emailService.sendConfirmationEmail(
            contactName,
            businessName,
            commencementDate,
            email,
            Some(startDate)
          ).futureValue mustBe EMAIL_ACCEPTED

          verify(connector, times(1)).send(refEq(expectedEmailToSendRequest))(any(), any())
      }
    }

    "call sendConfirmationEmail with oss_registration_confirmation_pre_10th_of_month with the correct parameters when no date of first sale" in {
      val maxLengthBusiness = 160
      val maxLengthContactName = 105
      val commencementDate = LocalDate.of(2010, 1, 1)
      val lastDayOfCalendarQuarterForPeriod = dateService.getVatReturnEndDate(commencementDate)
      val lastDayOfMonthAfterCalendarQuarterForPeriod = dateService.getVatReturnDeadline(lastDayOfCalendarQuarterForPeriod)

      forAll(
        validEmails,
        safeInputsWithMaxLength(maxLengthBusiness),
        safeInputsWithMaxLength(maxLengthContactName),
      ) {
        (email: String, businessName: String, contactName: String) =>
          val expectedDate = commencementDate.format(formatter)
          val formattedLastDayOfCalendarQuarterForPeriod = lastDayOfCalendarQuarterForPeriod.format(formatter)
          val formattedLastDayOfMonthAfterCalendarQuarterForPeriod = lastDayOfMonthAfterCalendarQuarterForPeriod.format(formatter)

          val expectedEmailToSendRequest = EmailToSendRequest(
            List(email),
            "oss_registration_confirmation_pre_10th_of_month",
            RegistrationConfirmationEmailPre10thParameters(
              contactName,
              businessName,
              expectedDate,
              formattedLastDayOfCalendarQuarterForPeriod,
              formattedLastDayOfMonthAfterCalendarQuarterForPeriod)
          )

          when(connector.send(any())(any(), any())).thenReturn(Future.successful(EMAIL_ACCEPTED))

          emailService.sendConfirmationEmail(
            contactName,
            businessName,
            commencementDate,
            email,
            None
          ).futureValue mustBe EMAIL_ACCEPTED

          verify(connector, times(1)).send(refEq(expectedEmailToSendRequest))(any(), any())
      }
    }
  }
}