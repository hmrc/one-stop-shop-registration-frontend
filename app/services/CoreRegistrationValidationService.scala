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

import connectors.ValidateCoreRegistrationConnector
import logging.Logging
import models.core.{CoreRegistrationRequest, Match, MatchType, SourceType}
import uk.gov.hmrc.domain.Vrn

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CoreRegistrationValidationService @Inject()(connector: ValidateCoreRegistrationConnector) extends Logging {

  def searchUkVrn(vrn: Vrn)(implicit ec: ExecutionContext): Future[Option[Match]] = {

    val coreRegistrationRequest = CoreRegistrationRequest(SourceType.VATNumber.toString, None, vrn.vrn, None, "GB")

    connector.validateCoreRegistration(coreRegistrationRequest).map {

      case Right(coreRegistrationResponse) if coreRegistrationResponse.matches.nonEmpty =>
        coreRegistrationResponse.matches.headOption

      case _ => None
    }
  }

  def searchEuTaxId(euTaxReference: String, countryCode: String)
                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Match]] = {

    val coreRegistrationRequest = CoreRegistrationRequest(SourceType.EUTraderId.toString, None, euTaxReference, None, countryCode)

    connector.validateCoreRegistration(coreRegistrationRequest).map {

      case Right(coreRegistrationResponse) => coreRegistrationResponse.matches.headOption

      case _ => None
    }
  }

  def searchEuVrn(euVrn: String, countryCode: String)
                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Match]] = {

    val coreRegistrationRequest = CoreRegistrationRequest(SourceType.EUVATNumber.toString, None, euVrn, None, countryCode)

    connector.validateCoreRegistration(coreRegistrationRequest).map {

      case Right(coreRegistrationResponse) => coreRegistrationResponse.matches.headOption

      case _ => None
    }
  }

  def isActiveTrader(activeMatch: Match): Boolean = {
    activeMatch.matchType == MatchType.FixedEstablishmentActiveNETP ||
      activeMatch.matchType == MatchType.TraderIdActiveNETP || activeMatch.matchType == MatchType.OtherMSNETPActiveNETP
  }

  def isExcludedTrader(activeMatch: Match): Boolean = {
    activeMatch.matchType == MatchType.FixedEstablishmentQuarantinedNETP ||
      activeMatch.matchType == MatchType.TraderIdQuarantinedNETP || activeMatch.matchType == MatchType.OtherMSNETPQuarantinedNETP
  }

}
