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

import models.exclusions.{ExcludedTrader, ExclusionReason}

import java.time.LocalDate
import javax.inject.Inject
import scala.math.Ordered.orderingToOrdered

class RejoinRegistrationService @Inject() {

  def canRejoinRegistration(currentDate: LocalDate, exclusions: Option[ExcludedTrader]): Boolean = {
    exclusions match {
      case Some(etmpExclusion) if etmpExclusion.exclusionReason == ExclusionReason.Reversal => false
      case Some(etmpExclusion) if isQuarantinedAndAfterTwoYears(currentDate, etmpExclusion) => true
      case Some(etmpExclusion) if notQuarantinedAndAfterEffectiveDate(currentDate, etmpExclusion) => true
      case _ => false
    }
  }

  private def isQuarantinedAndAfterTwoYears(currentDate: LocalDate, etmpExclusion: ExcludedTrader): Boolean = {
    if (etmpExclusion.quarantined) {
      val minimumDate = currentDate.minusYears(2)
      etmpExclusion.effectiveDate.isBefore(minimumDate)
    } else {
      false
    }
  }

  private def notQuarantinedAndAfterEffectiveDate(currentDate: LocalDate, etmpExclusion: ExcludedTrader): Boolean = {
    if (!etmpExclusion.quarantined) {
      etmpExclusion.effectiveDate <= currentDate
    } else {
      false
    }
  }
}
