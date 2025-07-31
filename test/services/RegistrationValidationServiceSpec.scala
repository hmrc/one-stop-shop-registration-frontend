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

package services

import base.SpecBase
import cats.data.NonEmptyChain
import cats.data.Validated.{Invalid, Valid}
import models.*
import models.domain.*
import models.euDetails.{EuConsumerSalesMethod, RegistrationType}
import models.previousRegistrations.NonCompliantDetails
import models.requests.AuthenticatedDataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.*
import pages.euDetails.*
import pages.previousRegistrations.{PreviousEuCountryPage, PreviousOssNumberPage, PreviousSchemePage, PreviouslyRegisteredPage}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import queries.previousRegistration.{AllPreviousRegistrationsRawQuery, NonCompliantDetailsQuery}
import queries.{AllEuDetailsRawQuery, AllTradingNames, AllWebsites}
import testutils.RegistrationData
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class RegistrationValidationServiceSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val iban = RegistrationData.iban
  private val bic = RegistrationData.bic

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  private val request = AuthenticatedDataRequest(FakeRequest("GET", "/"), testCredentials, vrn, None, emptyUserAnswers, None, 0, None)
  private implicit val dataRequest: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest(request, testCredentials, vrn, None, emptyUserAnswers, None, 0, None)

  private val mockDateService: DateService = mock[DateService]
  private val mockRegistrationService: RegistrationService = mock[RegistrationService]

  private def getRegistrationService =
    new RegistrationValidationService(mockDateService, mockRegistrationService)

  private val nonCompliantDetails: NonCompliantDetails = NonCompliantDetails(nonCompliantReturns = Some(1), nonCompliantPayments = Some(1))

  private val answersPartOfVatGroup =
    UserAnswers("id",
      vatInfo = Some(VatCustomerInfo(
        DesAddress("Line 1", None, None, None, None, Some("AA11 1AA"), "GB"),
        LocalDate.now,
        partOfVatGroup = true,
        Some("foo"),
        Some("a b c"),
        Some(true),
        Some(LocalDate.now)
      ))
    )
      .set(BusinessBasedInNiPage, true).success.value
      .set(DateOfFirstSalePage, arbitraryDate).success.value
      .set(HasTradingNamePage, true).success.value
      .set(AllTradingNames, List("single", "double")).success.value
      .set(TaxRegisteredInEuPage, true).success.value
      .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
      .set(SellsGoodsToEUConsumersPage(Index(0)), true).success.value
      .set(SellsGoodsToEUConsumerMethodPage(Index(0)), EuConsumerSalesMethod.DispatchWarehouse).success.value
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
      .set(EuTaxReferencePage(Index(2)), "IE1234567AB").success.value
      .set(EuSendGoodsTradingNamePage(Index(2)), "Irish trading name").success.value
      .set(EuSendGoodsAddressPage(Index(2)), InternationalAddress("Line 1", None, "Town", None, None, Country("IE", "Ireland"))).success.value
      .set(
        BusinessContactDetailsPage,
        BusinessContactDetails("Joe Bloggs", "01112223344", "email@email.com")).success.value
      .set(HasWebsitePage, true).success.value
      .set(AllWebsites, List("website1", "website2")).success.value
      .set(PreviouslyRegisteredPage, true).success.value
      .set(PreviousEuCountryPage(Index(0)), Country("DE", "Germany")).success.value
      .set(PreviousSchemePage(Index(0), Index(0)), PreviousScheme.OSSU).success.value
      .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers("DE123", None)).success.value
      .set(NonCompliantDetailsQuery(Index(0), Index(0)), nonCompliantDetails).success.value
      .set(BankDetailsPage, BankDetails("Account name", Some(bic), iban)).success.value
      .set(IsOnlineMarketplacePage, false).success.value

  private val answersNotPartOfVatGroup =
    answersPartOfVatGroup.copy(vatInfo =
        Some(VatCustomerInfo(
          DesAddress("Line 1", None, None, None, None, Some("AA11 1AA"), "GB"),
          LocalDate.now,
          partOfVatGroup = false,
          organisationName = Some("foo"),
          individualName = None,
          singleMarketIndicator = Some(true),
          Some(LocalDate.now)
        ))
      )
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
      .set(EuTaxReferencePage(Index(4)), "DK12345678").success.value
      .set(FixedEstablishmentTradingNamePage(Index(4)), "Danish trading name").success.value
      .set(FixedEstablishmentAddressPage(Index(4)), InternationalAddress("Line 1", None, "Town", None, None, Country("DK", "Denmark"))).success.value

  "fromUserAnswers" - {

    "must return a Registration when user answers are provided and we have full VAT information on the user and is not part of vat group" in {

      when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(Some(arbitraryDate))
      when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

      val regDate = LocalDate.of(2000, 1, 1)
      val address = DesAddress("Line 1", None, None, None, None, Some("BB22 2BB"), "GB")
      val vatInfo = VatCustomerInfo(
        registrationDate = regDate,
        address = address,
        partOfVatGroup = false,
        organisationName = Some("bar"),
        individualName = Some("a b c"),
        singleMarketIndicator = Some(true),
        deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate))
      )

      val userAnswers =
        answersNotPartOfVatGroup.copy(vatInfo = Some(vatInfo))

      val registration = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

      val expectedRegistration =
        RegistrationData.registration.copy(
          dateOfFirstSale = Some(arbitraryDate),
          vatDetails = VatDetails(regDate, address, partOfVatGroup = false, VatDetailSource.Etmp),
          registeredCompanyName = "bar",
          commencementDate = arbitraryDate
        )

      registration mustEqual Valid(expectedRegistration)
    }

    "must return a Registration when no trading names, EU countries or websites were provided" in {

      when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

      val userAnswers =
        answersNotPartOfVatGroup
          .set(HasTradingNamePage, false).success.value
          .remove(AllTradingNames).success.value
          .set(TaxRegisteredInEuPage, false).success.value
          .remove(AllEuDetailsRawQuery).success.value
          .set(HasWebsitePage, false).success.value
          .remove(AllWebsites).success.value

      val expectedRegistration =
        RegistrationData.registration.copy(
          dateOfFirstSale = Some(arbitraryDate),
          tradingNames = Seq.empty,
          euRegistrations = Seq.empty,
          vatDetails = RegistrationData.registration.vatDetails,
          websites = Seq.empty,
          commencementDate = arbitraryDate
        )

      val registration = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

      registration mustEqual Valid(expectedRegistration)
    }

    "must return a Registration when user answers are provided and BusinessBasedInNi is false and HasFixedEstablishmentInNi is true" in {

      when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

      val regDate = LocalDate.of(2000, 1, 1)
      val address = DesAddress("Line 1", None, None, None, None, Some("BB22 2BB"), "GB")
      val vatInfo = VatCustomerInfo(
        registrationDate = regDate,
        address = address,
        partOfVatGroup = false,
        organisationName = Some("bar"),
        individualName = Some("a b c"),
        singleMarketIndicator = Some(true),
        deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate))
      )

      val userAnswers =
        answersNotPartOfVatGroup.copy(vatInfo = Some(vatInfo))
          .set(BusinessBasedInNiPage, false).success.value
          .set(HasFixedEstablishmentInNiPage, true).success.value

      val registration = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

      val expectedRegistration =
        RegistrationData.registration.copy(
          dateOfFirstSale = Some(arbitraryDate),
          vatDetails = VatDetails(regDate, address, false, VatDetailSource.Etmp),
          registeredCompanyName = "bar",
          commencementDate = arbitraryDate,
          niPresence = Some(FixedEstablishmentInNi)
        )

      registration mustEqual Valid(expectedRegistration)
    }

    "must return a Registration when user answers are provided and BusinessBasedInNi is false and HasFixedEstablishmentInNi is false and SalesChannels has been answered" in {

      when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

      val regDate = LocalDate.of(2000, 1, 1)
      val address = DesAddress("Line 1", None, None, None, None, Some("BB22 2BB"), "GB")
      val vatInfo = VatCustomerInfo(
        registrationDate = regDate,
        address = address,
        partOfVatGroup = false,
        organisationName = Some("bar"),
        individualName = Some("a b c"),
        singleMarketIndicator = Some(true),
        deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate))
      )

      val userAnswers =
        answersNotPartOfVatGroup.copy(vatInfo = Some(vatInfo))
          .set(BusinessBasedInNiPage, false).success.value
          .set(HasFixedEstablishmentInNiPage, false).success.value
          .set(SalesChannelsPage, SalesChannels.OnlineMarketplaces).success.value

      val registration = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

      val expectedRegistration =
        RegistrationData.registration.copy(
          dateOfFirstSale = Some(arbitraryDate),
          vatDetails = VatDetails(regDate, address, false, VatDetailSource.Etmp),
          registeredCompanyName = "bar",
          commencementDate = arbitraryDate,
          niPresence = Some(NoPresence(SalesChannels.OnlineMarketplaces))
        )

      registration mustEqual Valid(expectedRegistration)
    }

    "must return a registration when a user is part of a vat group and has eu registrations with vat numbers and no other details" in {

      when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true
      val userAnswers =
        UserAnswers("id", vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
          .set(BusinessBasedInNiPage, true).success.value
          .set(DateOfFirstSalePage, arbitraryDate).success.value
          .set(HasTradingNamePage, true).success.value
          .set(AllTradingNames, List("single", "double")).success.value
          .set(TaxRegisteredInEuPage, true).success.value
          .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
          .set(SellsGoodsToEUConsumersPage(Index(0)), false).success.value
          .set(VatRegisteredPage(Index(0)), true).success.value
          .set(EuVatNumberPage(Index(0)), "FRAA123456789").success.value
          .set(
            BusinessContactDetailsPage,
            BusinessContactDetails("Joe Bloggs", "01112223344", "email@email.com")).success.value
          .set(HasWebsitePage, true).success.value
          .set(AllWebsites, List("website1", "website2")).success.value
          .set(PreviouslyRegisteredPage, false).success.value
          .set(BankDetailsPage, BankDetails("Account name", Some(bic), iban)).success.value
          .set(IsOnlineMarketplacePage, false).success.value

      val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

      val expectedRegistration = {
        RegistrationData.registration.copy(
          dateOfFirstSale = Some(arbitraryDate),
          registeredCompanyName = "Company name",
          vatDetails = VatDetails(
            registrationDate = LocalDate.now(stubClockAtArbitraryDate),
            address = DesAddress("Line 1", None, None, None, None, Some("AA11 1AA"), "GB"),
            partOfVatGroup = true,
            source = VatDetailSource.Etmp
          ),
          commencementDate = arbitraryDate,
          previousRegistrations = Seq.empty,
          euRegistrations = Seq(
            EuVatRegistration(Country("FR", "France"), "FRAA123456789")
          ),
          nonCompliantReturns = None,
          nonCompliantPayments = None,
        )
      }
      result mustEqual Valid(expectedRegistration)

    }

    "must return a registration" - {

      "when Business Based in NI is missing" in {

        when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

        val userAnswers = answersNotPartOfVatGroup.remove(BusinessBasedInNiPage).success.value

        val expectedRegistration =
          RegistrationData.registration.copy(
            dateOfFirstSale = Some(arbitraryDate),
            vatDetails = RegistrationData.registration.vatDetails,
            niPresence = None,
            commencementDate = arbitraryDate
          )

        val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

        result mustEqual Valid(expectedRegistration)
      }

      "when Business Based in NI is false and Has Fixed Establishment in NI is missing" in {

        when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

        val userAnswers =
          answersNotPartOfVatGroup
            .set(BusinessBasedInNiPage, false).success.value
            .remove(HasFixedEstablishmentInNiPage).success.value

        val expectedRegistration =
          RegistrationData.registration.copy(
            dateOfFirstSale = Some(arbitraryDate),
            vatDetails = RegistrationData.registration.vatDetails,
            niPresence = None,
            commencementDate = arbitraryDate
          )

        val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

        result mustEqual Valid(expectedRegistration)
      }

      "when Has Fixed Establishment in NI is false and Sales Channels is missing" in {

        when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

        val userAnswers =
          answersNotPartOfVatGroup
            .set(BusinessBasedInNiPage, false).success.value
            .set(HasFixedEstablishmentInNiPage, false).success.value
            .remove(SalesChannelsPage).success.value

        val expectedRegistration =
          RegistrationData.registration.copy(
            dateOfFirstSale = Some(arbitraryDate),
            vatDetails = RegistrationData.registration.vatDetails,
            niPresence = None,
            commencementDate = arbitraryDate
          )

        val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

        result mustEqual Valid(expectedRegistration)
      }

      "when Date Of First Sale is missing and previously registered is true" in {

        when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

        val userAnswers = answersNotPartOfVatGroup
          .remove(DateOfFirstSalePage).success.value
          .set(PreviouslyRegisteredPage, true).success.value

        val expectedRegistration =
          RegistrationData.registration.copy(
            vatDetails = RegistrationData.registration.vatDetails,
            dateOfFirstSale = None,
            commencementDate = arbitraryDate
          )

        val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

        result mustEqual Valid(expectedRegistration)
      }
    }

    "must return Invalid" - {

      "when Vat Customer Info is missing" in {

        when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

        val userAnswers = answersNotPartOfVatGroup.copy(vatInfo = None)
        val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

        result mustEqual Invalid(NonEmptyChain(DataMissingError(CheckVatDetailsPage), DataMissingError(CheckVatDetailsPage)))
      }

      "when Has Trading Name is missing" in {

        when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

        val userAnswers = answersNotPartOfVatGroup.remove(HasTradingNamePage).success.value
        val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

        result mustEqual Invalid(NonEmptyChain(DataMissingError(HasTradingNamePage)))
      }

      "when Has Trading Name is true, but there are no trading names" in {

        when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

        val userAnswers = answersNotPartOfVatGroup.remove(AllTradingNames).success.value
        val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

        result mustEqual Invalid(NonEmptyChain(DataMissingError(AllTradingNames)))
      }

      "when Has Trading Name is false, but there are trading names" in {

        val userAnswers = answersNotPartOfVatGroup.set(HasTradingNamePage, false).success.value
        val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

        result mustEqual Invalid(NonEmptyChain(DataMissingError(HasTradingNamePage)))
      }

      "when both Date of First Sale and previously registered are missing" in {

        when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

        val userAnswers = answersNotPartOfVatGroup
          .remove(DateOfFirstSalePage).success.value
          .remove(PreviouslyRegisteredPage).success.value

        val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

        result mustEqual Invalid(NonEmptyChain(DataMissingError(PreviouslyRegisteredPage)))
      }

      "when Contact Details are missing" in {

        when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

        val userAnswers = answersNotPartOfVatGroup.remove(BusinessContactDetailsPage).success.value
        val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

        result mustEqual Invalid(NonEmptyChain(DataMissingError(BusinessContactDetailsPage)))
      }

      "when Bank Details are missing" in {

        when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

        val userAnswers = answersNotPartOfVatGroup.remove(BankDetailsPage).success.value
        val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

        result mustEqual Invalid(NonEmptyChain(DataMissingError(BankDetailsPage)))
      }

      "when Has Website is missing" in {

        when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

        val userAnswers = answersNotPartOfVatGroup.remove(HasWebsitePage).success.value
        val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

        result mustEqual Invalid(NonEmptyChain(DataMissingError(HasWebsitePage)))
      }

      "when Has Website is true, but there are no websites" in {

        when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

        val userAnswers =
          answersNotPartOfVatGroup
            .set(HasWebsitePage, true).success.value
            .remove(AllWebsites).success.value

        val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

        result mustEqual Invalid(NonEmptyChain(DataMissingError(AllWebsites)))
      }

      "when Has Website is false, but there are websites" in {

        val userAnswers =
          answersNotPartOfVatGroup
            .set(HasWebsitePage, false).success.value

        val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

        result mustEqual Invalid(NonEmptyChain(DataMissingError(HasWebsitePage)))
      }

      "when Is Online Marketplace is missing" in {

        when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

        val userAnswers = answersNotPartOfVatGroup.remove(IsOnlineMarketplacePage).success.value
        val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

        result mustEqual Invalid(NonEmptyChain(DataMissingError(IsOnlineMarketplacePage)))
      }

      "when Previously Registered has not been answered" in {

        when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

        val userAnswers = answersNotPartOfVatGroup.remove(PreviouslyRegisteredPage).success.value
        val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

        result mustEqual Invalid(NonEmptyChain(DataMissingError(PreviouslyRegisteredPage)))
      }

      "when Previously Registered is true" - {

        "but there are no previous registrations" in {

          when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

          val userAnswers =
            answersNotPartOfVatGroup
              .set(PreviouslyRegisteredPage, true).success.value
              .remove(AllPreviousRegistrationsRawQuery).success.value

          val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

          result mustEqual Invalid(NonEmptyChain(DataMissingError(AllPreviousRegistrationsRawQuery)))
        }

        "but there is a previous registration without a country" in {

          when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

          val userAnswers = answersNotPartOfVatGroup.remove(PreviousEuCountryPage(Index(0))).success.value
          val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

          result mustEqual Invalid(NonEmptyChain(DataMissingError(PreviousEuCountryPage(Index(0)))))
        }

        "but there is a previous registration without a VAT number" in {

          when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

          val userAnswers = answersNotPartOfVatGroup.remove(PreviousOssNumberPage(Index(0), Index(0))).success.value
          val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

          result mustEqual Invalid(NonEmptyChain(DataMissingError(PreviousOssNumberPage(Index(0), Index(0)))))
        }
      }

      "when Previously Registered is false and there are previous registrations" in {
        val userAnswers =
          answersNotPartOfVatGroup
            .set(PreviouslyRegisteredPage, false).success.value

        val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

        result mustEqual Invalid(NonEmptyChain(DataMissingError(AllPreviousRegistrationsRawQuery)))
      }

      "when Tax Registered in EU is missing" in {

        when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

        val userAnswers = answersNotPartOfVatGroup.remove(TaxRegisteredInEuPage).success.value
        val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

        result mustEqual Invalid(NonEmptyChain(DataMissingError(TaxRegisteredInEuPage)))
      }

      "when Tax Registered in EU is false and there are EU countries" in {
        val userAnswers =
          answersNotPartOfVatGroup
            .set(TaxRegisteredInEuPage, false).success.value

        val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

        result mustEqual Invalid(NonEmptyChain(DataMissingError(AllEuDetailsRawQuery)))
      }

      "when Tax Registered in EU is true" - {

        "and there are no EU country details" in {

          when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

          val userAnswers =
            answersNotPartOfVatGroup
              .set(TaxRegisteredInEuPage, true).success.value
              .remove(AllEuDetailsRawQuery).success.value

          val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

          result mustEqual Invalid(NonEmptyChain(DataMissingError(AllEuDetailsRawQuery)))
        }

        "and there is a record with no country" in {

          when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

          val userAnswers = answersNotPartOfVatGroup.remove(EuCountryPage(Index(0))).success.value
          val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

          result mustEqual Invalid(NonEmptyChain(DataMissingError(EuCountryPage(Index(0)))))
        }

        "and there is a record with a country" - {

          "when Part of VAT group is true" - {

            "when Sells Goods To EU Consumers is true" - {

              "when Sells Goods To EU Consumer Method is DispatchWarehouse" - {

                "when Registration Type is Vat Number" - {

                  "and Vat Number is missing" in {

                    when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                    val userAnswers = answersPartOfVatGroup.remove(EuVatNumberPage(Index(0))).success.value
                    val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                    result mustEqual Invalid(NonEmptyChain(DataMissingError(EuVatNumberPage(Index(0)))))

                  }

                }

                "when Registration Type is Tax Id" - {

                  "and TaxId is missing" in {

                    when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                    val userAnswers = answersPartOfVatGroup.remove(EuTaxReferencePage(Index(2))).success.value
                    val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                    result mustEqual Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(2)))))

                  }

                }

                "when Registration Type is missing" - {

                  when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(Some(arbitraryDate))
                  when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                  val userAnswers = answersPartOfVatGroup.remove(RegistrationTypePage(Index(0))).success.value
                  val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(RegistrationTypePage(Index(0)))))

                }

                "when EU Send Goods Trading Name is missing" in {

                  when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                  val userAnswers = answersPartOfVatGroup.remove(EuSendGoodsTradingNamePage(Index(0))).success.value
                  val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(EuSendGoodsTradingNamePage(Index(0)))))

                }

                "when EU Send Goods Address is missing" in {

                  when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                  val userAnswers = answersPartOfVatGroup.remove(EuSendGoodsAddressPage(Index(0))).success.value
                  val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(EuSendGoodsAddressPage(Index(0)))))

                }

              }

              "when Sells Goods To EU Consumer Method is missing" - {

                when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(Some(arbitraryDate))
                when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                val userAnswers = answersPartOfVatGroup.remove(SellsGoodsToEUConsumerMethodPage(Index(0))).success.value
                val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                result mustEqual Invalid(NonEmptyChain(DataMissingError(SellsGoodsToEUConsumerMethodPage(Index(0)))))

              }

            }

            "when Sells Goods To EU Consumers is false" - {

              "when Vat Registered is true" - {

                "and EU Vat Number is missing" in {

                  when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                  val userAnswers = answersPartOfVatGroup.remove(EuVatNumberPage(Index(1))).success.value
                  val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(EuVatNumberPage(Index(1)))))

                }
              }

              "when Vat Registered is missing" in {

                when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                val userAnswers = answersPartOfVatGroup.remove(VatRegisteredPage(Index(1))).success.value
                val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                result mustEqual Invalid(NonEmptyChain(DataMissingError(VatRegisteredPage(Index(1)))))

              }

            }

            "when Sells Goods To EU Consumers is missing" in {

              when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

              val userAnswers = answersPartOfVatGroup.remove(SellsGoodsToEUConsumersPage(Index(0))).success.value
              val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

              result mustEqual Invalid(NonEmptyChain(DataMissingError(SellsGoodsToEUConsumersPage(Index(0)))))

            }

          }

          "when Part of VAT group is false" - {

            "when Sells Goods To EU Consumers is true" - {

              "when Sells Goods To EU Consumer Method is DispatchWarehouse" - {

                "when Registration Type is Vat Number" - {

                  "and Vat Number is missing" in {

                    when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                    val userAnswers = answersNotPartOfVatGroup.remove(EuVatNumberPage(Index(0))).success.value
                    val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                    result mustEqual Invalid(NonEmptyChain(DataMissingError(EuVatNumberPage(Index(0)))))

                  }

                }

                "when Registration Type is Tax Id" - {

                  "and TaxId is missing" in {

                    when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                    val userAnswers = answersNotPartOfVatGroup.remove(EuTaxReferencePage(Index(2))).success.value
                    val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                    result mustEqual Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(2)))))

                  }

                }

                "when Registration Type is missing" - {

                  when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(Some(arbitraryDate))
                  when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                  val userAnswers = answersNotPartOfVatGroup.remove(RegistrationTypePage(Index(0))).success.value
                  val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(RegistrationTypePage(Index(0)))))

                }

                "when EU Send Goods Trading Name is missing" in {

                  when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                  val userAnswers = answersNotPartOfVatGroup.remove(EuSendGoodsTradingNamePage(Index(0))).success.value
                  val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(EuSendGoodsTradingNamePage(Index(0)))))

                }

                "when EU Send Goods Address is missing" in {

                  when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                  val userAnswers = answersNotPartOfVatGroup.remove(EuSendGoodsAddressPage(Index(0))).success.value
                  val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(EuSendGoodsAddressPage(Index(0)))))

                }

              }

              "when Sells Goods To EU Consumer Method is Fixed Establishment" - {

                "when Registration Type is Vat Number" - {

                  "and Vat Number is missing" in {

                    when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                    val userAnswers = answersNotPartOfVatGroup.remove(EuVatNumberPage(Index(3))).success.value
                    val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                    result mustEqual Invalid(NonEmptyChain(DataMissingError(EuVatNumberPage(Index(3)))))

                  }

                }

                "when Registration Type is Tax Id" - {

                  "and TaxId is missing" in {

                    when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                    val userAnswers = answersNotPartOfVatGroup.remove(EuTaxReferencePage(Index(4))).success.value
                    val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                    result mustEqual Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(4)))))

                  }

                }

                "when Registration Type is missing" - {

                  when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(Some(arbitraryDate))
                  when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                  val userAnswers = answersNotPartOfVatGroup.remove(RegistrationTypePage(Index(4))).success.value
                  val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(RegistrationTypePage(Index(4)))))

                }

                "when Fixed Establishment Trading Name is missing" in {

                  when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                  val userAnswers = answersNotPartOfVatGroup.remove(FixedEstablishmentTradingNamePage(Index(4))).success.value
                  val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(FixedEstablishmentTradingNamePage(Index(4)))))

                }

                "when Fixed Establishment Address is missing" in {

                  when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                  val userAnswers = answersNotPartOfVatGroup.remove(FixedEstablishmentAddressPage(Index(4))).success.value
                  val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(FixedEstablishmentAddressPage(Index(4)))))

                }

              }

              "when Sells Goods To EU Consumer Method is missing" - {

                when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(Some(arbitraryDate))
                when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                val userAnswers = answersNotPartOfVatGroup.remove(SellsGoodsToEUConsumerMethodPage(Index(0))).success.value
                val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                result mustEqual Invalid(NonEmptyChain(DataMissingError(SellsGoodsToEUConsumerMethodPage(Index(0)))))

              }

            }

            "when Sells Goods To EU Consumers is false" - {

              "when Vat Registered is true" - {

                "and EU Vat Number is missing" in {

                  when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                  val userAnswers = answersNotPartOfVatGroup.remove(EuVatNumberPage(Index(1))).success.value
                  val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(EuVatNumberPage(Index(1)))))

                }
              }

              "when Vat Registered is missing" in {

                when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

                val userAnswers = answersNotPartOfVatGroup.remove(VatRegisteredPage(Index(1))).success.value
                val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

                result mustEqual Invalid(NonEmptyChain(DataMissingError(VatRegisteredPage(Index(1)))))

              }

            }

            "when Sells Goods To EU Consumers is missing" in {

              when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

              val userAnswers = answersNotPartOfVatGroup.remove(SellsGoodsToEUConsumersPage(Index(0))).success.value
              val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

              result mustEqual Invalid(NonEmptyChain(DataMissingError(SellsGoodsToEUConsumersPage(Index(0)))))

            }

          }
        }
      }

      "when Tax Registered has not been answered" in {

        when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

        val userAnswers =
          answersNotPartOfVatGroup
            .remove(TaxRegisteredInEuPage).success.value

        val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

        result mustEqual Invalid(NonEmptyChain(DataMissingError(TaxRegisteredInEuPage)))

      }

    }

    "date of first sales logic" - {

      "should return Valid" - {

        "when Has Made Sales is Yes and Date of First Sale is populated" in {

          when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

          val userAnswers =
            answersNotPartOfVatGroup
              .set(HasMadeSalesPage, true).success.value

          val expectedRegistration = RegistrationData.registration.copy(
            dateOfFirstSale = Some(arbitraryDate),
            vatDetails = RegistrationData.registration.vatDetails,
            commencementDate = arbitraryDate
          )

          val result = getRegistrationService.fromUserAnswers(userAnswers, vrn).futureValue

          result mustEqual Valid(expectedRegistration)
        }
      }
    }
  }
}
