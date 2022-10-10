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
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CoreRegistrationValidationService @Inject()(connector: ValidateCoreRegistrationConnector) extends Logging {

  def searchUkVrn(vrn: Vrn)
                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Match]] = {

    val coreRegistrationRequest = CoreRegistrationRequest(SourceType.VATNumber.toString, None, vrn.vrn, None, "GB")
    connector.validateCoreRegistration(coreRegistrationRequest).map {

      case Right(coreRegistrationResponse) if coreRegistrationResponse.matches.exists(_.matchType == MatchType.FixedEstablishmentActiveNETP) =>
        coreRegistrationResponse.matches.find(_.matchType == MatchType.FixedEstablishmentActiveNETP) match {

          case Some(activeMatch) if activeMatch.exclusionStatusCode.contains(-1) || activeMatch.exclusionStatusCode.contains(6) =>
            None

          case Some(activeMatch) => Some(activeMatch)

          case None => None
        }

      case Right(coreRegistrationResponse) if coreRegistrationResponse.matches.nonEmpty =>
        coreRegistrationResponse.matches.headOption

      case _ => None
    }
  }

}
