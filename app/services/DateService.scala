/*
 * Copyright 2021 HM Revenue & Customs
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

import config.Constants
import config.Constants.schemeStartDate

import java.time.{Clock, LocalDate}
import java.time.Month._
import javax.inject.Inject

class DateService @Inject()(clock: Clock) {

  def startOfNextPeriod: LocalDate = {
    val today                   = LocalDate.now(clock)
    val lastMonthOfPeriod       = (((today.getMonthValue - 1) / 3) + 1) * 3
    val dateInLastMonthOfPeriod = today.withMonth(lastMonthOfPeriod)
    val lastDayOfCurrentPeriod  = dateInLastMonthOfPeriod.withDayOfMonth(dateInLastMonthOfPeriod.lengthOfMonth)

    lastDayOfCurrentPeriod.plusDays(1)
  }

  def startDateBasedOnFirstSale(dateOfFirstSale: LocalDate): LocalDate = {
    val lastDayOfNotification = dateOfFirstSale.plusMonths(1).withDayOfMonth(10)
    if (lastDayOfNotification.isBefore(LocalDate.now(clock))) {
      startOfNextPeriod
    } else {
      dateOfFirstSale
    }
  }

  def earliestSaleAllowed: LocalDate = {
    val quarterStartMonths = Set(JANUARY, APRIL, JULY, OCTOBER)
    val today = LocalDate.now(clock)

    if (today.isBefore(schemeStartDate.plusMonths(1))) {
      schemeStartDate
    } else if (quarterStartMonths.contains(today.getMonth) && today.getDayOfMonth < 11) {
      today.minusMonths(1).withDayOfMonth(1)
    } else {
      startOfNextPeriod.minusMonths(3)
    }
  }
}