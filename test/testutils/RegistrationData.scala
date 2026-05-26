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

package testutils

import base.SpecBase
import generators.Generators
import models.domain.*
import models.euDetails.{EuConsumerSalesMethod, RegistrationType}
import models.previousRegistrations.NonCompliantDetails
import models.{BankDetails, Bic, BusinessContactDetails, Country, DesAddress, Iban, Index, InternationalAddress, PreviousScheme, UserAnswers}
import org.scalatest.EitherValues
import pages.euDetails.*
import pages.previousRegistrations.{PreviousEuCountryPage, PreviousOssNumberPage, PreviousSchemePage, PreviouslyRegisteredPage}
import pages.*
import queries.{AllTradingNames, AllWebsites}
import uk.gov.hmrc.domain.Vrn

import java.time.LocalDate

object RegistrationData extends Generators with EitherValues with SpecBase {

  val iban: Iban = Iban("GB33BUKB20201555555555").value
  val bic: Bic = Bic("ABCDGB2A").get

  val registration: Registration =
    Registration(
      vrn = Vrn("123456789"),
      registeredCompanyName = "Company name",
      tradingNames = List("single", "double"),
      vatDetails = VatDetails(
        registrationDate = LocalDate.now(),
        address = DesAddress("Line 1", None, None, None, None, Some("AA11 1AA"), "GB"),
        partOfVatGroup = false,
        source = VatDetailSource.Etmp
      ),
      euRegistrations = Seq(
        RegistrationWithoutFixedEstablishmentWithTradeDetails(
          Country("FR", "France"),
          EuTaxIdentifier(EuTaxIdentifierType.Vat, Some("FRAA123456789")),
          TradeDetails(
            "French trading name",
            InternationalAddress(
              line1 = "Line 1",
              line2 = None,
              townOrCity = "Town",
              stateOrRegion = None,
              None,
              Country("FR", "France")
            ))
        ),
        EuVatRegistration(
          Country("DE", "Germany"),
          vatNumber = "DE123456789"
        ),
        RegistrationWithoutFixedEstablishmentWithTradeDetails(
          Country("IE", "Ireland"),
          EuTaxIdentifier(EuTaxIdentifierType.Other, Some("IE1234567AB")),
          TradeDetails(
            "Irish trading name",
            InternationalAddress(
              line1 = "Line 1",
              line2 = None,
              townOrCity = "Town",
              stateOrRegion = None,
              None,
              Country("IE", "Ireland")
            ))
        ),
        RegistrationWithFixedEstablishment(
          Country("ES", "Spain"),
          EuTaxIdentifier(EuTaxIdentifierType.Vat, Some("ESA1234567A")),
          TradeDetails("Spanish trading name", InternationalAddress("Line 1", None, "Town", None, None, Country("ES", "Spain")))
        ),
        RegistrationWithFixedEstablishment(
          Country("DK", "Denmark"),
          EuTaxIdentifier(EuTaxIdentifierType.Other, Some("DK12345678")),
          TradeDetails("Danish trading name", InternationalAddress("Line 1", None, "Town", None, None, Country("DK", "Denmark")))
        )
      ),
      contactDetails = createBusinessContactDetails(),
      websites = Seq("website1", "website2"),
      commencementDate = LocalDate.now(),
      previousRegistrations = Seq(
        PreviousRegistrationNew(
          country = Country("DE", "Germany"),
          previousSchemesDetails = Seq(
            PreviousSchemeDetails(
              previousScheme = PreviousScheme.OSSU,
              previousSchemeNumbers = PreviousSchemeNumbers("DE123", None),
              nonCompliantDetails = Some(
                NonCompliantDetails(
                  nonCompliantReturns = Some(1),
                  nonCompliantPayments = Some(1)
                )
              )
            )
          )
        )
      ),
      bankDetails = BankDetails("Account name", Some(bic), iban),
      isOnlineMarketplace = false,
      niPresence = Some(PrincipalPlaceOfBusinessInNi),
      dateOfFirstSale = Some(LocalDate.now()),
      unusableStatus = None,
      nonCompliantReturns = Some("1"),
      nonCompliantPayments = Some("1")
    )

