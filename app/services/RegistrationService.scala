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
import models.domain._
import models.euDetails.EuDetails
import models.{Address, UserAnswers}
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
      euRegistrations              = buildEuRegistrations(userAnswers)
      startDate                    <- userAnswers.get(StartDatePage)
      businessContactDetails       <- userAnswers.get(BusinessContactDetailsPage)
      websites                     = getWebsites(userAnswers)
      currentCountryOfRegistration = userAnswers.get(CurrentCountryOfRegistrationPage)
      previousRegistrations        = buildPreviousRegistrations(userAnswers)
      bankDetails                  <- userAnswers.get(BankDetailsPage)
    } yield Registration(
      vrn,
      registeredCompanyName,
      tradingNames,
      vatDetails,
      euRegistrations,
      businessContactDetails,
      websites,
      startDate.date,
      currentCountryOfRegistration,
      previousRegistrations,
      bankDetails
    )

  private def getTradingNames(userAnswers: UserAnswers): List[String] =
    userAnswers.get(AllTradingNames).getOrElse(List.empty)

  private def getWebsites(userAnswers: UserAnswers): List[String] =
    userAnswers.get(AllWebsites).getOrElse(List.empty)

  private def buildEuRegistrations(answers: UserAnswers): List[EuTaxRegistration] =
    answers
      .get(AllEuDetailsQuery).getOrElse(List.empty)
      .flatMap {
        detail =>
          buildRegistrationWithFixedEstablishment(detail) orElse buildEuVatRegistration(detail)
    }

  private def buildRegistrationWithFixedEstablishment(euDetails: EuDetails): Option[RegistrationWithFixedEstablishment] =
    for {
      tradingName <- euDetails.fixedEstablishmentTradingName
      address     <- euDetails.fixedEstablishmentAddress
      country     = euDetails.euCountry
      identifier  <- buildEuTaxIdentifier(euDetails)
    } yield RegistrationWithFixedEstablishment(country, identifier, FixedEstablishment(tradingName, address))

  private def buildEuTaxIdentifier(euDetails: EuDetails): Option[EuTaxIdentifier] = {
    import EuTaxIdentifierType._

    euDetails.euVatNumber.map(EuTaxIdentifier(Vat, _)) orElse euDetails.euTaxReference.map(EuTaxIdentifier(Other, _))
  }

  private def buildEuVatRegistration(euDetails: EuDetails): Option[EuVatRegistration] =
    for {
      vatNumber <- euDetails.euVatNumber
      country   = euDetails.euCountry
    } yield EuVatRegistration(country, vatNumber)

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
        answers.get(UkAddressPage)
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
