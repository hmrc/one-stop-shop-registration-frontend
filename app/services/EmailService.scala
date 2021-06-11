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

import connectors.EmailConnector
import models.emails.{EmailTemplate, RegistrationConfirmationEmail, RegistrationConfirmationEmailParameters}
import uk.gov.hmrc.http.HeaderCarrier
import utils.JsonFormatters.emailCompleteParamsFormat

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class EmailService@Inject() (connector: EmailConnector) {

  def sendConfirmationEmail(vrn: String, emailAddress: String)(implicit hc: HeaderCarrier): Future[EmailTemplate] = {
    val emailParams = RegistrationConfirmationEmailParameters(vrn)
    val email = RegistrationConfirmationEmail(Seq(emailAddress), emailParams)

    connector.generate(email)
  }
}