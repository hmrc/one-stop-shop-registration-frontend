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

import models._
import models.domain._
import models.euDetails._
import models.previousRegistrations.PreviousSchemeNumbers
import pages._
import pages.euDetails._
import pages.previousRegistrations._
import queries.{AllEuOptionalDetailsQuery, AllTradingNames, AllWebsites}

import scala.concurrent.Future
import scala.util.Try

class RegistrationService {

  def toUserAnswers(userId: String, registration: Registration, vatCustomerInfo: VatCustomerInfo): Future[UserAnswers] = {

    val userAnswers = for {
      businessBasedInNiUA <- UserAnswers(userId,
        vatInfo = Some(vatCustomerInfo)
      ).set(BusinessBasedInNiPage, true)
      dateOfFirstSaleUA <- registration.dateOfFirstSale match {
        case Some(dateOfFirstSale) =>
          businessBasedInNiUA.set(DateOfFirstSalePage, dateOfFirstSale)
        case _ =>
          Try(businessBasedInNiUA)
      }
      hasMadeSalesUA <- dateOfFirstSaleUA.set(HasMadeSalesPage,registration.dateOfFirstSale.nonEmpty)

      hasTradingNameUA <- hasMadeSalesUA.set(HasTradingNamePage, registration.tradingNames.nonEmpty)
      tradingNamesUA <- if(registration.tradingNames.nonEmpty) {
        hasTradingNameUA.set(AllTradingNames, registration.tradingNames.toList)
      } else {
        Try(hasTradingNameUA)
      }

      hasTaxRegisteredInEuUA <- tradingNamesUA.set(TaxRegisteredInEuPage, registration.euRegistrations.nonEmpty)

      euVatDetailsUA <- if(registration.euRegistrations.nonEmpty) {
        hasTaxRegisteredInEuUA.set(AllEuOptionalDetailsQuery, getEuRegistrationDetails(registration).toList)
      } else {
        Try(hasTaxRegisteredInEuUA)
      }

      isOnlineMarket <- euVatDetailsUA.set(IsOnlineMarketplacePage, registration.isOnlineMarketplace)

      hasWebsiteUA <- isOnlineMarket.set(HasWebsitePage, registration.websites.nonEmpty)
      websites <- if (registration.websites.nonEmpty) {
        hasWebsiteUA.set(AllWebsites, registration.websites.toList)
      } else {
        Try(hasWebsiteUA)
      }

      bankDetails <- websites.set(BankDetailsPage, registration.bankDetails)

    } yield bankDetails // TODO remove test data
      .set(IsPlanningFirstEligibleSalePage, true).get
      .set(PreviouslyRegisteredPage, false).get
      .set(
        BusinessContactDetailsPage,
        BusinessContactDetails("Joe Bloggs", "01112223344", "email@email.com")).get
      .set(PreviouslyRegisteredPage, true).get
      .set(PreviousEuCountryPage(Index(0)), Country("DE", "Germany")).get
      .set(PreviousSchemePage(Index(0), Index(0)), PreviousScheme.OSSU).get
      .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers("DE123", None)).get

