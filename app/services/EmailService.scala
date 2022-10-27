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

import config.Constants.registrationConfirmationTemplateId
import config.FrontendAppConfig
import connectors.EmailConnector
import models.emails.{EmailParameters, EmailSendingResult, EmailToSendRequest}
import play.api.i18n.Messages
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailService@Inject()(
   emailConnector: EmailConnector,
   periodService: PeriodService,
   frontendAppConfig: FrontendAppConfig
 )(implicit executionContext: ExecutionContext) {

  private val redirectLink = frontendAppConfig.ossCompleteReturnUrl

  def sendConfirmationEmail(
   businessName: String,
   commencementDate: LocalDate,
   emailAddress: String
  )(implicit hc: HeaderCarrier, messages: Messages): Future[EmailSendingResult] = {

    val periodOfFirstReturn = periodService.getFirstReturnPeriod(commencementDate)
    val nextPeriod = periodService.getNextPeriod(periodOfFirstReturn)
    val firstDayOfNextPeriod = nextPeriod.firstDay

    val emailParameters =
        EmailParameters(
          businessName,
          periodOfFirstReturn.displayShortText,
          format(firstDayOfNextPeriod),
          format(commencementDate),
          redirectLink
        )

    emailConnector.send(
      EmailToSendRequest(
        List(emailAddress),
          registrationConfirmationTemplateId,
        emailParameters
      )
    )
  }

  private def format(date: LocalDate) = {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    date.format(formatter)
  }
}