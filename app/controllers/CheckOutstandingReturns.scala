/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers

import config.Constants.correctionsPeriodsLimit
import models.{CurrentReturns, SubmissionStatus}

import java.time.{Clock, LocalDate}

object CheckOutstandingReturns {

  def existsOutstandingReturns(currentReturns: CurrentReturns, clock: Clock): Boolean = {
    val existsOutstandingReturn = {
      if (currentReturns.finalReturnsCompleted) {
        false
      } else {
        currentReturns.returns.exists { currentReturn =>
          Seq(SubmissionStatus.Due, SubmissionStatus.Overdue, SubmissionStatus.Next).contains(currentReturn.submissionStatus) &&
            !isOlderThanThreeYears(currentReturn.dueDate, clock)
        }
      }
    }
    existsOutstandingReturn
  }

  private def isOlderThanThreeYears(dueDate: LocalDate, clock: Clock): Boolean = {
    val today: LocalDate = LocalDate.now(clock)
    today.isAfter(dueDate.plusYears(correctionsPeriodsLimit))
  }
}