    Future.fromTry(userAnswers)
  }

  private def getEuRegistrationDetails(registration: Registration): Seq[EuOptionalDetails] = {
    registration.euRegistrations.zipWithIndex map {
      case (euVatRegistration: EuVatRegistration, _) =>
        EuOptionalDetails(euCountry = euVatRegistration.country, None, None, None, None,
          euVatNumber = Some(euVatRegistration.vatNumber), None, None, None, None, None)
      case (registrationWithFE: RegistrationWithFixedEstablishment, _) =>
        EuOptionalDetails(euCountry = registrationWithFE.country, Some(true), None, None, None,
          euVatNumber = registrationWithFE.taxIdentifier.value,
          euTaxReference = Some(registrationWithFE.taxIdentifier.identifierType.toString),
          fixedEstablishmentTradingName = Some(registrationWithFE.fixedEstablishment.tradingName),
          fixedEstablishmentAddress = Some(registrationWithFE.fixedEstablishment.address), None, None)
      case (registrationWithoutFE: RegistrationWithoutFixedEstablishmentWithTradeDetails, _) =>
        EuOptionalDetails(euCountry = registrationWithoutFE.country, Some(true), None, None, None,
          euVatNumber = registrationWithoutFE.taxIdentifier.value,
          euTaxReference = Some(registrationWithoutFE.taxIdentifier.identifierType.toString),
          fixedEstablishmentTradingName = Some(registrationWithoutFE.tradeDetails.tradingName),
          fixedEstablishmentAddress = Some(registrationWithoutFE.tradeDetails.address), None, None)
      case (registrationWithoutTaxID: RegistrationWithoutTaxId, _) =>
        EuOptionalDetails(euCountry = registrationWithoutTaxID.country, None, None, None, None, None, None, None, None, None, None)
    }
  }

  /* TODO
        .set(HasTradingNamePage, false).success.value

        .set(IsPlanningFirstEligibleSalePage, true).success.value
        .set(TaxRegisteredInEuPage, false).success.value
        .set(PreviouslyRegisteredPage, false).success.value
        .set(HasWebsitePage, false).success.value

        .set(TaxRegisteredInEuPage, true).success.value
        .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
        .set(SellsGoodsToEUConsumersPage(Index(0)), true).success.value
        .set(SellsGoodsToEUConsumerMethodPage(Index(0)), EuConsumerSalesMethod.DispatchWarehouse).success.value
        .set(RegistrationTypePage(Index(0)), RegistrationType.VatNumber).success.value
        .set(EuVatNumberPage(Index(0)), "FR123456789").success.value
        .set(EuSendGoodsTradingNamePage(Index(0)), "French trading name").success.value
        .set(EuSendGoodsAddressPage(Index(0)), InternationalAddress("Line 1", None, "Town", None, None, Country("FR", "France"))).success.value
        .set(EuCountryPage(Index(1)), Country("DE", "Germany")).success.value
        .set(SellsGoodsToEUConsumersPage(Index(1)), false).success.value
        .set(VatRegisteredPage(Index(1)), true).success.value
        .set(EuVatNumberPage(Index(1)), "DE123456789").success.value
        .set(EuCountryPage(Index(2)), Country("IE", "Ireland")).success.value
        .set(SellsGoodsToEUConsumersPage(Index(2)), true).success.value
        .set(SellsGoodsToEUConsumerMethodPage(Index(2)), EuConsumerSalesMethod.DispatchWarehouse).success.value
        .set(RegistrationTypePage(Index(2)), RegistrationType.TaxId).success.value
        .set(EuTaxReferencePage(Index(2)), "IE123456789").success.value
        .set(EuSendGoodsTradingNamePage(Index(2)), "Irish trading name").success.value
        .set(EuSendGoodsAddressPage(Index(2)), InternationalAddress("Line 1", None, "Town", None, None, Country("IE", "Ireland"))).success.value
        .set(EuCountryPage(Index(3)), Country("CR", "Croatia")).success.value
        .set(SellsGoodsToEUConsumersPage(Index(3)), false).success.value
        .set(VatRegisteredPage(Index(3)), false).success.value
        .set(
          BusinessContactDetailsPage,
          BusinessContactDetails("Joe Bloggs", "01112223344", "email@email.com")).success.value
        .set(HasWebsitePage, true).success.value
        .set(AllWebsites, List("website1", "website2")).success.value
        .set(PreviouslyRegisteredPage, true).success.value
        .set(PreviousEuCountryPage(Index(0)), Country("DE", "Germany")).success.value
        .set(PreviousSchemePage(Index(0), Index(0)), PreviousScheme.OSSU).success.value
        .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers("DE123", None)).success.value
        .set(BankDetailsPage, BankDetails("Account name", Some(bic), iban)).success.value*/

}
