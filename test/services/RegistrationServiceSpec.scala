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
import cats.data.NonEmptyChain
import cats.data.Validated.{Invalid, Valid}
import models._
import models.domain._
import models.euDetails.{EuConsumerSalesMethod, RegistrationType}
import models.previousRegistrations.PreviousSchemeNumbers
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages._
import pages.euDetails._
import pages.previousRegistrations.{PreviousEuCountryPage, PreviousOssNumberPage, PreviousSchemePage, PreviouslyRegisteredPage}
import queries.previousRegistration.AllPreviousRegistrationsRawQuery
import queries.{AllEuDetailsRawQuery, AllTradingNames, AllWebsites}
import testutils.RegistrationData

import java.time.{Clock, LocalDate, ZoneId}

class RegistrationServiceSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val iban = RegistrationData.iban
  private val bic = RegistrationData.bic

  private def getStubClock(date: LocalDate) =
    Clock.fixed(date.atStartOfDay(ZoneId.systemDefault()).toInstant, ZoneId.systemDefault())

  private def getDateService(date: LocalDate) = new DateService(getStubClock(date))
  
  private def getRegistrationService(today: LocalDate) =
    new RegistrationService(getDateService(today))

  private val hasTradingNamePage = HasTradingNamePage

  private val answersPartOfVatGroup =
    UserAnswers("id",
      vatInfo = Some(VatCustomerInfo(
        DesAddress("Line 1", None, None, None, None, Some("AA11 1AA"), "GB"),
        LocalDate.now,
        partOfVatGroup = true,
        "foo",
      Some(true)
    ))
    )
      .set(BusinessBasedInNiPage, true).success.value
      .set(DateOfFirstSalePage, arbitraryDate).success.value
      .set(hasTradingNamePage, true).success.value
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
      .set(BankDetailsPage, BankDetails("Account name", Some(bic), iban)).success.value
      .set(IsOnlineMarketplacePage, false).success.value
      .set(BusinessBasedInNiPage, true).success.value

  private val answersNotPartOfVatGroup =
    answersPartOfVatGroup.copy(vatInfo =
      Some(VatCustomerInfo(
        DesAddress("Line 1", None, None, None, None, Some("AA11 1AA"), "GB"),
        LocalDate.now,
        partOfVatGroup = false,
        "foo")
      )
    )
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


  "fromUserAnswers" - {

    "must return a Registration when user answers are provided and we have full VAT information on the user and is not part of vat group" in {

      val regDate = LocalDate.of(2000, 1, 1)
      val address = DesAddress("Line 1", None, None, None, None, Some("BB22 2BB"), "GB")
      val vatInfo = VatCustomerInfo(
        registrationDate = regDate,
        address = address,
        partOfVatGroup = false,
        organisationName = "bar",
        singleMarketIndicator = Some(true)
      )

      val userAnswers =
        answersNotPartOfVatGroup.copy(vatInfo = Some(vatInfo))

      val registration = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

      val expectedRegistration =
        RegistrationData.registration.copy(
          dateOfFirstSale = Some(arbitraryDate),
          vatDetails = VatDetails(regDate, address, false, VatDetailSource.Etmp),
          registeredCompanyName = "bar",
          commencementDate = getDateService(arbitraryDate).startDateBasedOnFirstSale(arbitraryDate)
        )

      registration mustEqual Valid(expectedRegistration)
    }

    "must return a Registration when user answers are provided and PreviouslyRegisteredPage is false and user is not part of vat group" in {

      val regDate = LocalDate.of(2000, 1, 1)
      val address = DesAddress("Line 1", None, None, None, None, Some("BB22 2BB"), "GB")
      val vatInfo = VatCustomerInfo(
        registrationDate = regDate,
        address = address,
        partOfVatGroup = false,
        organisationName = "bar",
        singleMarketIndicator = Some(true)
      )

      val userAnswers =
        answersNotPartOfVatGroup.copy(vatInfo = Some(vatInfo))
          .set(PreviouslyRegisteredPage, false).success.value

      val registration = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

      val expectedRegistration =
        RegistrationData.registration.copy(
          dateOfFirstSale = Some(arbitraryDate),
          vatDetails = VatDetails(regDate, address, false, VatDetailSource.Etmp),
          registeredCompanyName = "bar",
          commencementDate = getDateService(arbitraryDate).startDateBasedOnFirstSale(arbitraryDate),
          previousRegistrations = Seq.empty
        )

      registration mustEqual Valid(expectedRegistration)
    }

    "must return a Registration when no trading names, EU countries or websites were provided" in {

      val userAnswers =
        answersNotPartOfVatGroup
          .set(hasTradingNamePage, false).success.value
          .remove(AllTradingNames).success.value
          .set(TaxRegisteredInEuPage, false).success.value
          .remove(AllEuDetailsRawQuery).success.value
          .set(HasWebsitePage, false).success.value
          .remove(AllWebsites).success.value

      val expectedRegistration =
        RegistrationData.registration copy(
          dateOfFirstSale = Some(arbitraryDate),
          tradingNames = Seq.empty,
          euRegistrations = Seq.empty,
          vatDetails = RegistrationData.registration.vatDetails,
          websites = Seq.empty,
          commencementDate = getDateService(arbitraryDate).startDateBasedOnFirstSale(arbitraryDate)
        )

      val registration = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)
      registration mustEqual Valid(expectedRegistration)
    }

    "must return a Registration when user answers are provided and BusinessBasedInNi is false and HasFixedEstablishmentInNi is true" in {

      val regDate = LocalDate.of(2000, 1, 1)
      val address = DesAddress("Line 1", None, None, None, None, Some("BB22 2BB"), "GB")
      val vatInfo = VatCustomerInfo(
        registrationDate = regDate,
        address = address,
        partOfVatGroup = false,
        organisationName = "bar",
        singleMarketIndicator = Some(true)
      )

      val userAnswers =
        answersNotPartOfVatGroup.copy(vatInfo = Some(vatInfo))
          .set(BusinessBasedInNiPage, false).success.value
          .set(HasFixedEstablishmentInNiPage, true).success.value

      val registration = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

      val expectedRegistration =
        RegistrationData.registration.copy(
          dateOfFirstSale = Some(arbitraryDate),
          vatDetails = VatDetails(regDate, address, false, VatDetailSource.Etmp),
          registeredCompanyName = "bar",
          commencementDate = getDateService(arbitraryDate).startDateBasedOnFirstSale(arbitraryDate),
          niPresence = Some(FixedEstablishmentInNi)
        )

      registration mustEqual Valid(expectedRegistration)
    }

    "must return a Registration when user answers are provided and BusinessBasedInNi is false and HasFixedEstablishmentInNi is false and SalesChannels has been answered" in {

      val regDate = LocalDate.of(2000, 1, 1)
      val address = DesAddress("Line 1", None, None, None, None, Some("BB22 2BB"), "GB")
      val vatInfo = VatCustomerInfo(
        registrationDate = regDate,
        address = address,
        partOfVatGroup = false,
        organisationName = "bar",
        singleMarketIndicator = Some(true)
      )

      val userAnswers =
        answersNotPartOfVatGroup.copy(vatInfo = Some(vatInfo))
          .set(BusinessBasedInNiPage, false).success.value
          .set(HasFixedEstablishmentInNiPage, false).success.value
          .set(SalesChannelsPage, SalesChannels.OnlineMarketplaces).success.value

      val registration = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

      val expectedRegistration =
        RegistrationData.registration.copy(
          dateOfFirstSale = Some(arbitraryDate),
          vatDetails = VatDetails(regDate, address, false, VatDetailSource.Etmp),
          registeredCompanyName = "bar",
          commencementDate = getDateService(arbitraryDate).startDateBasedOnFirstSale(arbitraryDate),
          niPresence = Some(NoPresence(SalesChannels.OnlineMarketplaces))
        )

      registration mustEqual Valid(expectedRegistration)
    }

    "must return a registration when a user is part of a vat group and has eu registrations with vat numbers and no other details" in {
      val userAnswers =
        UserAnswers("id", vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
          .set(BusinessBasedInNiPage, true).success.value
          .set(DateOfFirstSalePage, arbitraryDate).success.value
          .set(hasTradingNamePage, true).success.value
          .set(AllTradingNames, List("single", "double")).success.value
          .set(TaxRegisteredInEuPage, true).success.value
          .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
          .set(SellsGoodsToEUConsumersPage(Index(0)), false).success.value
          .set(VatRegisteredPage(Index(0)), true).success.value
          .set(EuVatNumberPage(Index(0)), "FR123456789").success.value
          .set(
            BusinessContactDetailsPage,
            BusinessContactDetails("Joe Bloggs", "01112223344", "email@email.com")).success.value
          .set(HasWebsitePage, true).success.value
          .set(AllWebsites, List("website1", "website2")).success.value
          .set(PreviouslyRegisteredPage, false).success.value
          .set(BankDetailsPage, BankDetails("Account name", Some(bic), iban)).success.value
          .set(IsOnlineMarketplacePage, false).success.value
          .set(BusinessBasedInNiPage, true).success.value

      val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)
      val expectedRegistration = {
        RegistrationData.registration copy(
          dateOfFirstSale = Some(arbitraryDate),
          registeredCompanyName = "Company name",
          vatDetails = VatDetails(
            registrationDate = LocalDate.now(stubClockAtArbitraryDate),
            address = DesAddress("Line 1", None, None, None, None, Some("AA11 1AA"), "GB"),
            partOfVatGroup = true,
            source = VatDetailSource.Etmp
          ),
          commencementDate = getDateService(arbitraryDate).startDateBasedOnFirstSale(arbitraryDate),
          previousRegistrations = Seq.empty,
          euRegistrations = Seq(
            EuVatRegistration(Country("FR", "France"), "FR123456789")
          )
        )
      }
      result mustEqual Valid(expectedRegistration)

    }

    "must return a registration" - {

      "when Business Based in NI is missing" in {

        val userAnswers = answersNotPartOfVatGroup.remove(BusinessBasedInNiPage).success.value

        val expectedRegistration =
          RegistrationData.registration copy(
            dateOfFirstSale = Some(arbitraryDate),
            vatDetails = RegistrationData.registration.vatDetails,
            niPresence = None,
            commencementDate = getDateService(arbitraryDate).startDateBasedOnFirstSale(arbitraryDate)
          )

        val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

        result mustEqual Valid(expectedRegistration)
      }

      "when Business Based in NI is false and Has Fixed Establishment in NI is missing" in {

        val userAnswers =
          answersNotPartOfVatGroup
            .set(BusinessBasedInNiPage, false).success.value
            .remove(HasFixedEstablishmentInNiPage).success.value

        val expectedRegistration =
          RegistrationData.registration copy(
            dateOfFirstSale = Some(arbitraryDate),
            vatDetails = RegistrationData.registration.vatDetails,
            niPresence = None,
            commencementDate = getDateService(arbitraryDate).startDateBasedOnFirstSale(arbitraryDate)
          )

        val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

        result mustEqual Valid(expectedRegistration)
      }

      "when Has Fixed Establishment in NI is false and Sales Channels is missing" in {

        val userAnswers =
          answersNotPartOfVatGroup
            .set(BusinessBasedInNiPage, false).success.value
            .set(HasFixedEstablishmentInNiPage, false).success.value
            .remove(SalesChannelsPage).success.value

        val expectedRegistration =
          RegistrationData.registration copy(
            dateOfFirstSale = Some(arbitraryDate),
            vatDetails = RegistrationData.registration.vatDetails,
            niPresence = None,
            commencementDate = getDateService(arbitraryDate).startDateBasedOnFirstSale(arbitraryDate)
          )

        val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

        result mustEqual Valid(expectedRegistration)
      }

      "when Date Of First Sale is missing and Is Planning First Eligible Sale is true" in {

        val userAnswers = answersNotPartOfVatGroup
          .remove(DateOfFirstSalePage).success.value
          .set(IsPlanningFirstEligibleSalePage, true).success.value

        val expectedRegistration =
          RegistrationData.registration copy(
            vatDetails = RegistrationData.registration.vatDetails,
            dateOfFirstSale = None
          )

        val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

        result mustEqual Valid(expectedRegistration)
      }
    }

    "must return Invalid" - {

      "when Vat Customer Info is missing" in {

        val userAnswers = answersNotPartOfVatGroup.copy(vatInfo = None)
        val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(CheckVatDetailsPage), DataMissingError(CheckVatDetailsPage)))
      }

      "when Has Trading Name is missing" in {

        val userAnswers = answersNotPartOfVatGroup.remove(hasTradingNamePage).success.value
        val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

        result.isInvalid mustBe true
      }

      "when Has Trading Name is true, but there are no trading names" in {

        val userAnswers = answersNotPartOfVatGroup.remove(AllTradingNames).success.value
        val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(AllTradingNames)))
      }

      "when both Date of First Sale and Is Planning First Eligible Sale are missing" in {

        val userAnswers = answersNotPartOfVatGroup
          .remove(DateOfFirstSalePage).success.value
          .remove(IsPlanningFirstEligibleSalePage).success.value

        val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(IsPlanningFirstEligibleSalePage)))
      }

      "when Contact Details are missing" in {

        val userAnswers = answersNotPartOfVatGroup.remove(BusinessContactDetailsPage).success.value
        val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(BusinessContactDetailsPage)))
      }

      "when Bank Details are missing" in {

        val userAnswers = answersNotPartOfVatGroup.remove(BankDetailsPage).success.value
        val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(BankDetailsPage)))
      }

      "when Has Website is missing" in {

        val userAnswers = answersNotPartOfVatGroup.remove(HasWebsitePage).success.value
        val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(HasWebsitePage)))
      }

      "when Has Website is true, but there are no websites" in {

        val userAnswers =
          answersNotPartOfVatGroup
            .set(HasWebsitePage, true).success.value
            .remove(AllWebsites).success.value

        val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(AllWebsites)))
      }

      "when Is Online Marketplace is missing" in {

        val userAnswers = answersNotPartOfVatGroup.remove(IsOnlineMarketplacePage).success.value
        val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(IsOnlineMarketplacePage)))
      }

      "when Previously Registered has not been answered" in {

        val userAnswers = answersNotPartOfVatGroup.remove(PreviouslyRegisteredPage).success.value
        val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(PreviouslyRegisteredPage)))
      }

      "when Previously Registered is true" - {

        "but there are no previous registrations" in {

          val userAnswers =
            answersNotPartOfVatGroup
              .set(PreviouslyRegisteredPage, true).success.value
              .remove(AllPreviousRegistrationsRawQuery).success.value

          val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

          result mustEqual Invalid(NonEmptyChain(DataMissingError(AllPreviousRegistrationsRawQuery)))
        }

        "but there is a previous registration without a country" in {

          val userAnswers = answersNotPartOfVatGroup.remove(PreviousEuCountryPage(Index(0))).success.value
          val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

          result mustEqual Invalid(NonEmptyChain(DataMissingError(PreviousEuCountryPage(Index(0)))))
        }

        "but there is a previous registration without a VAT number" in {

          val userAnswers = answersNotPartOfVatGroup.remove(PreviousOssNumberPage(Index(0), Index(0))).success.value
          val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

          result mustEqual Invalid(NonEmptyChain(DataMissingError(PreviousOssNumberPage(Index(0), Index(0)))))
        }
      }

      "when Tax Registered in EU is missing" in {

        val userAnswers = answersNotPartOfVatGroup.remove(TaxRegisteredInEuPage).success.value
        val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(TaxRegisteredInEuPage)))
      }

      "when Tax Registered in EU is true" - {

        "and there are no EU country details" in {

          val userAnswers =
            answersNotPartOfVatGroup
              .set(TaxRegisteredInEuPage, true).success.value
              .remove(AllEuDetailsRawQuery).success.value

          val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

          result mustEqual Invalid(NonEmptyChain(DataMissingError(AllEuDetailsRawQuery)))
        }

        "and there is a record with no country" in {

          val userAnswers = answersNotPartOfVatGroup.remove(EuCountryPage(Index(0))).success.value
          val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

          result mustEqual Invalid(NonEmptyChain(DataMissingError(EuCountryPage(Index(0)))))
        }

        "and there is a record with a country" - {

          "when Part of VAT group is true" - {

            "when Sells Goods To EU Consumers is true" - {

              "when Sells Goods To EU Consumer Method is DispatchWarehouse" - {

                "when Registration Type is Vat Number" - {

                  "and Vat Number is missing" in {

                    val userAnswers = answersPartOfVatGroup.remove(EuVatNumberPage(Index(0))).success.value
                    val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                    result mustEqual Invalid(NonEmptyChain(DataMissingError(EuVatNumberPage(Index(0)))))

                  }

                }

                "when Registration Type is Tax Id" - {

                  "and TaxId is missing" in {

                    val userAnswers = answersPartOfVatGroup.remove(EuTaxReferencePage(Index(2))).success.value
                    val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                    result mustEqual Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(2)))))

                  }

                }

                "when Registration Type is missing" - {

                  val userAnswers = answersPartOfVatGroup.remove(RegistrationTypePage(Index(0))).success.value
                  val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(RegistrationTypePage(Index(0)))))

                }

                "when EU Send Goods Trading Name is missing" in {

                  val userAnswers = answersPartOfVatGroup.remove(EuSendGoodsTradingNamePage(Index(0))).success.value
                  val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(EuSendGoodsTradingNamePage(Index(0)))))

                }

                "when EU Send Goods Address is missing" in {

                  val userAnswers = answersPartOfVatGroup.remove(EuSendGoodsAddressPage(Index(0))).success.value
                  val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(EuSendGoodsAddressPage(Index(0)))))

                }

              }

              "when Sells Goods To EU Consumer Method is missing" - {

                val userAnswers = answersPartOfVatGroup.remove(SellsGoodsToEUConsumerMethodPage(Index(0))).success.value
                val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                result mustEqual Invalid(NonEmptyChain(DataMissingError(SellsGoodsToEUConsumerMethodPage(Index(0)))))

              }

            }

            "when Sells Goods To EU Consumers is false" - {

              "when Vat Registered is true" - {

                "and EU Vat Number is missing" in {

                  val userAnswers = answersPartOfVatGroup.remove(EuVatNumberPage(Index(1))).success.value
                  val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(EuVatNumberPage(Index(1)))))

                }
              }

              "when Vat Registered is missing" in {

                val userAnswers = answersPartOfVatGroup.remove(VatRegisteredPage(Index(1))).success.value
                val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                result mustEqual Invalid(NonEmptyChain(DataMissingError(VatRegisteredPage(Index(1)))))

              }

            }

            "when Sells Goods To EU Consumers is missing" in {

              val userAnswers = answersPartOfVatGroup.remove(SellsGoodsToEUConsumersPage(Index(0))).success.value
              val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

              result mustEqual Invalid(NonEmptyChain(DataMissingError(SellsGoodsToEUConsumersPage(Index(0)))))

            }

          }

          "when Part of VAT group is false" - {

            "when Sells Goods To EU Consumers is true" - {

              "when Sells Goods To EU Consumer Method is DispatchWarehouse" - {

                "when Registration Type is Vat Number" - {

                  "and Vat Number is missing" in {

                    val userAnswers = answersNotPartOfVatGroup.remove(EuVatNumberPage(Index(0))).success.value
                    val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                    result mustEqual Invalid(NonEmptyChain(DataMissingError(EuVatNumberPage(Index(0)))))

                  }

                }

                "when Registration Type is Tax Id" - {

                  "and TaxId is missing" in {

                    val userAnswers = answersNotPartOfVatGroup.remove(EuTaxReferencePage(Index(2))).success.value
                    val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                    result mustEqual Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(2)))))

                  }

                }

                "when Registration Type is missing" - {

                  val userAnswers = answersNotPartOfVatGroup.remove(RegistrationTypePage(Index(0))).success.value
                  val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(RegistrationTypePage(Index(0)))))

                }

                "when EU Send Goods Trading Name is missing" in {

                  val userAnswers = answersNotPartOfVatGroup.remove(EuSendGoodsTradingNamePage(Index(0))).success.value
                  val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(EuSendGoodsTradingNamePage(Index(0)))))

                }

                "when EU Send Goods Address is missing" in {

                  val userAnswers = answersNotPartOfVatGroup.remove(EuSendGoodsAddressPage(Index(0))).success.value
                  val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(EuSendGoodsAddressPage(Index(0)))))

                }

              }

              "when Sells Goods To EU Consumer Method is Fixed Establishment" - {

                "when Registration Type is Vat Number" - {

                  "and Vat Number is missing" in {

                    val userAnswers = answersNotPartOfVatGroup.remove(EuVatNumberPage(Index(4))).success.value
                    val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                    result mustEqual Invalid(NonEmptyChain(DataMissingError(EuVatNumberPage(Index(4)))))

                  }

                }

                "when Registration Type is Tax Id" - {

                  "and TaxId is missing" in {

                    val userAnswers = answersNotPartOfVatGroup.remove(EuTaxReferencePage(Index(5))).success.value
                    val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                    result mustEqual Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(5)))))

                  }

                }

                "when Registration Type is missing" - {

                  val userAnswers = answersNotPartOfVatGroup.remove(RegistrationTypePage(Index(4))).success.value
                  val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(RegistrationTypePage(Index(4)))))

                }

                "when Fixed Establishment Trading Name is missing" in {

                  val userAnswers = answersNotPartOfVatGroup.remove(FixedEstablishmentTradingNamePage(Index(4))).success.value
                  val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(FixedEstablishmentTradingNamePage(Index(4)))))

                }

                "when Fixed Establishment Address is missing" in {

                  val userAnswers = answersNotPartOfVatGroup.remove(FixedEstablishmentAddressPage(Index(4))).success.value
                  val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(FixedEstablishmentAddressPage(Index(4)))))

                }

              }

              "when Sells Goods To EU Consumer Method is missing" - {

                val userAnswers = answersNotPartOfVatGroup.remove(SellsGoodsToEUConsumerMethodPage(Index(0))).success.value
                val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                result mustEqual Invalid(NonEmptyChain(DataMissingError(SellsGoodsToEUConsumerMethodPage(Index(0)))))

              }

            }

            "when Sells Goods To EU Consumers is false" - {

              "when Vat Registered is true" - {

                "and EU Vat Number is missing" in {

                  val userAnswers = answersNotPartOfVatGroup.remove(EuVatNumberPage(Index(1))).success.value
                  val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(EuVatNumberPage(Index(1)))))

                }
              }

              "when Vat Registered is missing" in {

                val userAnswers = answersNotPartOfVatGroup.remove(VatRegisteredPage(Index(1))).success.value
                val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

                result mustEqual Invalid(NonEmptyChain(DataMissingError(VatRegisteredPage(Index(1)))))

              }

            }

            "when Sells Goods To EU Consumers is missing" in {

              val userAnswers = answersNotPartOfVatGroup.remove(SellsGoodsToEUConsumersPage(Index(0))).success.value
              val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

              result mustEqual Invalid(NonEmptyChain(DataMissingError(SellsGoodsToEUConsumersPage(Index(0)))))

            }

          }
        }
      }

      "when Tax Registered has not been answered" in {

        val userAnswers =
          answersNotPartOfVatGroup
            .remove(TaxRegisteredInEuPage).success.value

        val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(TaxRegisteredInEuPage)))

      }

    }

    "date of first sales logic" - {

      "should return Valid" - {

        "when Has Made Sales is Yes and Date of First Sale is populated" in {
          val userAnswers =
            answersNotPartOfVatGroup
              .set(HasMadeSalesPage, true).success.value

          val expectedRegistration = RegistrationData.registration.copy(
            dateOfFirstSale = Some(arbitraryDate),
            vatDetails = RegistrationData.registration.vatDetails,
            commencementDate = getDateService(arbitraryDate).startDateBasedOnFirstSale(arbitraryDate)
          )

          val result = getRegistrationService(arbitraryDate).fromUserAnswers(userAnswers, vrn)

          result mustEqual Valid(expectedRegistration)
        }
      }
    }
  }
}