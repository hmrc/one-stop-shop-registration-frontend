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

package services

import models.domain.*
import models.requests.AuthenticatedMandatoryDataRequest
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RejoinEuRegistrationValidationService @Inject()(
                                                       coreRegistrationValidationService: CoreRegistrationValidationService,
                                                       clock: Clock
                                                     )(implicit ec: ExecutionContext) {

  def validateEuRegistrations(euRegistrations: Seq[EuTaxRegistration])
                             (implicit hc: HeaderCarrier,
                              request: AuthenticatedMandatoryDataRequest[_]): Future[Option[Result]] = {
    euRegistrations match {
      case head +: tail => validateSingleEuTaxRegistration(head).flatMap {
        case None => validateEuRegistrations(tail)
        case result => Future.successful(result)
      }
      case _ => Future.successful(None)
    }
  }

  private def validateSingleEuTaxRegistration(euTaxRegistration: EuTaxRegistration)
                                             (implicit hc: HeaderCarrier,
                                              request: AuthenticatedMandatoryDataRequest[_]): Future[Option[Result]] = {
    val countryCode: String = euTaxRegistration.country.code
    val failure = Future.failed(new RuntimeException(s"$euTaxRegistration has neither a vrn or taxIdentificationNumber"))

    euTaxRegistration match {
      case EuVatRegistration(_, vatNumber) =>
        coreRegistrationValidationService.searchEuVrn(vatNumber, countryCode, isOtherMS = false).map(m => RejoinRedirectService.redirectOnMatch(m, clock))
      case RegistrationWithFixedEstablishment(_, taxIdentifier, _) =>
        taxIdentifier.value match {
          case Some(taxId) => coreRegistrationValidationService.searchEuTaxId(taxId, countryCode).map(m => RejoinRedirectService.redirectOnMatch(m, clock))
          case None => failure
        }
      case RegistrationWithoutFixedEstablishmentWithTradeDetails(_, taxIdentifier, _) =>
        taxIdentifier.value match {
          case Some(taxId) => coreRegistrationValidationService.searchEuTaxId(taxId, countryCode).map(m => RejoinRedirectService.redirectOnMatch(m, clock))
          case None => failure
        }
      case RegistrationWithoutTaxId(_) => failure
    }
  }
}
