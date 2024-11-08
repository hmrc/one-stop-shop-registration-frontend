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

package services.ioss

import connectors.RegistrationConnector
import logging.Logging
import models.iossExclusions.{EtmpExclusion, EtmpExclusionReason}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IossExclusionService @Inject()(
                                      clock: Clock,
                                      registrationConnector: RegistrationConnector
                                    )(implicit ec: ExecutionContext) extends Logging {

  def isQuarantinedCode4()(implicit hc: HeaderCarrier): Future[Boolean] = {
    getIossEtmpExclusion().map {
      case Some(iossEtmpExclusion) =>
        !isAfterTwoYears(iossEtmpExclusion) &&
          iossEtmpExclusion.quarantine &&
          iossEtmpExclusion.exclusionReason.equals(EtmpExclusionReason.FailsToComply)
      case _ => false
    }
  }

  def getIossEtmpExclusion()(implicit hc: HeaderCarrier): Future[Option[EtmpExclusion]] = {
    registrationConnector.getIossRegistration.map {
      case Right(iossEtmpDisplayRegistration) =>
        iossEtmpDisplayRegistration.exclusions.headOption
      case Left(error) =>
        val exception = new Exception(s"An error occurred whilst retrieving the IOSS ETMP Display Registration with error: $error")
        logger.error(s"Unable to retrieve IOSS EtmpExclusion with error: ${exception.getMessage}", exception)
        throw exception
    }
  }

  private def isAfterTwoYears(etmpExclusion: EtmpExclusion): Boolean = {
    val currentDate: LocalDate = LocalDate.now(clock)
    val minimumDate: LocalDate = currentDate.minusYears(2)
    etmpExclusion.effectiveDate.isBefore(minimumDate)
  }
}
