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

package navigation

import base.SpecBase
import controllers.euDetails.{routes => euRoutes}
import controllers.previousRegistrations.{routes => previousRegRoutes}
import controllers.routes
import models._
import models.euDetails.FixedEstablishmentAddress
import pages._
import pages.euDetails._
import pages.previousRegistrations._

import java.time.LocalDate

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  val index: Index = Index(0)

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from Index" - {

        "to Sells Goods from NI" in {

          navigator.nextPage(FirstAuthedPage, NormalMode, emptyUserAnswersWithVatInfo)
            .mustBe(routes.SellsGoodsFromNiController.onPageLoad(NormalMode))
        }
      }

      "must go from Sells Goods from NI" - {

        "to In Control of Moving Goods when the user answers yes" in {

          val answers = emptyUserAnswers.set(SellsGoodsFromNiPage, true).success.value
          navigator.nextPage(SellsGoodsFromNiPage,NormalMode, answers)
            .mustBe(routes.InControlOfMovingGoodsController.onPageLoad(NormalMode))
        }

        "to Cannot Register for Service when the user answers no" in {

          val answers = emptyUserAnswers.set(SellsGoodsFromNiPage, false).success.value
          navigator.nextPage(SellsGoodsFromNiPage,NormalMode, answers)
            .mustBe(routes.CannotRegisterForServiceController.onPageLoad())
        }
      }

      "must go from In Control Of Moving Goods" - {

        "to Check VAT Details when the user answers yes and we have some VAT details" in {

          val answers = emptyUserAnswersWithVatInfo.set(InControlOfMovingGoodsPage, true).success.value
          navigator.nextPage(InControlOfMovingGoodsPage,NormalMode, answers)
            .mustBe(routes.CheckVatDetailsController.onPageLoad(NormalMode))
        }

        "to Check VAT Number when the user answers yes and we don't have any VAT details" in {

          val answers = emptyUserAnswers.set(InControlOfMovingGoodsPage, true).success.value
          navigator.nextPage(InControlOfMovingGoodsPage,NormalMode, answers)
            .mustBe(routes.CheckVatNumberController.onPageLoad(NormalMode))
        }

        "to Cannot Register for Service when the user answers no" in {

          val answers = emptyUserAnswers.set(InControlOfMovingGoodsPage, false).success.value
          navigator.nextPage(InControlOfMovingGoodsPage,NormalMode, answers)
            .mustBe(routes.CannotRegisterForServiceController.onPageLoad())
        }
      }

      "must go from Check VAT number" - {

        "to Registered Company Name when the user answers yes" in {

          val answers = emptyUserAnswers.set(CheckVatNumberPage, true).success.value
          navigator.nextPage(CheckVatNumberPage, NormalMode, answers)
            .mustBe(routes.RegisteredCompanyNameController.onPageLoad(NormalMode))
        }

        "to Use Other Account when the user answers no" in {

          val answers = emptyUserAnswers.set(CheckVatNumberPage, false).success.value
          navigator.nextPage(CheckVatNumberPage, NormalMode, answers)
            .mustBe(routes.UseOtherAccountController.onPageLoad())
        }
      }

      "must go from Check VAT Details" - {

        "when the user answers yes" - {

          "and we have VAT details" - {

            "including all items" - {

              "to Has Trading Name" in {

                val answers = emptyUserAnswersWithVatInfo.set(CheckVatDetailsPage, CheckVatDetails.Yes).success.value

                navigator.nextPage(CheckVatDetailsPage, NormalMode, answers)
                  .mustBe(routes.HasTradingNameController.onPageLoad(NormalMode))
              }
            }

            "without the organisation name" - {

              "to Registered Company Name" in {

                val updatedVatInfo = vatCustomerInfo copy (organisationName = None)
                val answers =
                  emptyUserAnswersWithVatInfo.copy(vatInfo = Some(updatedVatInfo))
                    .set(CheckVatDetailsPage, CheckVatDetails.Yes).success.value

                navigator.nextPage(CheckVatDetailsPage, NormalMode, answers)
                  .mustBe(routes.RegisteredCompanyNameController.onPageLoad(NormalMode))
              }
            }

            "without Part of VAT Group" - {

              "to Part of VAT Group" in {

                val updatedVatInfo = vatCustomerInfo copy (partOfVatGroup = None)
                val answers =
                  emptyUserAnswersWithVatInfo.copy(vatInfo = Some(updatedVatInfo))
                    .set(CheckVatDetailsPage, CheckVatDetails.Yes).success.value

                navigator.nextPage(CheckVatDetailsPage, NormalMode, answers)
                  .mustBe(routes.PartOfVatGroupController.onPageLoad(NormalMode))
              }
            }

            "without VAT registration date" - {

              "to UK VAT Effective Date" in {

                val updatedVatInfo = vatCustomerInfo copy (registrationDate =  None)
                val answers =
                  emptyUserAnswersWithVatInfo.copy(vatInfo = Some(updatedVatInfo))
                    .set(CheckVatDetailsPage, CheckVatDetails.Yes).success.value

                navigator.nextPage(CheckVatDetailsPage, NormalMode, answers)
                  .mustBe(routes.UkVatEffectiveDateController.onPageLoad(NormalMode))
              }
            }
          }
        }

        "when the user answers Wrong Account" - {

          "to User Other Account" in {

            val answers = emptyUserAnswers.set(CheckVatDetailsPage, CheckVatDetails.WrongAccount).success.value
            navigator.nextPage(CheckVatDetailsPage, NormalMode, answers)
              .mustBe(routes.UseOtherAccountController.onPageLoad())
          }

          "when the user answers Details Incorrect" - {

            "to Update VAT Details" in {

              val answers = emptyUserAnswers.set(CheckVatDetailsPage, CheckVatDetails.DetailsIncorrect).success.value
              navigator.nextPage(CheckVatDetailsPage, NormalMode, answers)
                .mustBe(routes.UpdateVatDetailsController.onPageLoad())
            }
          }
        }
      }

      "must go from Registered Company Name" - {

        "when we don't have any VAT details" - {

          "to Part of VAT Group" in {

            navigator.nextPage(RegisteredCompanyNamePage, NormalMode, emptyUserAnswers)
              .mustBe(routes.PartOfVatGroupController.onPageLoad(NormalMode))
          }
        }

        "when we have VAT details" - {

          "that include all items" - {

            "to Has Trading Name" in {

              navigator.nextPage(RegisteredCompanyNamePage, NormalMode, emptyUserAnswersWithVatInfo)
                .mustBe(routes.HasTradingNameController.onPageLoad(NormalMode))
            }
          }

          "that do not include Part of VAT Group" - {

            "to Part of VAT Group" in {

              val updatedVatInfo = vatCustomerInfo copy (partOfVatGroup = None)
              val answers = emptyUserAnswersWithVatInfo.copy(vatInfo = Some(updatedVatInfo))

              navigator.nextPage(RegisteredCompanyNamePage, NormalMode, answers)
                .mustBe(routes.PartOfVatGroupController.onPageLoad(NormalMode))
            }
          }

          "that do not include registration date" - {

            "to UK VAT Effective Date" in {

              val updatedVatInfo = vatCustomerInfo copy (registrationDate =  None)
              val answers = emptyUserAnswersWithVatInfo.copy(vatInfo = Some(updatedVatInfo))

              navigator.nextPage(RegisteredCompanyNamePage, NormalMode, answers)
                .mustBe(routes.UkVatEffectiveDateController.onPageLoad(NormalMode))
            }
          }
        }
      }

      "must go from Part of VAT Group" - {

        "when we do not have VAT details" - {

          "to UK Effective Date" in {

            navigator.nextPage(PartOfVatGroupPage, NormalMode, emptyUserAnswers)
              .mustBe(routes.UkVatEffectiveDateController.onPageLoad(NormalMode))
          }
        }

        "when we have VAT details" - {

          "that include registered date" - {

            "to has Trading Name" in {

              navigator.nextPage(PartOfVatGroupPage, NormalMode, emptyUserAnswersWithVatInfo)
                .mustBe(routes.HasTradingNameController.onPageLoad(NormalMode))
            }
          }

          "that do not include registered date" - {

            "to UK VAT Effective Date" in {

              val updatedVatInfo = vatCustomerInfo copy (registrationDate =  None)
              val answers = emptyUserAnswersWithVatInfo.copy(vatInfo = Some(updatedVatInfo))

              navigator.nextPage(PartOfVatGroupPage, NormalMode, answers)
                .mustBe(routes.UkVatEffectiveDateController.onPageLoad(NormalMode))
            }
          }
        }
      }

      "must go from UK VAT Effective Date" - {

        "when we do not have VAT details" - {

          "to Business Address" in {

            navigator.nextPage(UkVatEffectiveDatePage, NormalMode, emptyUserAnswers)
              .mustBe(routes.BusinessAddressController.onPageLoad(NormalMode))
          }
        }

        "when we have VAT details" - {

          "to Has Trading Name" in {

            navigator.nextPage(UkVatEffectiveDatePage, NormalMode, emptyUserAnswersWithVatInfo)
              .mustBe(routes.HasTradingNameController.onPageLoad(NormalMode))
          }
        }
      }

      "must go from Business Address to Has Trading Name" in {

        navigator.nextPage(BusinessAddressPage, NormalMode, emptyUserAnswers)
          .mustBe(routes.HasTradingNameController.onPageLoad(NormalMode))
      }

      "must go from Has Trading Name" - {

        "to Trading Name when the user answers true" in {

          val answers = emptyUserAnswers.set(HasTradingNamePage, true).success.value

          navigator.nextPage(HasTradingNamePage, NormalMode, answers)
            .mustBe(routes.TradingNameController.onPageLoad(NormalMode, Index(0)))
        }

        "to Tax Registered in EU when the user answers false" in {

          val answers = emptyUserAnswers.set(HasTradingNamePage, false).success.value

          navigator.nextPage(HasTradingNamePage, NormalMode, answers)
            .mustBe(euRoutes.TaxRegisteredInEuController.onPageLoad(NormalMode))
        }
      }

      "must go from Trading Name to Add Trading Name" in {

        navigator.nextPage(TradingNamePage(Index(0)), NormalMode, emptyUserAnswers)
          .mustBe(routes.AddTradingNameController.onPageLoad(NormalMode))
      }

      "must go from Add Trading Name" - {

        "to Trading Name for the next index when the user answers yes" in {

          val answers =
            emptyUserAnswers
              .set(TradingNamePage(Index(0)), "foo").success.value
              .set(AddTradingNamePage, true).success.value

          navigator.nextPage(AddTradingNamePage, NormalMode, answers)
            .mustBe(routes.TradingNameController.onPageLoad(NormalMode, Index(1)))
        }

        "to Tax Registered in EU when the user answers no" in {

          val answers = emptyUserAnswers.set(AddTradingNamePage, false).success.value

          navigator.nextPage(AddTradingNamePage, NormalMode, answers)
            .mustBe(euRoutes.TaxRegisteredInEuController.onPageLoad(NormalMode))
        }
      }

      "must go from Delete Trading Name" - {

        "to Add Trading Name when there are still trading names left" in {

          val answers =
            emptyUserAnswers
              .set(TradingNamePage(Index(0)), "foo").success.value

          navigator.nextPage(DeleteTradingNamePage(Index(0)), NormalMode, answers)
            .mustBe(routes.AddTradingNameController.onPageLoad(NormalMode))
        }

        "to Has Trading Name when there are no trading names left" in {

          navigator.nextPage(DeleteTradingNamePage(Index(0)), NormalMode, emptyUserAnswers)
            .mustBe(routes.HasTradingNameController.onPageLoad(NormalMode))
        }
      }

      "must go from Tax Registered in EU" - {

        "to EU Country when the user answers true" in {

          val answers = emptyUserAnswers.set(TaxRegisteredInEuPage, true).success.value

          navigator.nextPage(TaxRegisteredInEuPage, NormalMode, answers)
            .mustBe(euRoutes.EuCountryController.onPageLoad(NormalMode, index))
        }

        "to Previously Registered when the user answers false" in {

          val answers = emptyUserAnswers.set(TaxRegisteredInEuPage, false).success.value

          navigator.nextPage(TaxRegisteredInEuPage, NormalMode, answers)
            .mustBe(previousRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode))
        }
      }

      "must go from EU Country to VAT Registered" in {

        navigator.nextPage(EuCountryPage(index), NormalMode, emptyUserAnswers)
          .mustBe(euRoutes.VatRegisteredController.onPageLoad(NormalMode, index))
      }

      "must go from VAT Registered" - {

        "to EU VAT Number when the answer is yes" in {

          val answers = emptyUserAnswers.set(VatRegisteredPage(index), true).success.value
          navigator.nextPage(VatRegisteredPage(index), NormalMode, answers)
            .mustBe(euRoutes.EuVatNumberController.onPageLoad(NormalMode, index))
        }

        "to Has Fixed Establishment when the answer is no" in {

          val answers = emptyUserAnswers.set(VatRegisteredPage(index), false).success.value
          navigator.nextPage(VatRegisteredPage(index), NormalMode, answers)
            .mustBe(euRoutes.HasFixedEstablishmentController.onPageLoad(NormalMode, index))
        }
      }

      "must go from EU VAT Number to Has Fixed Establishment" in {

        navigator.nextPage(EuVatNumberPage(index), NormalMode, emptyUserAnswers)
          .mustBe(euRoutes.HasFixedEstablishmentController.onPageLoad(NormalMode, index))
      }

      "must go from Has Fixed Establishment" - {

        "to Fixed Establishment Trading Name when the user answers yes and has given a VAT number" in {

          val answers =
            emptyUserAnswers
              .set(VatRegisteredPage(index), true).success.value
              .set(HasFixedEstablishmentPage(index), true).success.value

          navigator.nextPage(pages.euDetails.HasFixedEstablishmentPage(index), NormalMode, answers)
            .mustBe(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(NormalMode, index))
        }

        "to EU Tax Reference when the user answers yes and has not given a VAT number" in {

          val answers =
            emptyUserAnswers
              .set(VatRegisteredPage(index), false).success.value
              .set(HasFixedEstablishmentPage(index), true).success.value

          navigator.nextPage(pages.euDetails.HasFixedEstablishmentPage(index), NormalMode, answers)
            .mustBe(euRoutes.EuTaxReferenceController.onPageLoad(NormalMode, index))
        }

        "to Check EU VAT Details Answers Name when the user answers yes" in {

          val answers = emptyUserAnswers.set(pages.euDetails.HasFixedEstablishmentPage(index), false).success.value
          navigator.nextPage(pages.euDetails.HasFixedEstablishmentPage(index), NormalMode, answers)
            .mustBe(euRoutes.CheckEuDetailsAnswersController.onPageLoad(index))
        }
      }

      "must go from Fixed Establishment Trading Name to Fixed Establishment Address" in {

        navigator.nextPage(FixedEstablishmentTradingNamePage(index), NormalMode, emptyUserAnswers)
          .mustBe(euRoutes.FixedEstablishmentAddressController.onPageLoad(NormalMode, index))
      }

      "must go from Fixed Establishment Address to Check EU VAT Details Answers" in {

        navigator.nextPage(FixedEstablishmentAddressPage(index), NormalMode, emptyUserAnswers)
          .mustBe(euRoutes.CheckEuDetailsAnswersController.onPageLoad(index))
      }

      "must go from Add EU VAT Details" - {

        "to EU Country when the user answers true" in {

          val answers = emptyUserAnswers
            .set(AddEuDetailsPage, true).success.value
            .set(pages.euDetails.EuCountryPage(index), Country("FR", "France")).success.value
            .set(pages.euDetails.EuVatNumberPage(index), "FR123456789").success.value

          navigator.nextPage(AddEuDetailsPage, NormalMode, answers)
            .mustBe(euRoutes.EuCountryController.onPageLoad(NormalMode, Index(1)))
        }

        "to Currently Registered in EU when the user answers false" in {

          val answers = emptyUserAnswers.set(AddEuDetailsPage, false).success.value

          navigator.nextPage(AddEuDetailsPage, NormalMode, answers)
            .mustBe(routes.CurrentlyRegisteredInEuController.onPageLoad(NormalMode))
        }
      }

      "must go from Delete EU VAT Details" - {

        "to Add EU VAT Details when there are still some EU VAT details" in {

          val answers =
            emptyUserAnswers
              .set(pages.euDetails.EuCountryPage(index), Country("FR", "France")).success.value
              .set(pages.euDetails.EuVatNumberPage(index), "VAT Number").success.value

          navigator.nextPage(DeleteEuDetailsPage(index), NormalMode, answers)
            .mustBe(euRoutes.AddEuDetailsController.onPageLoad(NormalMode))
        }

        "to Tax Registered in EU when there are no EU VAT details left" in {

          navigator.nextPage(pages.euDetails.DeleteEuDetailsPage(index), NormalMode, emptyUserAnswers)
            .mustBe(euRoutes.TaxRegisteredInEuController.onPageLoad(NormalMode))
        }
      }

      "must go from Currently Registered in EU" - {

        "to Current Country of Registration when the user answers yes" in {

          val answers = emptyUserAnswers.set(CurrentlyRegisteredInEuPage, true).success.value
          navigator.nextPage(CurrentlyRegisteredInEuPage, NormalMode, answers)
            .mustBe(routes.CurrentCountryOfRegistrationController.onPageLoad(NormalMode))
        }

        "to Previously Registered when the user answers no" in {

          val answers = emptyUserAnswers.set(CurrentlyRegisteredInEuPage, false).success.value
          navigator.nextPage(CurrentlyRegisteredInEuPage, NormalMode, answers)
            .mustBe(previousRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode))
        }
      }

      "must go from Current Country of Registration to Previously Registered" in {

        navigator.nextPage(CurrentCountryOfRegistrationPage, NormalMode, emptyUserAnswers)
          .mustBe(previousRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode))
      }

      "must go from Previously Registered" - {

        "to Previous EU Country when the user answers yes" in {
          
          val answers = emptyUserAnswers.set(PreviouslyRegisteredPage, true).success.value
          navigator.nextPage(PreviouslyRegisteredPage, NormalMode, answers)
            .mustBe(previousRegRoutes.PreviousEuCountryController.onPageLoad(NormalMode, Index(0)))
        }
        
        "to Start Date when the user answers no" in {

          val answers = emptyUserAnswers.set(PreviouslyRegisteredPage, false).success.value
          navigator.nextPage(PreviouslyRegisteredPage, NormalMode, answers)
            .mustBe(routes.StartDateController.onPageLoad(NormalMode))
        }
      }
      
      "must go from Previous EU Country to Previous VAT Number" in {
        
        navigator.nextPage(PreviousEuCountryPage(index), NormalMode, emptyUserAnswers)
          .mustBe(previousRegRoutes.PreviousEuVatNumberController.onPageLoad(NormalMode, index))
      }
      
      "must go from Previous VAT Number to Add Previous Registration" in {
        
        navigator.nextPage(PreviousEuVatNumberPage(index), NormalMode, emptyUserAnswers)
          .mustBe(previousRegRoutes.AddPreviousRegistrationController.onPageLoad(NormalMode))
      }
      
      "must go from Add Previous Registration" - {
        
        "to Previous EU Country for the next index when the user answers yes" in {
          
          val answers =
            emptyUserAnswers
              .set(PreviousEuCountryPage(index), Country("FR", "France")).success.value
              .set(PreviousEuVatNumberPage(index), "foo").success.value
              .set(AddPreviousRegistrationPage, true).success.value

          navigator.nextPage(AddPreviousRegistrationPage, NormalMode, answers)
            .mustBe(previousRegRoutes.PreviousEuCountryController.onPageLoad(NormalMode, Index(1)))
        }

        "to Start Date when the user answers no" in {

          val answers =
            emptyUserAnswers
              .set(PreviousEuCountryPage(index), Country("FR", "France")).success.value
              .set(PreviousEuVatNumberPage(index), "foo").success.value
              .set(AddPreviousRegistrationPage, false).success.value

          navigator.nextPage(AddPreviousRegistrationPage, NormalMode, answers)
            .mustBe(routes.StartDateController.onPageLoad(NormalMode))
        }
      }

      "must go from Delete Previous Registration" - {

        "to Add Previous Registration when there are still some previous registrations" in {

          val answers =
            emptyUserAnswers
              .set(PreviousEuCountryPage(index), Country("FR", "France")).success.value
              .set(PreviousEuVatNumberPage(index), "foo").success.value

          navigator.nextPage(DeletePreviousRegistrationPage(index), NormalMode, answers)
            .mustBe(previousRegRoutes.AddPreviousRegistrationController.onPageLoad(NormalMode))
        }

        "to Previously Registered when there are no previous registrations left" in {

          navigator.nextPage(DeletePreviousRegistrationPage(index), NormalMode, emptyUserAnswers)
            .mustBe(previousRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode))
        }
      }

      "must go from Start Date" - {

        "to Has Website" in {

          navigator.nextPage(StartDatePage, NormalMode, emptyUserAnswers)
            .mustBe(routes.HasWebsiteController.onPageLoad(NormalMode))
        }
      }

      "must go from Has Website" - {

        "to Website when the user answers yes" in {

          val answers = emptyUserAnswers.set(HasWebsitePage, true).success.value
          navigator.nextPage(HasWebsitePage, NormalMode, answers)
            .mustBe(routes.WebsiteController.onPageLoad(NormalMode, index))
        }

        "to Business Contact when the user answers no" in {

          val answers = emptyUserAnswers.set(HasWebsitePage, false).success.value
          navigator.nextPage(HasWebsitePage, NormalMode, answers)
            .mustBe(routes.BusinessContactDetailsController.onPageLoad(NormalMode))
        }
      }

      "must go from Website to Add Website" in {

        navigator.nextPage(WebsitePage(Index(0)), NormalMode, emptyUserAnswers)
          .mustBe(routes.AddWebsiteController.onPageLoad(NormalMode))
      }

      "must go from Add Website" - {

        "to Website for the next index when the user answers yes" in {

          val answers =
            emptyUserAnswers
              .set(WebsitePage(Index(0)), "website").success.value
              .set(AddWebsitePage, true).success.value

          navigator.nextPage(AddWebsitePage, NormalMode, answers)
            .mustBe(routes.WebsiteController.onPageLoad(NormalMode, Index(1)))
        }

        "to Business Contact Details when the user answers no" in {

          val answers = emptyUserAnswers.set(AddWebsitePage, false).success.value

          navigator.nextPage(AddWebsitePage, NormalMode, answers)
            .mustBe(routes.BusinessContactDetailsController.onPageLoad(NormalMode))
        }
      }

      "must go from Delete Website" - {

        "to Website Name when there are still websites left" in {

          val answers =
            emptyUserAnswers
              .set(WebsitePage(Index(0)), "foo").success.value

          navigator.nextPage(DeleteWebsitePage(Index(0)), NormalMode, answers)
            .mustBe(routes.AddWebsiteController.onPageLoad(NormalMode))
        }

        "to Website with index 0 when there are no websites left" in {

          navigator.nextPage(DeleteWebsitePage(Index(0)), NormalMode, emptyUserAnswers)
            .mustBe(routes.WebsiteController.onPageLoad(NormalMode, Index(0)))
        }
      }

      "must go from Business Contact Details to Bank Details" in {

        navigator.nextPage(BusinessContactDetailsPage, NormalMode, emptyUserAnswers)
          .mustBe(routes.BankDetailsController.onPageLoad(NormalMode))
      }

      "must go from Bank Details to Check Your Answers" in {

        navigator.nextPage(BankDetailsPage, NormalMode, emptyUserAnswers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from Check Your Answers to Application Complete" in {

        navigator.nextPage(CheckYourAnswersPage, NormalMode, emptyUserAnswers)
          .mustBe(routes.ApplicationCompleteController.onPageLoad())
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad()
      }

      "must go from Registered Company Name page back to Check Your Answers page" in {

        val answers = emptyUserAnswers.set(RegisteredCompanyNamePage, "Company1").success.value

        navigator.nextPage(RegisteredCompanyNamePage, CheckMode, answers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from Has Trading Name" - {

        "to Trading Name when the user answers true" in {

          val answers = emptyUserAnswers.set(HasTradingNamePage, true).success.value

          navigator.nextPage(HasTradingNamePage, CheckMode, answers)
            .mustBe(routes.TradingNameController.onPageLoad(CheckMode, Index(0)))
        }

        "to Check Your Answers when the user answers false" in {

          val answers = emptyUserAnswers.set(HasTradingNamePage, false).success.value

          navigator.nextPage(HasTradingNamePage, CheckMode, answers)
            .mustBe(routes.CheckYourAnswersController.onPageLoad())
        }
      }

      "must go from Part Of A VAT Group page to Check Your Answers page" in {

        val answers = emptyUserAnswers.set(PartOfVatGroupPage, true ).success.value

        navigator.nextPage(PartOfVatGroupPage, CheckMode, answers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from UK VAT Effective Date page to Check Your Answers page" in {

        val answers = emptyUserAnswers.set(UkVatEffectiveDatePage, LocalDate.now()).success.value

        navigator.nextPage(UkVatEffectiveDatePage, CheckMode, answers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from Tax Registered In EU page" - {

        "to EU Country page if true" in {

          val answers = emptyUserAnswers.set(TaxRegisteredInEuPage, true).success.value

          navigator.nextPage(TaxRegisteredInEuPage, CheckMode, answers)
            .mustBe(euRoutes.EuCountryController.onPageLoad(CheckMode, index))
        }

        "to Check Your Answers if false" in {

          val answers = emptyUserAnswers.set(TaxRegisteredInEuPage, false).success.value

          navigator.nextPage(TaxRegisteredInEuPage, CheckMode, answers)
            .mustBe(routes.CheckYourAnswersController.onPageLoad())
        }
      }

      "must go from EU Country" - {

        "to Check EU VAT details when EU VAT number has been answered" in {

          val answers =
            emptyUserAnswers
              .set(pages.euDetails.EuVatNumberPage(index), "foo").success.value

          navigator.nextPage(pages.euDetails.EuCountryPage(index), CheckMode, answers)
            .mustBe(euRoutes.CheckEuDetailsAnswersController.onPageLoad(index))
        }

        "to EU VAT Number when that question has not been answered" in {

          navigator.nextPage(pages.euDetails.EuCountryPage(index), CheckMode, emptyUserAnswers)
            .mustBe(euRoutes.EuVatNumberController.onPageLoad(CheckMode, index))
        }
      }

      "must go from EU VAT Number page" - {

        "to Check EU VAT details when Has Fixed Establishment has already been answered" in {
          val answers = emptyUserAnswers.set(pages.euDetails.HasFixedEstablishmentPage(index), false).success.value

          navigator.nextPage(pages.euDetails.EuVatNumberPage(index), CheckMode, answers)
            .mustBe(euRoutes.CheckEuDetailsAnswersController.onPageLoad(index))
        }

        "to Has Fixed Establishment when that question has not been answered" in {

          navigator.nextPage(pages.euDetails.EuVatNumberPage(index), CheckMode, emptyUserAnswers)
            .mustBe(euRoutes.HasFixedEstablishmentController.onPageLoad(CheckMode, index))
        }
      }

      "must go from Has Fixed Establishment" - {

        "to Fixed Establishment Trading Name when the user answers yes and has not answered Fixed Establishment Trading Name" in {

          val answers = emptyUserAnswers.set(pages.euDetails.HasFixedEstablishmentPage(index), true).success.value
          navigator.nextPage(pages.euDetails.HasFixedEstablishmentPage(index), CheckMode, answers)
            .mustBe(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(NormalMode, index))
        }

        "to Check EU VAT details answers when the user answers yes and has already answered Fixed Establishment Trading Name" in {

          val answers =
            emptyUserAnswers
              .set(pages.euDetails.HasFixedEstablishmentPage(index), true).success.value
              .set(pages.euDetails.FixedEstablishmentTradingNamePage(index), "foo").success.value

          navigator.nextPage(pages.euDetails.HasFixedEstablishmentPage(index), CheckMode, answers)
            .mustBe(euRoutes.CheckEuDetailsAnswersController.onPageLoad(index))
        }

        "to Check EU VAT details answers when the user answers no" in {

          val answers = emptyUserAnswers.set(pages.euDetails.HasFixedEstablishmentPage(index), false).success.value

          navigator.nextPage(pages.euDetails.HasFixedEstablishmentPage(index), CheckMode, answers)
            .mustBe(euRoutes.CheckEuDetailsAnswersController.onPageLoad(index))
        }
      }

      "must go from Fixed Establishment Trading Name" - {

        "to Check EU VAT details when Fixed Establishment Address has been answered" in {

          val answers =
            emptyUserAnswers
              .set(pages.euDetails.FixedEstablishmentAddressPage(index), FixedEstablishmentAddress("line1", None, "town", None, None)).success.value

          navigator.nextPage(pages.euDetails.FixedEstablishmentTradingNamePage(index), CheckMode, answers)
            .mustBe(euRoutes.CheckEuDetailsAnswersController.onPageLoad(index))
        }

        "to Fixed Establishment Address when that question has not been answered" in {
          navigator.nextPage(pages.euDetails.FixedEstablishmentTradingNamePage(index), CheckMode, emptyUserAnswers)
            .mustBe(euRoutes.FixedEstablishmentAddressController.onPageLoad(CheckMode, index))
        }
      }

      "must go from Add EU VAT Details page" - {

        "to EU Country page if true" in {

          val answers = emptyUserAnswers.set(AddEuDetailsPage, true).success.value
            .set(pages.euDetails.EuCountryPage(index), Country("FR", "France")).success.value
            .set(pages.euDetails.EuVatNumberPage(index), "FR123456789").success.value

          navigator.nextPage(AddEuDetailsPage, CheckMode, answers)
            .mustBe(euRoutes.EuCountryController.onPageLoad(CheckMode, Index(1)))
        }

        "to Check Your Answers if false" in {

          val answers = emptyUserAnswers.set(AddEuDetailsPage, false).success.value

          navigator.nextPage(AddEuDetailsPage, CheckMode, answers)
            .mustBe(routes.CheckYourAnswersController.onPageLoad())
        }
      }

      "must go from Business Address page to Check your Answers page" in {

        val businessAddress = new UkAddress(
          "value 1",
          Some ("value 2"),
          "value 3",
          Some("test@test.com"),
          "value 4"
        )

        val answers = emptyUserAnswers.set(BusinessAddressPage, businessAddress).success.value

        navigator.nextPage(BusinessAddressPage, CheckMode, answers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }

      "must go from Business Contact Details page to Check your Answers page" in {

        val businessContactDetails = new BusinessContactDetails(
          "Name",
          "0111 1111111",
          "email@email.com"
        )

        val answers = emptyUserAnswers.set(BusinessContactDetailsPage, businessContactDetails).success.value

        navigator.nextPage(BusinessContactDetailsPage, CheckMode, answers)
          .mustBe(routes.CheckYourAnswersController.onPageLoad())
      }
    }
  }
}
