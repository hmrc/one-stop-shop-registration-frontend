/*
 * Copyright 2023 HM Revenue & Customs
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

import connectors.ValidateCoreRegistrationConnector
import logging.Logging
import models.{CountryWithValidationDetails, PreviousScheme}
import models.audit.CoreRegistrationAuditModel
import models.core.{CoreRegistrationRequest, Match, MatchType, SourceType}
import models.requests.AuthenticatedDataRequest
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CoreRegistrationValidationService @Inject()(
                                                   connector: ValidateCoreRegistrationConnector,
                                                   auditService: AuditService,
                                                 )
                                                 (implicit ec: ExecutionContext) extends Logging {

  def searchUkVrn(vrn: Vrn)(implicit hc: HeaderCarrier,
                            request: AuthenticatedDataRequest[_]): Future[Option[Match]] = {

    val coreRegistrationRequest = CoreRegistrationRequest(SourceType.VATNumber.toString, None, vrn.vrn, None, "GB")

    connector.validateCoreRegistration(coreRegistrationRequest).map {

      case Right(coreRegistrationResponse) =>
        auditService.audit(CoreRegistrationAuditModel.build(coreRegistrationRequest, coreRegistrationResponse))
        coreRegistrationResponse.matches.headOption

      case _ => throw CoreRegistrationValidationException("Error while validating core registration")

    }
  }

  def searchEuTaxId(euTaxReference: String, countryCode: String)(implicit hc: HeaderCarrier,
                                                                 request: AuthenticatedDataRequest[_]): Future[Option[Match]] = {

    val coreRegistrationRequest = CoreRegistrationRequest(SourceType.EUTraderId.toString, None, euTaxReference, None, countryCode)

    connector.validateCoreRegistration(coreRegistrationRequest).map {

      case Right(coreRegistrationResponse) =>
        auditService.audit(CoreRegistrationAuditModel.build(coreRegistrationRequest, coreRegistrationResponse))
        coreRegistrationResponse.matches.headOption

      case _ => throw CoreRegistrationValidationException("Error while validating core registration")
    }
  }

  def searchEuVrn(euVrn: String, countryCode: String)(implicit hc: HeaderCarrier,
                                                      request: AuthenticatedDataRequest[_]): Future[Option[Match]] = {

    val convertedEuVrn = convertTaxIdentifierForTransfer(euVrn, countryCode)
    val coreRegistrationRequest = CoreRegistrationRequest(SourceType.EUVATNumber.toString, None, convertedEuVrn, None, countryCode)

    connector.validateCoreRegistration(coreRegistrationRequest).map {

      case Right(coreRegistrationResponse) =>
        auditService.audit(CoreRegistrationAuditModel.build(coreRegistrationRequest, coreRegistrationResponse))
        coreRegistrationResponse.matches.headOption

      case _ => throw CoreRegistrationValidationException("Error while validating core registration")
    }
  }

  def searchScheme(searchNumber: String, previousScheme: PreviousScheme, intermediaryNumber: Option[String], countryCode: String)
                  (implicit hc: HeaderCarrier, request: AuthenticatedDataRequest[_]): Future[Option[Match]] = {

    if (previousScheme == PreviousScheme.OSSNU) {
      Future.successful(None)
    } else {

      val sourceType = previousScheme match {
        case PreviousScheme.OSSU => SourceType.EUVATNumber
        case PreviousScheme.OSSNU => SourceType.EUVATNumber
        case PreviousScheme.IOSSWOI => SourceType.TraderId
        case PreviousScheme.IOSSWI => SourceType.TraderId
      }

      val convertedSearchNumber = if (sourceType == SourceType.EUVATNumber) {
        convertTaxIdentifierForTransfer(searchNumber, countryCode)
      } else {
        searchNumber
      }

      val coreRegistrationRequest = CoreRegistrationRequest(
        sourceType.toString,
        Some(convertScheme(previousScheme)),
        convertedSearchNumber,
        intermediaryNumber,
        countryCode
      )

      connector.validateCoreRegistration(coreRegistrationRequest).map {
        case Right(coreRegistrationResponse) =>
          auditService.audit(CoreRegistrationAuditModel.build(coreRegistrationRequest, coreRegistrationResponse))
          coreRegistrationResponse.matches.headOption
        case _ => throw CoreRegistrationValidationException("Error while validating core registration")
      }
    }
  }

  def isActiveTrader(activeMatch: Match): Boolean = {
    activeMatch.matchType == MatchType.FixedEstablishmentActiveNETP ||
      activeMatch.matchType == MatchType.TraderIdActiveNETP || activeMatch.matchType == MatchType.OtherMSNETPActiveNETP
  }

  def isQuarantinedTrader(activeMatch: Match): Boolean = {
    activeMatch.matchType == MatchType.FixedEstablishmentQuarantinedNETP ||
      activeMatch.matchType == MatchType.TraderIdQuarantinedNETP || activeMatch.matchType == MatchType.OtherMSNETPQuarantinedNETP
  }

  private def convertScheme(previousScheme: PreviousScheme): String = {
    previousScheme match {
      case PreviousScheme.OSSU => "OSS"
      case PreviousScheme.OSSNU => "OSS"
      case PreviousScheme.IOSSWOI => "IOSS"
      case PreviousScheme.IOSSWI => "IOSS"
    }
  }

  private def convertTaxIdentifierForTransfer(identifier: String, countryCode: String): String = {

    CountryWithValidationDetails.euCountriesWithVRNValidationRules.find(_.country.code == countryCode) match {
      case Some(countryValidationDetails) =>
        if (identifier.matches(countryValidationDetails.vrnRegex)) {
          identifier.substring(2)
        } else {
          identifier
        }

      case _ =>
        logger.error("Error occurred while getting country code regex, unable to convert identifier")
        throw new IllegalStateException("Error occurred while getting country code regex, unable to convert identifier")
    }
  }
}
case class CoreRegistrationValidationException(message: String) extends Exception(message)
