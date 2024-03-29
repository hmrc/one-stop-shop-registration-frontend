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
import connectors.ExternalEntryUrlHttpParser.{ExternalEntryUrlResponse, ExternalEntryUrlResponseReads}
import connectors.RegistrationHttpParser.{RegistrationResponseReads, RegistrationResultResponse}
import connectors.AmendRegistrationHttpParser.{AmendRegistrationResultResponse, AmendRegistrationResultResponseReads}
import connectors.VatCustomerInfoHttpParser.{VatCustomerInfoResponse, VatCustomerInfoResponseReads}
import models.domain.Registration
import play.api.Configuration
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpErrorFunctions, HttpResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationConnector @Inject()(config: Configuration, httpClient: HttpClient)
                                     (implicit ec: ExecutionContext) extends HttpErrorFunctions {

  private val baseUrl = config.get[Service]("microservice.services.one-stop-shop-registration")

  def submitRegistration(registration: Registration)(implicit hc: HeaderCarrier): Future[RegistrationResultResponse] = {
    val url = s"$baseUrl/create"

    httpClient.POST[Registration, RegistrationResultResponse](url, registration)
 }

  def getRegistration()(implicit hc: HeaderCarrier): Future[Option[Registration]] =
    httpClient.GET[Option[Registration]](s"$baseUrl/registration")

  def getVatCustomerInfo()(implicit hc: HeaderCarrier): Future[VatCustomerInfoResponse] =
    httpClient.GET[VatCustomerInfoResponse](s"$baseUrl/vat-information")

  def getSavedExternalEntry()(implicit hc: HeaderCarrier): Future[ExternalEntryUrlResponse] = {
    httpClient.GET[ExternalEntryUrlResponse](s"$baseUrl/external-entry")
  }

  def amendRegistration(registration: Registration)(implicit hc: HeaderCarrier): Future[AmendRegistrationResultResponse] = {
    httpClient.POST[Registration, AmendRegistrationResultResponse](s"$baseUrl/amend", registration)
  }

  def enrolUser()(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpClient.POSTEmpty[HttpResponse](s"$baseUrl/confirm-enrolment")

}
