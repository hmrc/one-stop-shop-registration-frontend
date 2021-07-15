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

import config.Constants.registrationConfirmationTemplateId
import connectors.EmailConnector
import models.emails.{EmailSendingResult, EmailToSendRequest, RegistrationConfirmationEmailParameters}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailService@Inject()(emailConnector: EmailConnector)(implicit executionContext: ExecutionContext) {

  def sendConfirmationEmail(
    recipientName_line1: String,
    businessName: String,
    reference: String,
    startDate: LocalDate,
    emailAddress: String,
    lastDayOfCalendarQuarter: LocalDate,
    lastDayOfMonthAfterCalendarQuarter: LocalDate
  )(implicit hc: HeaderCarrier): Future[EmailSendingResult] = {

    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
    val formattedStartDate = startDate.format(formatter)
    val formattedLastDateOfCalendarQuarter = lastDayOfCalendarQuarter.format(formatter)
    val formattedLastDayOfMonthAfterCalendarQuarter = lastDayOfMonthAfterCalendarQuarter.format(formatter)

    emailConnector.send(
      EmailToSendRequest(
        List(emailAddress),
        registrationConfirmationTemplateId,
        RegistrationConfirmationEmailParameters(
          recipientName_line1,
          businessName,
          formattedStartDate,
          reference,
          formattedLastDateOfCalendarQuarter,
          formattedLastDayOfMonthAfterCalendarQuarter
        )
      )
    )
  }
}