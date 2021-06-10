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

package connectors

import com.google.inject.Inject
import config.Service
import models.emails.{Email, EmailTemplate}
import play.api.libs.json.Format
import utils.Base64Utils
import uk.gov.hmrc.http.HttpReadsInstances.readFromJson
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.JsonFormatters.emailTemplateFormat
import play.api.Configuration

import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailConnector @Inject()(
  config: Configuration,
  client: HttpClient
)(implicit ec: ExecutionContext) {

  private val baseUrl = config.get[Service]("microservice.services.hmrc-email-renderer")

  def generate[T](e: Email[T])(implicit hc: HeaderCarrier, writes: Format[T]): Future[EmailTemplate] = {
    val url = s"$baseUrl/templates/${e.templateId}"

    client.POST[Map[String, T], EmailTemplate](url, Map("parameters" -> e.parameters)).map(decodingContent)
  }

  private def decodingContent: EmailTemplate => EmailTemplate = { t: EmailTemplate =>
    t.copy(
      plain = Base64Utils.decode(t.plain),
      html  = Base64Utils.decode(t.html)
    )
  }
}
