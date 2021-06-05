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

import models.domain.VatDetailSource.{Etmp, Mixed, UserEntered}
import models.{Address, UserAnswers}
import models.domain.{EuVatRegistration, FixedEstablishment, PreviousRegistration, Registration, VatDetailSource, VatDetails}
import pages._
import queries.{AllEuDetailsQuery, AllPreviousRegistrationsQuery, AllTradingNames, AllWebsites}
import uk.gov.hmrc.domain.Vrn

import java.time.LocalDate

class RegistrationService {

  def fromUserAnswers(userAnswers: UserAnswers, vrn: Vrn): Option[Registration] =
    for {
      registeredCompanyName        <- getName(userAnswers)
      tradingNames                 = getTradingNames(userAnswers)
      vatDetails                   <- buildVatDetails(userAnswers)
      euVatRegistrations           = buildEuVatRegistrations(userAnswers)
      startDate                    <- userAnswers.get(StartDatePage)
      businessContactDetails       <- userAnswers.get(BusinessContactDetailsPage)
      websites                     <- userAnswers.get(AllWebsites)
      currentCountryOfRegistration = userAnswers.get(CurrentCountryOfRegistrationPage)
      previousRegistrations        = buildPreviousRegistrations(userAnswers)
    } yield Registration(
      vrn,
      registeredCompanyName,
      tradingNames,
      vatDetails,
      euVatRegistrations,
      businessContactDetails,
      websites,
      startDate.date,
      currentCountryOfRegistration,
      previousRegistrations
    )

  private def getTradingNames(userAnswers: UserAnswers): List[String] =
    userAnswers.get(AllTradingNames).getOrElse(List.empty)

  private def buildEuVatRegistrations(answers: UserAnswers): List[EuVatRegistration] =
    answers
      .get(AllEuDetailsQuery).getOrElse(List.empty)
      .map {
        detail =>
          val fixedEstablishment = (detail.fixedEstablishmentTradingName, detail.fixedEstablishmentAddress) match {
            case (Some(tradingName), Some(address)) =>
              Some(FixedEstablishment(tradingName, address))
            case _ => None
          }

          EuVatRegistration(detail.euCountry, detail.euVatNumber, fixedEstablishment)
    }

  private def buildPreviousRegistrations(answers: UserAnswers): List[PreviousRegistration] =
    answers
      .get(AllPreviousRegistrationsQuery)
      .getOrElse(List.empty)
      .map {
        detail =>
          PreviousRegistration(detail.previousEuCountry, detail.previousEuVatNumber)
      }

  private def buildVatDetails(answers: UserAnswers): Option[VatDetails] =
    for {
      address          <- getAddress(answers)
      registrationDate <- getRegistrationDate(answers)
      partOfVatGroup   <- getPartOfVatGroup(answers)
      source           = getVatDetailSource(answers)
    } yield VatDetails(registrationDate, address, partOfVatGroup, source)

  private def getPartOfVatGroup(answers: UserAnswers): Option[Boolean] =
    answers.vatInfo match {
      case Some(vatInfo) =>
        vatInfo.partOfVatGroup orElse answers.get(PartOfVatGroupPage)
      case None =>
        answers.get(PartOfVatGroupPage)
    }

  private def getRegistrationDate(answers: UserAnswers): Option[LocalDate] =
    answers.vatInfo match {
      case Some(vatInfo) =>
        vatInfo.registrationDate orElse answers.get(UkVatEffectiveDatePage)
      case None =>
        answers.get(UkVatEffectiveDatePage)
    }

  private def getAddress(answers: UserAnswers): Option[Address] =
    answers.vatInfo match {
      case Some(vatInfo) =>
        Some(vatInfo.address)
      case None =>
        answers.get(BusinessAddressPage)
    }

  private def getName(answers: UserAnswers): Option[String] =
    answers.vatInfo match {
      case Some(vatInfo) =>
        vatInfo.organisationName orElse answers.get(RegisteredCompanyNamePage)
      case None =>
        answers.get(RegisteredCompanyNamePage)
    }

  private def getVatDetailSource(answers: UserAnswers): VatDetailSource =
    answers.vatInfo match {
      case Some(vatInfo) if vatInfo.registrationDate.isDefined & vatInfo.partOfVatGroup.isDefined & vatInfo.organisationName.isDefined =>
        Etmp
      case Some(_) =>
        Mixed
      case None =>
        UserEntered
    }
}
