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

import models.PreviousScheme
import models.core.Match
import models.domain.{EuTaxRegistration, EuVatRegistration, RegistrationWithFixedEstablishment, RegistrationWithoutFixedEstablishmentWithTradeDetails, RegistrationWithoutTaxId}
import models.etmp.SchemeType.{OSSNonUnion, OSSUnion}
import models.etmp.{EtmpDisplayEuRegistrationDetails, EtmpPreviousEuRegistrationDetails}
import models.requests.AuthenticatedDataRequest
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

sealed trait PreviousValidationInvalidResult

case class InvalidActiveTrader(countryCode: String, memberState: String) extends PreviousValidationInvalidResult

case object InvalidQuarantinedTrader extends PreviousValidationInvalidResult

class EuRegistrationsValidationService @Inject()(coreRegistrationValidationService: CoreRegistrationValidationService) {

  def validateEuRegistrationDetails(euRegistrations: Seq[EuTaxRegistration])
                                   (implicit hc: HeaderCarrier,
                                    request: AuthenticatedDataRequest[_],
                                    ec: ExecutionContext): Future[Either[PreviousValidationInvalidResult, Boolean]] = {
    euRegistrations.toList match {
      case ::(currentDetails: EuTaxRegistration, remaining) =>
        lookupSingleEtmpDisplayEuRegistrationDetails(currentDetails).flatMap { maybeMatch: Option[Match] =>
          maybeMatch match {
            case Some(foundMatch) =>
              remapMatchToError(currentDetails.country.code, foundMatch) match {
                case Some(previousValidationInvalidResult) => Future.successful(Left(previousValidationInvalidResult))
                case _ => validateEuRegistrationDetails(remaining)
              }
            case _ =>
              validateEuRegistrationDetails(remaining)
          }
        }
      case Nil =>
        Future.successful(Right(true))
    }
  }

  private def remapMatchToError(countryCode: String, foundMatch: Match, isOss: Boolean = false): Option[PreviousValidationInvalidResult] = {
    if (foundMatch.matchType.isActiveTrader && !isOss) {
      Some(InvalidActiveTrader(countryCode = countryCode, memberState = foundMatch.memberState))
    }
    else if (foundMatch.matchType.isQuarantinedTrader) {
      Some(InvalidQuarantinedTrader)
    } else {
      None
    }
  }

  private def lookupSingleEtmpDisplayEuRegistrationDetails(euTaxRegistration: EuTaxRegistration)
                                                          (implicit hc: HeaderCarrier,
                                                           request: AuthenticatedDataRequest[_]): Future[Option[Match]] = {
    val countryCode: String = euTaxRegistration.country.code
    val failure = Future.failed(new RuntimeException(s"$euTaxRegistration has neither a vrn or taxIdentificationNumber"))

    euTaxRegistration match {
      case EuVatRegistration(_, vatNumber) =>
        coreRegistrationValidationService.searchEuVrn(vatNumber, countryCode, isOtherMS = false)
      case RegistrationWithFixedEstablishment(_, taxIdentifier, _) =>
        taxIdentifier.value match {
          case Some(taxId) => coreRegistrationValidationService.searchEuTaxId(taxId, countryCode)
          case None => failure
        }
      case RegistrationWithoutFixedEstablishmentWithTradeDetails(_, taxIdentifier, _) =>
        taxIdentifier.value match {
          case Some(taxId) => coreRegistrationValidationService.searchEuTaxId(taxId, countryCode)
          case None => failure
        }
      case RegistrationWithoutTaxId(_) => failure
    }
  }

  def validatePreviousEuRegistrationDetails(previousEURegistrationDetails: Seq[EtmpPreviousEuRegistrationDetails])
                                           (implicit hc: HeaderCarrier,
                                            request: AuthenticatedDataRequest[_],
                                            ec: ExecutionContext): Future[Either[PreviousValidationInvalidResult, Boolean]] = {

    previousEURegistrationDetails.toList match {
      case ::(currentPreviousEuRegistrationDetails, remaining) =>
        validateSingle(currentPreviousEuRegistrationDetails, remaining)

      case Nil =>
        Future.successful(Right(true))
    }
  }

  private def validateSingle(currentPreviousEuRegistrationDetails: EtmpPreviousEuRegistrationDetails,
                             next: List[EtmpPreviousEuRegistrationDetails])
                            (implicit hc: HeaderCarrier,
                             request: AuthenticatedDataRequest[_],
                             ec: ExecutionContext): Future[Either[PreviousValidationInvalidResult, Boolean]] = {

    val ossSchemaTypes = List(OSSNonUnion, OSSUnion)
    val isOss = ossSchemaTypes.contains(currentPreviousEuRegistrationDetails.schemeType)

    coreRegistrationValidationService.searchScheme(
      currentPreviousEuRegistrationDetails.registrationNumber,
      PreviousScheme.fromEmtpSchemeType(currentPreviousEuRegistrationDetails.schemeType),
      currentPreviousEuRegistrationDetails.intermediaryNumber,
      currentPreviousEuRegistrationDetails.issuedBy
    )(hc, request.toAuthenticatedOptionalDataRequest).flatMap {
      case Some(foundMatch) =>
        remapMatchToError(currentPreviousEuRegistrationDetails.issuedBy, foundMatch, isOss) match {
          case Some(previousValidationInvalidResult) =>
            Future.successful(Left(previousValidationInvalidResult))
          case None =>
            validatePreviousEuRegistrationDetails(next)
        }

      case _ =>
        validatePreviousEuRegistrationDetails(next)
    }
  }
}
