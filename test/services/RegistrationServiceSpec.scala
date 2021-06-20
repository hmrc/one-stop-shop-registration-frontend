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

import base.SpecBase
import cats.data.NonEmptyChain
import cats.data.Validated.{Invalid, Valid}
import models._
import models.domain.VatDetailSource.UserEntered
import models.domain.{VatCustomerInfo, VatDetailSource, VatDetails}
import pages._
import pages.euDetails._
import pages.previousRegistrations.{PreviousEuCountryPage, PreviousEuVatNumberPage, PreviouslyRegisteredPage}
import queries.{AllEuDetailsRawQuery, AllPreviousRegistrationsRawQuery, AllTradingNames, AllWebsites}
import testutils.RegistrationData

import java.time.LocalDate

class RegistrationServiceSpec extends SpecBase {

  private val iban = RegistrationData.iban

  private val answers =
    UserAnswers("id")
      .set(SellsGoodsFromNiPage, true).success.value
      .set(InControlOfMovingGoodsPage, true).success.value
      .set(RegisteredCompanyNamePage, "foo").success.value
      .set(HasTradingNamePage, true).success.value
      .set(AllTradingNames, List("single", "double")).success.value
      .set(PartOfVatGroupPage, true).success.value
      .set(UkVatEffectiveDatePage, LocalDate.now()).success.value
      .set(BusinessAddressInUkPage, true).success.value
      .set(TaxRegisteredInEuPage, true).success.value
      .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
      .set(VatRegisteredPage(Index(0)), true).success.value
      .set(EuVatNumberPage(Index(0)), "FR123456789").success.value
      .set(HasFixedEstablishmentPage(Index(0)), false).success.value
      .set(EuCountryPage(Index(1)), Country("ES", "Spain")).success.value
      .set(VatRegisteredPage(Index(1)), true).success.value
      .set(EuVatNumberPage(Index(1)), "ES123456789").success.value
      .set(HasFixedEstablishmentPage(Index(1)), true).success.value
      .set(FixedEstablishmentTradingNamePage(Index(1)), "Spanish trading name").success.value
      .set(FixedEstablishmentAddressPage(Index(1)), InternationalAddress("Line 1", None, "Town", None, None, Country("ES", "Spain"))).success.value
      .set(EuCountryPage(Index(2)), Country("DE", "Germany")).success.value
      .set(VatRegisteredPage(Index(2)), false).success.value
      .set(HasFixedEstablishmentPage(Index(2)), true).success.value
      .set(EuTaxReferencePage(Index(2)), "DE123456789").success.value
      .set(FixedEstablishmentTradingNamePage(Index(2)), "German trading name").success.value
      .set(FixedEstablishmentAddressPage(Index(2)), InternationalAddress("Line 1", None, "Town", None, None, Country("DE", "Germany"))).success.value
      .set(EuCountryPage(Index(3)), Country("IE", "Ireland")).success.value
      .set(VatRegisteredPage(Index(3)), false).success.value
      .set(HasFixedEstablishmentPage(Index(3)), false).success.value
      .set(CommencementDatePage, LocalDate.now()).success.value
      .set(
        UkAddressPage,
        UkAddress("123 Street", Some("Street"), "City", Some("county"), "AA12 1AB")
      ).success.value
      .set(
        BusinessContactDetailsPage,
        BusinessContactDetails("Joe Bloggs", "01112223344", "email@email.com")).success.value
      .set(HasWebsitePage, true).success.value
      .set(AllWebsites, List("website1", "website2")).success.value
      .set(PreviouslyRegisteredPage, true).success.value
      .set(PreviousEuCountryPage(Index(0)), Country("DE", "Germany")).success.value
      .set(PreviousEuVatNumberPage(Index(0)), "DE123").success.value
      .set(BankDetailsPage, BankDetails("Account name", Some("12345678"), iban)).success.value

  private val registrationService = new RegistrationService()

