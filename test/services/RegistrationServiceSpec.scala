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

import base.SpecBase
import models.domain.PreviousSchemeNumbers
import models.euDetails.{EuConsumerSalesMethod, RegistrationType}
import models.{BankDetails, Bic, BusinessContactDetails, Country, Iban, Index, InternationalAddress, PreviousScheme, UserAnswers}
import org.scalatest.OptionValues
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._
import pages.euDetails._
import pages.previousRegistrations._
import queries.{AllTradingNames, AllWebsites}
import testutils.RegistrationData

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationServiceSpec
  extends SpecBase
    with MockitoSugar
    with ScalaCheckPropertyChecks
    with OptionValues {

  private val answersPartOfVatGroup =
    UserAnswers("12345-credId",
      vatInfo = Some(vatCustomerInfo)
    )
      .set(BusinessBasedInNiPage, true).success.value
      .set(DateOfFirstSalePage, LocalDate.now).success.value
      .set(HasMadeSalesPage, true).success.value
      .set(HasTradingNamePage, true).success.value
      .set(AllTradingNames, List("single", "double")).success.value
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

      .set(EuCountryPage(Index(3)), Country("HR", "Croatia")).success.value
      .set(SellsGoodsToEUConsumersPage(Index(3)), false).success.value
      .set(VatRegisteredPage(Index(3)), false).success.value

      .set(EuCountryPage(Index(4)), Country("ES", "Spain")).success.value
      .set(SellsGoodsToEUConsumersPage(Index(4)), true).success.value
      .set(SellsGoodsToEUConsumerMethodPage(Index(4)), EuConsumerSalesMethod.FixedEstablishment).success.value
      .set(RegistrationTypePage(Index(4)), RegistrationType.VatNumber).success.value
      .set(EuVatNumberPage(Index(4)), "ES123456789").success.value
      .set(FixedEstablishmentTradingNamePage(Index(4)), "Spanish trading name").success.value
      .set(FixedEstablishmentAddressPage(Index(4)), InternationalAddress("Line 1", None, "Town", None, None, Country("ES", "Spain"))).success.value

      .set(EuCountryPage(Index(5)), Country("DK", "Denmark")).success.value
      .set(SellsGoodsToEUConsumersPage(Index(5)), true).success.value
      .set(SellsGoodsToEUConsumerMethodPage(Index(5)), EuConsumerSalesMethod.FixedEstablishment).success.value
      .set(RegistrationTypePage(Index(5)), RegistrationType.TaxId).success.value
      .set(EuTaxReferencePage(Index(5)), "DK123456789").success.value
      .set(FixedEstablishmentTradingNamePage(Index(5)), "Danish trading name").success.value
      .set(FixedEstablishmentAddressPage(Index(5)), InternationalAddress("Line 1", None, "Town", None, None, Country("DK", "Denmark"))).success.value

      .set(IsOnlineMarketplacePage, false).success.value
      .set(HasWebsitePage, true).success.value
      .set(AllWebsites, List("website1", "website2")).success.value

      .set(BankDetailsPage, BankDetails("Account name", Some(Bic("ABCDGB2A").get), Iban("GB33BUKB20201555555555").
        getOrElse(throw new Exception("TODO")))).success.value

      .set(IsPlanningFirstEligibleSalePage, true).success.value
      .set(
        BusinessContactDetailsPage,
        BusinessContactDetails("Joe Bloggs", "01112223344", "email@email.com")).success.value
      .set(PreviouslyRegisteredPage, true).success.value
      .set(PreviousEuCountryPage(Index(0)), Country("DE", "Germany")).success.value
      .set(PreviousSchemePage(Index(0), Index(0)), PreviousScheme.OSSU).success.value
      .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers("DE123", None)).success.value


  ".toUserAnswers" - {

    "normal registration returns good user answers for all pages" in {

      val service = new RegistrationService()

      val result = service.toUserAnswers(userAnswersId, RegistrationData.registration, vatCustomerInfo).futureValue

      result mustBe answersPartOfVatGroup.copy(lastUpdated = result.lastUpdated)
    }

  }

  ".eligibleSalesDifference" - {
    "return true if the user answers are different" in {
      val service = new RegistrationService()

      val result = service.eligibleSalesDifference(Some(RegistrationData.registration), completeUserAnswers)

      result mustBe true
    }

    "return true if there is no registration provided" in {
      val service = new RegistrationService()

      val result = service.eligibleSalesDifference(None, completeUserAnswers)

      result mustBe true
    }

    "return false if the user answers are not different" in {
      val service = new RegistrationService()

      val userAnswers = completeUserAnswers.set(DateOfFirstSalePage, RegistrationData.registration.dateOfFirstSale.get).success.value

      val result = service.eligibleSalesDifference(Some(RegistrationData.registration), userAnswers)

      result mustBe false
    }
  }

}