  private def createBusinessContactDetails(): BusinessContactDetails =
    BusinessContactDetails(
      "Joe Bloggs",
      "01112223344",
      "email@email.com"
    )

  val registrationToUserAnswers: UserAnswers =
    UserAnswers("12345-credId",
      vatInfo = Some(
        VatCustomerInfo(
          registrationDate = LocalDate.now(),
          address = DesAddress("Line 1", None, None, None, None, Some("AA11 1AA"), "GB"),
          partOfVatGroup = false,
          organisationName = Some("Company name"),
          singleMarketIndicator = Some(true),
          individualName = None,
          deregistrationDecisionDate = None
        )
      )
    )
      .set(BusinessBasedInNiPage, true).success.value
      .set(HasMadeSalesPage, true).success.value
      .set(DateOfFirstSalePage, LocalDate.now()).success.value
      .set(HasTradingNamePage, true).success.value
      .set(AllTradingNames, List("single", "double")).success.value
      .set(TaxRegisteredInEuPage, true).success.value

      .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
      .set(SellsGoodsToEUConsumersPage(Index(0)), true).success.value
      .set(SellsGoodsToEUConsumerMethodPage(Index(0)), EuConsumerSalesMethod.DispatchWarehouse).success.value
      .set(VatRegisteredPage(Index(0)), true).success.value
      .set(RegistrationTypePage(Index(0)), RegistrationType.VatNumber).success.value
      .set(EuVatNumberPage(Index(0)), "FRAA123456789").success.value
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

      .set(EuCountryPage(Index(3)), Country("ES", "Spain")).success.value
      .set(SellsGoodsToEUConsumersPage(Index(3)), true).success.value
      .set(SellsGoodsToEUConsumerMethodPage(Index(3)), EuConsumerSalesMethod.FixedEstablishment).success.value
      .set(RegistrationTypePage(Index(3)), RegistrationType.VatNumber).success.value
      .set(EuVatNumberPage(Index(3)), "ESA1234567A").success.value
      .set(FixedEstablishmentTradingNamePage(Index(3)), "Spanish trading name").success.value
      .set(FixedEstablishmentAddressPage(Index(3)), InternationalAddress("Line 1", None, "Town", None, None, Country("ES", "Spain"))).success.value

      .set(EuCountryPage(Index(4)), Country("DK", "Denmark")).success.value
      .set(SellsGoodsToEUConsumersPage(Index(4)), true).success.value
      .set(SellsGoodsToEUConsumerMethodPage(Index(4)), EuConsumerSalesMethod.FixedEstablishment).success.value
      .set(RegistrationTypePage(Index(4)), RegistrationType.TaxId).success.value
      .set(EuTaxReferencePage(Index(4)), "DK123456789").success.value
      .set(FixedEstablishmentTradingNamePage(Index(4)), "Danish trading name").success.value
      .set(FixedEstablishmentAddressPage(Index(4)), InternationalAddress("Line 1", None, "Town", None, None, Country("DK", "Denmark"))).success.value

      .set(IsOnlineMarketplacePage, false).success.value
      .set(HasWebsitePage, true).success.value
      .set(AllWebsites, List("website1", "website2")).success.value

      .set(BankDetailsPage, BankDetails("Account name", Some(Bic("ABCDGB2A").get), Iban("GB33BUKB20201555555555").
        getOrElse(throw new Exception("TODO")))).success.value

      .set(PreviouslyRegisteredPage, true).success.value
      .set(PreviousEuCountryPage(Index(0)), Country("DE", "Germany")).success.value
      .set(PreviousSchemePage(Index(0), Index(0)), PreviousScheme.OSSU).success.value
      .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers("DE123", None)).success.value
      .set(
        BusinessContactDetailsPage,
        BusinessContactDetails("Joe Bloggs", "01112223344", "email@email.com")
      ).success.value
}

