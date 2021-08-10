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

import config.Constants.{registrationConfirmationPost10thTemplateId, registrationConfirmationTemplateId}
import connectors.EmailConnector
import models.emails.{EmailSendingResult, EmailToSendRequest, RegistrationConfirmationEmailPre10thParameters, RegistrationConfirmationEmailPost10thParameters}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailService@Inject()(
   emailConnector: EmailConnector,
   dateService: DateService
 )(implicit executionContext: ExecutionContext) {

  def sendConfirmationEmail(
   recipientName_line1: String,
   businessName: String,
   reference: String,
   commencementDate: LocalDate,
   emailAddress: String,
   startDate: Option[LocalDate]
  )(implicit hc: HeaderCarrier): Future[EmailSendingResult] = {

    val startDateEqualsCommencementDate = commencementDate == startDate.get
    val lastDayOfCalendarQuarter = dateService.lastDayOfCalendarQuarter
    val lastDayOfMonthAfterCalendarQuarter = dateService.lastDayOfMonthAfterCalendarQuarter
    val firstDayOfNextCalendarQuarter = dateService.startOfNextQuarter

    val emailParameters =
      if(startDateEqualsCommencementDate) {
        RegistrationConfirmationEmailPre10thParameters(
          recipientName_line1,
          businessName,
          format(commencementDate),
          reference,
          format(lastDayOfCalendarQuarter),
          format(lastDayOfMonthAfterCalendarQuarter)
        )} else {
        RegistrationConfirmationEmailPost10thParameters(
          recipientName_line1,
          businessName,
          format(commencementDate),
          reference,
          format(lastDayOfCalendarQuarter),
          format(lastDayOfMonthAfterCalendarQuarter),
          format(firstDayOfNextCalendarQuarter)
        )
      }

    emailConnector.send(
      EmailToSendRequest(
        List(emailAddress),
        if (startDateEqualsCommencementDate)
          registrationConfirmationTemplateId
        else
          registrationConfirmationPost10thTemplateId,
        emailParameters
      )
    )
  }

  private def format(date: LocalDate) = {
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
    date.format(formatter)
  }
}