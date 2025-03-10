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