  "fromUserAnswers" - {

    "must return a Registration when user answers are provided and the user manually entered all their VAT details" in {

      val registration = registrationService.fromUserAnswers(answers, vrn)

      val expectedRegistration = RegistrationData.registration copy (vatDetails = RegistrationData.registration.vatDetails.copy(source = UserEntered))

      registration mustEqual Valid(expectedRegistration)
    }

    "must return a Registration when user answers are provided and we have full VAT information on the user" in {

      val regDate = LocalDate.of(2000, 1, 1)
      val address = DesAddress("Line 1", None, None, None, None, Some("BB22 2BB"), "GB")
      val vatInfo = VatCustomerInfo(
        registrationDate = Some(regDate),
        address = address,
        partOfVatGroup = Some(true),
        organisationName = Some("bar")
      )

      val userAnswers =
        answers.copy(vatInfo = Some(vatInfo))
          .remove(UkVatEffectiveDatePage).success.value
          .remove(UkAddressPage).success.value
          .remove(PartOfVatGroupPage).success.value

      val registration = registrationService.fromUserAnswers(userAnswers, vrn)

      val expectedRegistration =
        RegistrationData.registration.copy(
          vatDetails = VatDetails(regDate, address, true, VatDetailSource.Etmp),
          registeredCompanyName = "bar"
        )

      registration mustEqual Valid(expectedRegistration)
    }

    "must return a Registration when no trading names, EU countries or websites were provided" in {

      val userAnswers =
        answers
          .set(HasTradingNamePage, false).success.value
          .remove(AllTradingNames).success.value
          .set(TaxRegisteredInEuPage, false).success.value
          .remove(AllEuDetailsRawQuery).success.value
          .set(HasWebsitePage, false).success.value
          .remove(AllWebsites).success.value

      val expectedRegistration =
        RegistrationData.registration copy(
          tradingNames = Seq.empty,
          euRegistrations = Seq.empty,
          vatDetails = RegistrationData.registration.vatDetails copy (source = UserEntered),
          websites = Seq.empty
        )

      val registration = registrationService.fromUserAnswers(userAnswers, vrn)
      registration mustEqual Valid(expectedRegistration)
    }

    "must return a registration when an International address is given" in {

      val address = InternationalAddress("line 1", None, "town", None, None, Country("FR", "France"))
      val userAnswers =
        answers
          .set(BusinessAddressInUkPage, false).success.value
          .set(InternationalAddressPage, address).success.value
          .remove(UkAddressPage).success.value

      val expectedRegistration =
        RegistrationData.registration copy (
          vatDetails = RegistrationData.registration.vatDetails copy(
            address = address,
            source = UserEntered
          )
          )

      val registration = registrationService.fromUserAnswers(userAnswers, vrn)
      registration mustEqual Valid(expectedRegistration)
    }

    "must return Invalid" - {

      "when Sells Goods From NI is false" in {

        val userAnswers = answers.set(SellsGoodsFromNiPage, false).success.value
        val result = registrationService.fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(NotSellingGoodsFromNiError))
      }

      "when Sells Goods From NI is missing" in {

        val userAnswers = answers.remove(SellsGoodsFromNiPage).success.value
        val result = registrationService.fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(SellsGoodsFromNiPage)))
      }

      "when In Control of Moving Goods is false" in {

        val userAnswers = answers.set(InControlOfMovingGoodsPage, false).success.value
        val result = registrationService.fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(NotInControlOfMovingGoodsError))
      }

      "when In Control of Moving Goods is missing" in {

        val userAnswers = answers.remove(InControlOfMovingGoodsPage).success.value
        val result = registrationService.fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(InControlOfMovingGoodsPage)))
      }

      "when Registered Company Name is missing" in {

        val userAnswers = answers.remove(RegisteredCompanyNamePage).success.value
        val result = registrationService.fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(RegisteredCompanyNamePage)))
      }

      "when Has Trading Name is missing" in {

        val userAnswers = answers.remove(HasTradingNamePage).success.value
        val result = registrationService.fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(HasTradingNamePage)))
      }

      "when Has Trading Name is true, but there are no trading names" in {

        val userAnswers = answers.remove(AllTradingNames).success.value
        val result = registrationService.fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(AllTradingNames)))
      }

      "when UK VAT Effective Date is missing" in {

        val userAnswers = answers.remove(UkVatEffectiveDatePage).success.value
        val result = registrationService.fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(UkVatEffectiveDatePage)))
      }

      "when Business Address in UK is missing" in {

        val userAnswers = answers.remove(BusinessAddressInUkPage).success.value
        val result = registrationService.fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(BusinessAddressInUkPage)))
      }

      "when Business Address in UK is true, but UK Address is missing" in {

        val userAnswers =
          answers
            .set(BusinessAddressInUkPage, true).success.value
            .remove(UkAddressPage).success.value

        val result = registrationService.fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(UkAddressPage)))
      }

      "when Business Address in UK is false, but International Address is missing" in {

        val userAnswers =
          answers
            .set(BusinessAddressInUkPage, false).success.value
            .remove(InternationalAddressPage).success.value

        val result = registrationService.fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(InternationalAddressPage)))
      }

      "when Part of VAT Group is missing" in {

        val userAnswers = answers.remove(PartOfVatGroupPage).success.value
        val result = registrationService.fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(PartOfVatGroupPage)))
      }

      "when Commencement Date is missing" in {

        val userAnswers = answers.remove(CommencementDatePage).success.value
        val result = registrationService.fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(CommencementDatePage)))
      }

      "when Contact Details are missing" in {

        val userAnswers = answers.remove(BusinessContactDetailsPage).success.value
        val result = registrationService.fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(BusinessContactDetailsPage)))
      }

      "when Bank Details are missing" in {

        val userAnswers = answers.remove(BankDetailsPage).success.value
        val result = registrationService.fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(BankDetailsPage)))
      }

      "when Has Website is missing" in {

        val userAnswers = answers.remove(HasWebsitePage).success.value
        val result = registrationService.fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(HasWebsitePage)))
      }

      "when Has Website is true, but there are no websites" in {

        val userAnswers =
          answers
            .set(HasWebsitePage, true).success.value
            .remove(AllWebsites).success.value

        val result = registrationService.fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(AllWebsites)))
      }

      "when Previously Registered has not been answered" in {

        val userAnswers = answers.remove(PreviouslyRegisteredPage).success.value
        val result = registrationService.fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(PreviouslyRegisteredPage)))
      }

      "when Previously Registered is true" - {

        "but there are no previous registrations" in {
          val userAnswers =
            answers
              .set(PreviouslyRegisteredPage, true).success.value
              .remove(AllPreviousRegistrationsRawQuery).success.value

          val result = registrationService.fromUserAnswers(userAnswers, vrn)

          result mustEqual Invalid(NonEmptyChain(DataMissingError(AllPreviousRegistrationsRawQuery)))
        }

        "but there is a previous registration without a country" in {
          val userAnswers = answers.remove(PreviousEuCountryPage(Index(0))).success.value
          val result = registrationService.fromUserAnswers(userAnswers, vrn)

          result mustEqual Invalid(NonEmptyChain(DataMissingError(PreviousEuCountryPage(Index(0)))))
        }

        "but there is a previous registration without a VAT number" in {
          val userAnswers = answers.remove(PreviousEuVatNumberPage(Index(0))).success.value
          val result = registrationService.fromUserAnswers(userAnswers, vrn)

          result mustEqual Invalid(NonEmptyChain(DataMissingError(PreviousEuVatNumberPage(Index(0)))))
        }
      }

      "when Tax Registered in EU is missing" - {

        val userAnswers = answers.remove(TaxRegisteredInEuPage).success.value
        val result = registrationService.fromUserAnswers(userAnswers, vrn)

        result mustEqual Invalid(NonEmptyChain(DataMissingError(TaxRegisteredInEuPage)))
      }

      "when Tax Registered in EU is true" - {

        "and there are no EU country details" in {

          val userAnswers =
            answers
              .set(TaxRegisteredInEuPage, true).success.value
              .remove(AllEuDetailsRawQuery).success.value

          val result = registrationService.fromUserAnswers(userAnswers, vrn)

          result mustEqual Invalid(NonEmptyChain(DataMissingError(AllEuDetailsRawQuery)))
        }

        "and there is a record with no country" in {
          val userAnswers = answers.remove(EuCountryPage(Index(0))).success.value
          val result = registrationService.fromUserAnswers(userAnswers, vrn)

          result mustEqual Invalid(NonEmptyChain(DataMissingError(EuCountryPage(Index(0)))))
        }

        "and there is a record with a country" - {

          "where Vat Registered is missing" in {
            val userAnswers = answers.remove(VatRegisteredPage(Index(2))).success.value
            val result = registrationService.fromUserAnswers(userAnswers, vrn)

            result mustEqual Invalid(NonEmptyChain(DataMissingError(VatRegisteredPage(Index(2)))))
          }

          "without a VAT registration" - {

            "which does not have an answer for Has Fixed Establishment" in {
              val userAnswers = answers.remove(HasFixedEstablishmentPage(Index(2))).success.value
              val result = registrationService.fromUserAnswers(userAnswers, vrn)

              result mustEqual Invalid(NonEmptyChain(DataMissingError(HasFixedEstablishmentPage(Index(2)))))
            }

            "and Has Fixed Establishment is true" - {

              "and it does not have an EU Tax identifier" in {
                val userAnswers = answers.remove(EuTaxReferencePage(Index(2))).success.value
                val result = registrationService.fromUserAnswers(userAnswers, vrn)

                result mustEqual Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(2)))))
              }

              "and it does not have a fixed establishment trading name" in {
                val userAnswers = answers.remove(FixedEstablishmentTradingNamePage(Index(2))).success.value
                val result = registrationService.fromUserAnswers(userAnswers, vrn)

                result mustEqual Invalid(NonEmptyChain(DataMissingError(FixedEstablishmentTradingNamePage(Index(2)))))
              }

              "and it does not have a fixed establishment address" in {
                val userAnswers = answers.remove(FixedEstablishmentAddressPage(Index(2))).success.value
                val result = registrationService.fromUserAnswers(userAnswers, vrn)

                result mustEqual Invalid(NonEmptyChain(DataMissingError(FixedEstablishmentAddressPage(Index(2)))))
              }
            }
          }

          "with a VAT registration" - {

            "with the VAT number missing" in {
              val userAnswers = answers.remove(EuVatNumberPage(Index(0))).success.value
              val result = registrationService.fromUserAnswers(userAnswers, vrn)

              result mustEqual Invalid(NonEmptyChain(DataMissingError(EuVatNumberPage(Index(0)))))
            }

            "with a VAT number" - {

              "which does not have an answer for Has Fixed Establishment" in {
                val userAnswers = answers.remove(HasFixedEstablishmentPage(Index(0))).success.value
                val result = registrationService.fromUserAnswers(userAnswers, vrn)

                result mustEqual Invalid(NonEmptyChain(DataMissingError(HasFixedEstablishmentPage(Index(0)))))
              }

              "and Has Fixed Establishment is true" - {

                "and it does not have a fixed establishment trading name" in {
                  val userAnswers = answers.remove(FixedEstablishmentTradingNamePage(Index(1))).success.value
                  val result = registrationService.fromUserAnswers(userAnswers, vrn)

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(FixedEstablishmentTradingNamePage(Index(1)))))
                }

                "and it does not have a fixed establishment address" in {
                  val userAnswers = answers.remove(FixedEstablishmentAddressPage(Index(1))).success.value
                  val result = registrationService.fromUserAnswers(userAnswers, vrn)

                  result mustEqual Invalid(NonEmptyChain(DataMissingError(FixedEstablishmentAddressPage(Index(1)))))
                }
              }
            }
          }
        }
      }
    }
  }
}