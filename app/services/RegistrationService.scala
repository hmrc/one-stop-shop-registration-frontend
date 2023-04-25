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
import queries.{AllTradingNames, AllWebsites}

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
      isOnlineMarket <- tradingNamesUA.set(IsOnlineMarketplacePage, registration.isOnlineMarketplace)
      hasWebsiteUA <- isOnlineMarket.set(HasWebsitePage, registration.websites.nonEmpty)
      websites <- if (registration.websites.nonEmpty) {
        hasWebsiteUA.set(AllWebsites, registration.websites.toList)
      } else {
        Try(hasWebsiteUA)
      }

    } yield websites // TODO remove test data
      .set(IsPlanningFirstEligibleSalePage, true).get
      .set(TaxRegisteredInEuPage, false).get
      .set(PreviouslyRegisteredPage, false).get
      .set(TaxRegisteredInEuPage, true).get
      .set(EuCountryPage(Index(0)), Country("FR", "France")).get
      .set(SellsGoodsToEUConsumersPage(Index(0)), true).get
      .set(SellsGoodsToEUConsumerMethodPage(Index(0)), EuConsumerSalesMethod.DispatchWarehouse).get
      .set(RegistrationTypePage(Index(0)), RegistrationType.VatNumber).get
      .set(EuVatNumberPage(Index(0)), "FR123456789").get
      .set(EuSendGoodsTradingNamePage(Index(0)), "French trading name").get
      .set(EuSendGoodsAddressPage(Index(0)), InternationalAddress("Line 1", None, "Town", None, None, Country("FR", "France"))).get
      .set(EuCountryPage(Index(1)), Country("DE", "Germany")).get
      .set(SellsGoodsToEUConsumersPage(Index(1)), false).get
      .set(VatRegisteredPage(Index(1)), true).get
      .set(EuVatNumberPage(Index(1)), "DE123456789").get
      .set(EuCountryPage(Index(2)), Country("IE", "Ireland")).get
      .set(SellsGoodsToEUConsumersPage(Index(2)), true).get
      .set(SellsGoodsToEUConsumerMethodPage(Index(2)), EuConsumerSalesMethod.DispatchWarehouse).get
      .set(RegistrationTypePage(Index(2)), RegistrationType.TaxId).get
      .set(EuTaxReferencePage(Index(2)), "IE123456789").get
      .set(EuSendGoodsTradingNamePage(Index(2)), "Irish trading name").get
      .set(EuSendGoodsAddressPage(Index(2)), InternationalAddress("Line 1", None, "Town", None, None, Country("IE", "Ireland"))).get
      .set(EuCountryPage(Index(3)), Country("CR", "Croatia")).get
      .set(SellsGoodsToEUConsumersPage(Index(3)), false).get
      .set(VatRegisteredPage(Index(3)), false).get
      .set(
        BusinessContactDetailsPage,
        BusinessContactDetails("Joe Bloggs", "01112223344", "email@email.com")).get
      .set(PreviouslyRegisteredPage, true).get
      .set(PreviousEuCountryPage(Index(0)), Country("DE", "Germany")).get
      .set(PreviousSchemePage(Index(0), Index(0)), PreviousScheme.OSSU).get
      .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers("DE123", None)).get
      .set(BankDetailsPage, BankDetails("Account name", Some(Bic("ABCDGB2A").get), Iban("GB33BUKB20201555555555").getOrElse(throw new Exception("TODO")))).get

    Future.fromTry(userAnswers)
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
