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

import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import controllers.euDetails.{routes => euRoutes}
import controllers.previousRegistrations.{routes => previousRegRoutes}
import controllers.routes
import pages._
import models._
import pages.euDetails._
import pages.previousRegistrations._
import queries._

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => UserAnswers => Call = {
    case FirstAuthedPage                          => _ => routes.SellsGoodsFromNiController.onPageLoad(NormalMode)
    case SellsGoodsFromNiPage                     => sellsGoodsFromNiRoute
    case InControlOfMovingGoodsPage               => inControlOfMovingGoodsRoute
    case CheckVatNumberPage                       => checkVatNumberRoute
    case CheckVatDetailsPage                      => checkVatDetailsRoute
    case RegisteredCompanyNamePage                => registeredCompanyNameRoute
    case PartOfVatGroupPage                       => partOfVatGroupRoute
    case UkVatEffectiveDatePage                   => ukVatEffectiveDateRoute
    case BusinessAddressPage                      => _ => routes.HasTradingNameController.onPageLoad(NormalMode)
    case HasTradingNamePage                       => hasTradingNameRoute
    case TradingNamePage(_)                       => _ => routes.AddTradingNameController.onPageLoad(NormalMode)
    case AddTradingNamePage                       => addTradingNameRoute
    case DeleteTradingNamePage(_)                 => deleteTradingNameRoute
    case TaxRegisteredInEuPage                    => isEuTaxRegistered
    case EuCountryPage(index)                     => _ => euRoutes.VatRegisteredController.onPageLoad(NormalMode, index)
    case VatRegisteredPage(index)                 => vatRegisteredRoute(index)
    case EuVatNumberPage(index)                   => _ => euRoutes.HasFixedEstablishmentController.onPageLoad(NormalMode, index)
    case HasFixedEstablishmentPage(index)         => hasFixedEstablishmentRoute(index)
    case EuTaxReferencePage(index)                => _ => euRoutes.FixedEstablishmentTradingNameController.onPageLoad(NormalMode, index)
    case FixedEstablishmentTradingNamePage(index) => _ => euRoutes.FixedEstablishmentAddressController.onPageLoad(NormalMode, index)
    case FixedEstablishmentAddressPage(index)     => _ => euRoutes.CheckEuDetailsAnswersController.onPageLoad(index)
    case AddEuDetailsPage                         => addEuDetailsRoute
    case DeleteEuDetailsPage(_)                   => deleteEuVatDetailsRoute
    case CurrentlyRegisteredInEuPage              => currentlyRegisteredInEuRoute
    case CurrentCountryOfRegistrationPage         => _ => previousRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode)
    case CurrentlyRegisteredInCountryPage         => _ => previousRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode)
    case PreviouslyRegisteredPage                 => previouslyRegisteredRoute
    case PreviousEuCountryPage(index)             => _ => previousRegRoutes.PreviousEuVatNumberController.onPageLoad(NormalMode, index)
    case PreviousEuVatNumberPage(_)               => _ => previousRegRoutes.AddPreviousRegistrationController.onPageLoad(NormalMode)
    case AddPreviousRegistrationPage              => addPreviousRegistrationRoute
    case DeletePreviousRegistrationPage(_)        => deletePreviousRegistrationRoute
    case StartDatePage                            => _ => routes.HasWebsiteController.onPageLoad(NormalMode)
    case HasWebsitePage                           => hasWebsiteRoute
    case WebsitePage(_)                           => _ => routes.AddWebsiteController.onPageLoad(NormalMode)
    case AddWebsitePage                           => addWebsiteRoute
    case DeleteWebsitePage(_)                     => deleteWebsiteRoute
    case BusinessContactDetailsPage               => _ => routes.BankDetailsController.onPageLoad(NormalMode)
    case BankDetailsPage                          => _ => routes.CheckYourAnswersController.onPageLoad()
    case CheckYourAnswersPage                     => _ => routes.ApplicationCompleteController.onPageLoad()
    case _                                        => _ => routes.IndexController.onPageLoad()
  }

  private def sellsGoodsFromNiRoute(answers: UserAnswers): Call = answers.get(SellsGoodsFromNiPage) match {
    case Some(true)  => routes.InControlOfMovingGoodsController.onPageLoad(NormalMode)
    case Some(false) => routes.CannotRegisterForServiceController.onPageLoad()
    case None        => routes.JourneyRecoveryController.onPageLoad()
  }

  private def inControlOfMovingGoodsRoute(answers: UserAnswers): Call =
    (answers.get(InControlOfMovingGoodsPage), answers.vatInfo) match {
      case (Some(true), Some(_)) => routes.CheckVatDetailsController.onPageLoad(NormalMode)
      case (Some(true), None)    => routes.CheckVatNumberController.onPageLoad(NormalMode)
      case (Some(false), _)      => routes.CannotRegisterForServiceController.onPageLoad()
      case _                     => routes.JourneyRecoveryController.onPageLoad()
  }

  private def checkVatNumberRoute(answers: UserAnswers): Call = answers.get(CheckVatNumberPage) match {
    case Some(true)  => routes.RegisteredCompanyNameController.onPageLoad(NormalMode)
    case Some(false) => routes.UseOtherAccountController.onPageLoad()
    case None        => routes.JourneyRecoveryController.onPageLoad()
  }

  private def checkVatDetailsRoute(answers: UserAnswers): Call = {
    import CheckVatDetails._

    (answers.get(CheckVatDetailsPage), answers.vatInfo) match {
      case (Some(Yes), Some(vatInfo)) if vatInfo.organisationName.isDefined => registeredCompanyNameRoute(answers)
      case (Some(Yes), _)                                                   => routes.RegisteredCompanyNameController.onPageLoad(NormalMode)
      case (Some(WrongAccount), _)                                          => routes.UseOtherAccountController.onPageLoad()
      case (Some(DetailsIncorrect), _)                                      => routes.UpdateVatDetailsController.onPageLoad()
      case _                                                                => routes.JourneyRecoveryController.onPageLoad()
    }
  }

  private def registeredCompanyNameRoute(answers: UserAnswers): Call = answers.vatInfo match {
    case Some(vatInfo) if vatInfo.partOfVatGroup.isDefined => partOfVatGroupRoute(answers)
    case _                                                 => routes.PartOfVatGroupController.onPageLoad(NormalMode)
  }

  private def partOfVatGroupRoute(answers: UserAnswers): Call = answers.vatInfo match {
    case Some(vatInfo) if vatInfo.registrationDate.isDefined => ukVatEffectiveDateRoute(answers)
    case _                                                   => routes.UkVatEffectiveDateController.onPageLoad(NormalMode)
  }

  private def ukVatEffectiveDateRoute(answers: UserAnswers): Call =
    if (answers.vatInfo.isDefined) {
      routes.HasTradingNameController.onPageLoad(NormalMode)
    } else {
      routes.BusinessAddressController.onPageLoad(NormalMode)
    }

  private def hasTradingNameRoute(answers: UserAnswers): Call = answers.get(HasTradingNamePage) match {
    case Some(true)  => routes.TradingNameController.onPageLoad(NormalMode, Index(0))
    case Some(false) => euRoutes.TaxRegisteredInEuController.onPageLoad(NormalMode)
    case None        => routes.JourneyRecoveryController.onPageLoad()
  }

  private def addTradingNameRoute(answers: UserAnswers): Call =
    (answers.get(AddTradingNamePage), answers.get(DeriveNumberOfTradingNames)) match {
      case (Some(true), Some(size)) => routes.TradingNameController.onPageLoad(NormalMode, Index(size))
      case (Some(false), _)         => euRoutes.TaxRegisteredInEuController.onPageLoad(NormalMode)
      case _                        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def deleteTradingNameRoute(answers: UserAnswers): Call =
    answers.get(DeriveNumberOfTradingNames) match {
      case Some(n) if n > 0 => routes.AddTradingNameController.onPageLoad(NormalMode)
      case _                => routes.HasTradingNameController.onPageLoad(NormalMode)
    }

  private def isEuTaxRegistered(answers: UserAnswers): Call = answers.get(TaxRegisteredInEuPage) match {
    case Some(true)  => euRoutes.EuCountryController.onPageLoad(NormalMode, Index(0))
    case Some(false) => previousRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode)
    case None        => routes.JourneyRecoveryController.onPageLoad()
  }

  private def vatRegisteredRoute(index: Index)(answers: UserAnswers): Call =
    answers.get(VatRegisteredPage(index)) match {
      case Some(true)  => euRoutes.EuVatNumberController.onPageLoad(NormalMode, index)
      case Some(false) => euRoutes.HasFixedEstablishmentController.onPageLoad(NormalMode, index)
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def hasFixedEstablishmentRoute(index: Index)(answers: UserAnswers): Call =
    (answers.get(pages.euDetails.HasFixedEstablishmentPage(index)), answers.get(pages.euDetails.VatRegisteredPage(index))) match {
    case (Some(true), Some(true))  => euRoutes.FixedEstablishmentTradingNameController.onPageLoad(NormalMode, index)
    case (Some(true), Some(false)) => euRoutes.EuTaxReferenceController.onPageLoad(NormalMode, index)
    case (Some(false), _)          => euRoutes.CheckEuDetailsAnswersController.onPageLoad(index)
    case _                         => routes.JourneyRecoveryController.onPageLoad()
  }

  private def addEuDetailsRoute(answers: UserAnswers): Call = {

    def noRoute: Call = answers.get(DeriveNumberOfEuVatRegistrations) match {
      case Some(size) if size == 1 => routes.CurrentlyRegisteredInCountryController.onPageLoad(NormalMode)
      case Some(size) if size > 1  => routes.CurrentlyRegisteredInEuController.onPageLoad(NormalMode)
      case _                       => previousRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode)
    }

    (answers.get(AddEuDetailsPage), answers.get(DeriveNumberOfEuRegistrations)) match {
      case (Some(true), Some(size)) => euRoutes.EuCountryController.onPageLoad(NormalMode, Index(size))
      case (Some(false), _)         => noRoute
      case _                        => routes.JourneyRecoveryController.onPageLoad()
    }
  }

  private def currentlyRegisteredInEuRoute(answers: UserAnswers): Call =
    answers.get(CurrentlyRegisteredInEuPage) match {
      case Some(true)  => routes.CurrentCountryOfRegistrationController.onPageLoad(NormalMode)
      case Some(false) => previousRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode)
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def deleteEuVatDetailsRoute(answers: UserAnswers): Call =
    answers.get(DeriveNumberOfEuRegistrations) match {
      case Some(n) if n > 0 => euRoutes.AddEuDetailsController.onPageLoad(NormalMode)
      case _                => euRoutes.TaxRegisteredInEuController.onPageLoad(NormalMode)
    }

  private def previouslyRegisteredRoute(answers: UserAnswers): Call =
    answers.get(PreviouslyRegisteredPage) match {
      case Some(true)  => previousRegRoutes.PreviousEuCountryController.onPageLoad(NormalMode, Index(0))
      case Some(false) => routes.StartDateController.onPageLoad(NormalMode)
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def addPreviousRegistrationRoute(answers: UserAnswers): Call =
    (answers.get(AddPreviousRegistrationPage), answers.get(DeriveNumberOfPreviousRegistrations)) match {
      case (Some(true), Some(size)) => previousRegRoutes.PreviousEuCountryController.onPageLoad(NormalMode, Index(size))
      case (Some(false), _)         => routes.StartDateController.onPageLoad(NormalMode)
      case _                        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def deletePreviousRegistrationRoute(answers: UserAnswers): Call =
    answers.get(DeriveNumberOfPreviousRegistrations) match {
      case Some(n) if n > 0 => previousRegRoutes.AddPreviousRegistrationController.onPageLoad(NormalMode)
      case _                => previousRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode)
    }

  private def hasWebsiteRoute(answers: UserAnswers): Call = answers.get(HasWebsitePage) match {
    case Some(true)  => routes.WebsiteController.onPageLoad(NormalMode, Index(0))
    case Some(false) => routes.BusinessContactDetailsController.onPageLoad(NormalMode)
    case None        => routes.JourneyRecoveryController.onPageLoad()
  }

  private def addWebsiteRoute(answers: UserAnswers): Call =
    (answers.get(AddWebsitePage), answers.get(DeriveNumberOfWebsites)) match {
      case (Some(true), Some(size)) => routes.WebsiteController.onPageLoad(NormalMode, Index(size))
      case (Some(false), _)         => routes.BusinessContactDetailsController.onPageLoad(NormalMode)
      case _                        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def deleteWebsiteRoute(answers: UserAnswers): Call =
    answers.get(DeriveNumberOfWebsites) match {
      case Some(n) if n > 0 => routes.AddWebsiteController.onPageLoad(NormalMode)
      case _                => routes.WebsiteController.onPageLoad(NormalMode, Index(0))
    }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case HasTradingNamePage                       => hasTradingNameRoute(CheckMode)
    case AddEuDetailsPage                         => addEuVatDetailsRoute(CheckMode)
    case TaxRegisteredInEuPage                    => taxRegisteredInEuRoute(CheckMode)
    case EuCountryPage(index)                     => euCountryCheckRoute(index)
    case EuVatNumberPage(index)                   => euVatNumberCheckRoute(index)
    case HasFixedEstablishmentPage(index)         => hasFixedEstablishmentCheckRoute(index)
    case FixedEstablishmentTradingNamePage(index) => fixedEstablishmentTradingNameCheckRoute(index)
    case FixedEstablishmentAddressPage(index)     => _ => euRoutes.CheckEuDetailsAnswersController.onPageLoad(index)
    case _                                        => _ => routes.CheckYourAnswersController.onPageLoad()
  }

  private def hasTradingNameRoute(mode: Mode)(answers: UserAnswers): Call = answers.get(HasTradingNamePage) match {
    case Some(true)   => routes.TradingNameController.onPageLoad(mode, Index(0))
    case Some(false)  => routes.CheckYourAnswersController.onPageLoad()
    case None         => routes.JourneyRecoveryController.onPageLoad()
  }

  private def taxRegisteredInEuRoute(mode: Mode)(answers: UserAnswers): Call = answers.get(TaxRegisteredInEuPage) match {
    case Some(true)   => euRoutes.EuCountryController.onPageLoad(mode, Index(0))
    case Some(false)  => routes.CheckYourAnswersController.onPageLoad()
    case None         => routes.JourneyRecoveryController.onPageLoad()
  }

  private def addEuVatDetailsRoute(mode: Mode)(answers: UserAnswers): Call =
    (answers.get(AddEuDetailsPage), answers.get(DeriveNumberOfEuRegistrations)) match {
      case (Some(true), Some(size)) => euRoutes.EuCountryController.onPageLoad(mode, Index(size))
      case (Some(false), _ )        => routes.CheckYourAnswersController.onPageLoad()
      case (None, Some(n)) if n > 0 => euRoutes.AddEuDetailsController.onPageLoad(mode)
      case _                        => taxRegisteredInEuRoute(mode)(answers)
  }

  private def euCountryCheckRoute(index: Index)(answers: UserAnswers): Call =
    answers.get(pages.euDetails.EuVatNumberPage(index)) match {
      case Some(_) => euRoutes.CheckEuDetailsAnswersController.onPageLoad(index)
      case None    => euRoutes.EuVatNumberController.onPageLoad(CheckMode, index)
    }

  private def euVatNumberCheckRoute(index: Index)(answers: UserAnswers): Call =
    answers.get(pages.euDetails.HasFixedEstablishmentPage(index)) match {
      case Some(_) => euRoutes.CheckEuDetailsAnswersController.onPageLoad(index)
      case None    => euRoutes.HasFixedEstablishmentController.onPageLoad(CheckMode, index)
    }

  private def hasFixedEstablishmentCheckRoute(index: Index)(answers: UserAnswers): Call =
    (answers.get(pages.euDetails.HasFixedEstablishmentPage(index)), answers.get(pages.euDetails.FixedEstablishmentTradingNamePage(index))) match {
      case (Some(true), None) => euRoutes.FixedEstablishmentTradingNameController.onPageLoad(NormalMode, index)
      case _                  => euRoutes.CheckEuDetailsAnswersController.onPageLoad(index)
    }

  private def fixedEstablishmentTradingNameCheckRoute(index: Index)(answers: UserAnswers): Call =
    answers.get(pages.euDetails.FixedEstablishmentAddressPage(index)) match {
      case Some(_) => euRoutes.CheckEuDetailsAnswersController.onPageLoad(index)
      case None    => euRoutes.FixedEstablishmentAddressController.onPageLoad(CheckMode, index)
    }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode => normalRoutes(page)(userAnswers)
    case CheckMode  => checkRouteMap(page)(userAnswers)
  }
}
