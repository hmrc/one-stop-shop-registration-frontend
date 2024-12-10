/*
 * Copyright 2024 HM Revenue & Customs
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

import config.Service
import connectors.AmendRegistrationHttpParser.{AmendRegistrationResultResponse, AmendRegistrationResultResponseReads}
import connectors.ExternalEntryUrlHttpParser.{ExternalEntryUrlResponse, ExternalEntryUrlResponseReads}
import connectors.RegistrationHttpParser.{IossEtmpDisplayRegistrationReads, IossEtmpDisplayRegistrationResultResponse, RegistrationResponseReads, RegistrationResultResponse}
import connectors.VatCustomerInfoHttpParser.{VatCustomerInfoResponse, VatCustomerInfoResponseReads}
import models.domain.Registration
import play.api.Configuration
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, HttpResponse, StringContextOps}
import play.api.libs.ws.writeableOf_JsValue

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationConnector @Inject()(config: Configuration, httpClientV2: HttpClientV2)
                                     (implicit ec: ExecutionContext) extends HttpErrorFunctions {

  private val baseUrl = config.get[Service]("microservice.services.one-stop-shop-registration")

  def submitRegistration(registration: Registration)(implicit hc: HeaderCarrier): Future[RegistrationResultResponse] =
    httpClientV2.post(url"$baseUrl/create").withBody(Json.toJson(registration)).execute[RegistrationResultResponse]

  def getRegistration()(implicit hc: HeaderCarrier): Future[Option[Registration]] =
    httpClientV2.get(url"$baseUrl/registration").execute[Option[Registration]]

  def getVatCustomerInfo()(implicit hc: HeaderCarrier): Future[VatCustomerInfoResponse] =
    httpClientV2.get(url"$baseUrl/vat-information").execute[VatCustomerInfoResponse]

  def getSavedExternalEntry()(implicit hc: HeaderCarrier): Future[ExternalEntryUrlResponse] =
    httpClientV2.get(url"$baseUrl/external-entry").execute[ExternalEntryUrlResponse]

  def amendRegistration(registration: Registration)(implicit hc: HeaderCarrier): Future[AmendRegistrationResultResponse] =
    httpClientV2.post(url"$baseUrl/amend").withBody(Json.toJson(registration)).execute[AmendRegistrationResultResponse]

  def enrolUser()(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClientV2.post(url"$baseUrl/confirm-enrolment").execute[HttpResponse]

  def getIossRegistration()(implicit hc: HeaderCarrier): Future[IossEtmpDisplayRegistrationResultResponse] = {
    val baseUrl: Service = config.get[Service]("microservice.services.ioss-registration")

    httpClientV2.get(url"$baseUrl/registration").execute[IossEtmpDisplayRegistrationResultResponse]
  }
}
