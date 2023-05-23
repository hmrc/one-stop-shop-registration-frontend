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

import config.Constants.schemeStartDate
import logging.Logging
import models.core.{Match, MatchType}
import models.requests.AuthenticatedDataRequest
import models.{PreviousScheme, UserAnswers}
import pages.{DateOfFirstSalePage, HasMadeSalesPage}
import queries.previousRegistration.AllPreviousRegistrationsQuery
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Month._
import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DateService @Inject()(
                             clock: Clock,
                             coreRegistrationValidationService: CoreRegistrationValidationService
                           ) extends Logging {

  def startOfNextQuarter: LocalDate = {
    val today                    = LocalDate.now(clock)
    val lastMonthOfQuarter       = (((today.getMonthValue - 1) / 3) + 1) * 3
    val dateInLastMonthOfQuarter = today.withMonth(lastMonthOfQuarter)
    val lastDayOfCurrentQuarter  = dateInLastMonthOfQuarter.withDayOfMonth(dateInLastMonthOfQuarter.lengthOfMonth)

    lastDayOfCurrentQuarter.plusDays(1)
  }

  def lastDayOfNextCalendarQuarter: LocalDate = {
    val lastMonthOfNextQuarter = startOfNextQuarter.plusMonths(3).minusDays(1)
    val lengthOfMonth = lastMonthOfNextQuarter.lengthOfMonth()

    lastMonthOfNextQuarter.withDayOfMonth(lengthOfMonth)
  }

  def lastDayOfMonthAfterNextCalendarQuarter: LocalDate = {
    val startOfQuarterAfterNextQuarter = startOfNextQuarter.plusMonths(3)
    val lengthOfMonth = startOfQuarterAfterNextQuarter.lengthOfMonth()

    startOfQuarterAfterNextQuarter.withDayOfMonth(lengthOfMonth)
  }

  private def startDateBasedOnFirstSale(dateOfFirstSale: LocalDate): LocalDate = {
    val lastDayOfNotification = dateOfFirstSale.plusMonths(1).withDayOfMonth(10)
    if (lastDayOfNotification.isBefore(LocalDate.now(clock))) {
      startOfNextQuarter
    } else {
      dateOfFirstSale
    }
  }

  def lastDayOfCalendarQuarter: LocalDate = {
    startOfNextQuarter.minusDays(1)
  }

  def startOfCurrentQuarter: LocalDate = {
    startOfNextQuarter.minusMonths(3)
  }

  def lastDayOfMonthAfterCalendarQuarter: LocalDate = {
    startOfNextQuarter.withDayOfMonth(startOfNextQuarter.lengthOfMonth)
  }

  def isDOFSDifferentToCommencementDate(dateOfFirstSale: Option[LocalDate], commencementDate: LocalDate): Boolean = {
    if(dateOfFirstSale.isDefined) dateOfFirstSale.get != commencementDate else false
  }

  def getVatReturnEndDate(commencementDate: LocalDate): LocalDate = {
    val lastMonthOfQuarter       = (((commencementDate.getMonthValue - 1) / 3) + 1) * 3
    val dateInLastMonthOfQuarter = commencementDate.withMonth(lastMonthOfQuarter)

    dateInLastMonthOfQuarter.withDayOfMonth(dateInLastMonthOfQuarter.lengthOfMonth)
  }

  def getVatReturnDeadline(vatReturnEndDate: LocalDate): LocalDate = {
    val startOfNextQuarterAfterEndDate = vatReturnEndDate.plusDays(1)

    startOfNextQuarterAfterEndDate.withDayOfMonth(startOfNextQuarterAfterEndDate.lengthOfMonth)
  }

  def earliestSaleAllowed: LocalDate = {
    val quarterStartMonths = Set(JANUARY, APRIL, JULY, OCTOBER)
    val today = LocalDate.now(clock)

    if (today.isBefore(schemeStartDate.plusMonths(1))) {
      schemeStartDate
    } else if (quarterStartMonths.contains(today.getMonth) && today.getDayOfMonth < 11) {
      today.minusMonths(1).withDayOfMonth(1)
    } else {
      startOfNextQuarter.minusMonths(3)
    }
  }

  private def isWithinLastDayOfRegistrationWhenTransferring(exclusionEffectiveDate: LocalDate): Boolean = {
    val lastDayOfRegistration = exclusionEffectiveDate.plusMonths(1).withDayOfMonth(10)
    LocalDate.now(clock).minusDays(1).isBefore(lastDayOfRegistration)
  }


  private def searchPreviousRegistrationSchemes(userAnswers: UserAnswers)
                                               (implicit hc: HeaderCarrier, ec: ExecutionContext, request: AuthenticatedDataRequest[_]): Future[List[Match]] = {
    val futureSeqAllMatches = userAnswers.get(AllPreviousRegistrationsQuery) match {
      case Some(allPreviousRegistrationDetails) =>

        val schemesToSearch = allPreviousRegistrationDetails.flatMap { countryPreviousRegistrations =>
          val countriesIdsToSearch = countryPreviousRegistrations.previousSchemesDetails.filter(_.previousScheme == PreviousScheme.OSSU)
          val idToSearch = countriesIdsToSearch.map(_.previousSchemeNumbers.previousSchemeNumber)
          idToSearch.map((countryPreviousRegistrations.previousEuCountry.code, _))
        }

        schemesToSearch.map { case (countryCode, searchId) =>
          coreRegistrationValidationService.searchScheme(
            searchId,
            PreviousScheme.OSSU,
            None,
            countryCode
          )

        }
      case None =>
        List.empty
    }

    Future.sequence(futureSeqAllMatches).map(_.flatten)
  }

  def calculateCommencementDate(userAnswers: UserAnswers)
                               (implicit ec: ExecutionContext, hc: HeaderCarrier, request: AuthenticatedDataRequest[_]): Future[LocalDate] = {

    for {
      allMatches <- searchPreviousRegistrationSchemes(userAnswers)
    } yield {
      val findTransferringMsid = allMatches.find(_.matchType == MatchType.TransferringMSID)
      println("test3")

      findTransferringMsid match {
        case Some(matchedTransferringMsid) =>
          println("test2")
          matchedTransferringMsid.exclusionEffectiveDate match {
            case Some(exclusionEffectiveDate) =>
              if (isWithinLastDayOfRegistrationWhenTransferring(exclusionEffectiveDate)) {
                exclusionEffectiveDate
              } else {
                getDateOfFirstSale(userAnswers)
              }
            case _ =>
              val exception = new IllegalStateException("Transferring MSID match didn't have an expected exclusion effective date")
              logger.error(exception.getMessage, exception)
              throw exception
          }

        case _ =>
          userAnswers.get(HasMadeSalesPage) match {
            case Some(true) =>
              println("test4")
              getDateOfFirstSale(userAnswers)
            case Some(false) =>
              println("test...")
              startOfNextQuarter
            case _ =>
              val exception = new IllegalStateException("Must answer Has Made Sales")
              logger.error(exception.getMessage, exception)
              throw exception
          }
      }
    }
  }

  private def getDateOfFirstSale(userAnswers: UserAnswers): LocalDate = {
    userAnswers.get(DateOfFirstSalePage) match {
      case Some(date) =>
        startDateBasedOnFirstSale(date)
      case _ =>
        val exception = new IllegalStateException("Must provide a Date of First Sale")
        logger.error(exception.getMessage, exception)
        throw exception
    }
  }

  def calculateFinalAmendmentDate(commencementDate: LocalDate): LocalDate = {
    val daysIntoReturnForAmendment = 10
    getVatReturnEndDate(commencementDate).plusDays(daysIntoReturnForAmendment)
  }
}
