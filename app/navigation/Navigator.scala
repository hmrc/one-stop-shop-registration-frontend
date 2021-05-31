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
import controllers.euVatDetails.{routes => euVatRoutes}
import controllers.routes
import pages._
import models._
import pages.euVatDetails._
import queries._

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => UserAnswers => Call = {
    case CheckVatDetailsPage                      => checkVatDetailsRoute
    case RegisteredCompanyNamePage                => _ => routes.HasTradingNameController.onPageLoad(NormalMode)
    case HasTradingNamePage                       => hasTradingNameRoute
    case TradingNamePage(_)                       => _ => routes.AddTradingNameController.onPageLoad(NormalMode)
    case AddTradingNamePage                       => addTradingNameRoute
    case DeleteTradingNamePage(_)                 => deleteTradingNameRoute
    case PartOfVatGroupPage                       => _ => routes.UkVatEffectiveDateController.onPageLoad(NormalMode)
    case UkVatEffectiveDatePage                   => _ => routes.UkVatRegisteredPostcodeController.onPageLoad(NormalMode)
    case UkVatRegisteredPostcodePage              => _ => euVatRoutes.VatRegisteredInEuController.onPageLoad(NormalMode)
    case VatRegisteredInEuPage                    => isEuVatRegistered
    case EuCountryPage(index)                     => _ => euVatRoutes.EuVatNumberController.onPageLoad(NormalMode, index)
    case EuVatNumberPage(index)                   => _ => euVatRoutes.HasFixedEstablishmentController.onPageLoad(NormalMode, index)
    case HasFixedEstablishmentPage(index)         => hasFixedEstablishmentRoute(index)
    case FixedEstablishmentTradingNamePage(index) => _ => euVatRoutes.FixedEstablishmentAddressController.onPageLoad(NormalMode, index)
    case FixedEstablishmentAddressPage(index)     => _ => euVatRoutes.CheckEuVatDetailsAnswersController.onPageLoad(index)
    case AddEuVatDetailsPage                      => addEuVatDetailsRoute
    case DeleteEuVatDetailsPage(_)                => deleteEuVatDetailsRoute
    case StartDatePage                            => _ => routes.BusinessAddressController.onPageLoad(NormalMode)
    case BusinessAddressPage                      => _ => routes.WebsiteController.onPageLoad(NormalMode, Index(0))
    case WebsitePage(_)                           => _ => routes.AddWebsiteController.onPageLoad(NormalMode)
    case AddWebsitePage                           => addWebsiteRoute
    case DeleteWebsitePage(_)                     => deleteWebsiteRoute
    case BusinessContactDetailsPage               => _ => routes.CheckYourAnswersController.onPageLoad()
    case CheckYourAnswersPage                     => _ => routes.ApplicationCompleteController.onPageLoad()
    case _                                        => _ => routes.IndexController.onPageLoad()
  }

  private def checkVatDetailsRoute(answers: UserAnswers): Call = answers.get(CheckVatDetailsPage) match {
    case Some(true)  => routes.RegisteredCompanyNameController.onPageLoad(NormalMode)
    case Some(false) => routes.UseOtherAccountController.onPageLoad()
    case None        => routes.JourneyRecoveryController.onPageLoad()
  }

  private def hasTradingNameRoute(answers: UserAnswers): Call = answers.get(HasTradingNamePage) match {
    case Some(true)  => routes.TradingNameController.onPageLoad(NormalMode, Index(0))
    case Some(false) => routes.PartOfVatGroupController.onPageLoad(NormalMode)
    case None        => routes.JourneyRecoveryController.onPageLoad()
  }

  private def addTradingNameRoute(answers: UserAnswers): Call =
    (answers.get(AddTradingNamePage), answers.get(DeriveNumberOfTradingNames)) match {
      case (Some(true), Some(size)) => routes.TradingNameController.onPageLoad(NormalMode, Index(size))
      case (Some(false), _)         => routes.PartOfVatGroupController.onPageLoad(NormalMode)
      case _                        => routes.JourneyRecoveryController.onPageLoad()
    }

  private def deleteTradingNameRoute(answers: UserAnswers): Call =
    answers.get(DeriveNumberOfTradingNames) match {
      case Some(n) if n > 0 => routes.AddTradingNameController.onPageLoad(NormalMode)
      case _                => routes.HasTradingNameController.onPageLoad(NormalMode)
    }

  private def isEuVatRegistered(answers: UserAnswers): Call = answers.get(VatRegisteredInEuPage) match {
    case Some(true)  => euVatRoutes.EuCountryController.onPageLoad(NormalMode, Index(0))
    case Some(false) => routes.StartDateController.onPageLoad(NormalMode)
    case None        => routes.JourneyRecoveryController.onPageLoad()
  }

  private def hasFixedEstablishmentRoute(index: Index)(answers: UserAnswers): Call = answers.get(pages.euVatDetails.HasFixedEstablishmentPage(index)) match {
    case Some(true)  => euVatRoutes.FixedEstablishmentTradingNameController.onPageLoad(NormalMode, index)
    case Some(false) => euVatRoutes.CheckEuVatDetailsAnswersController.onPageLoad(index)
    case None        => routes.JourneyRecoveryController.onPageLoad()
  }

  private def addEuVatDetailsRoute(answers: UserAnswers): Call =
    (answers.get(AddEuVatDetailsPage), answers.get(DeriveNumberOfEuVatRegisteredCountries)) match {
      case (Some(true), Some(size)) => euVatRoutes.EuCountryController.onPageLoad(NormalMode, Index(size))
      case (Some(false), _)         => routes.StartDateController.onPageLoad(NormalMode)
      case _                        => routes.JourneyRecoveryController.onPageLoad()
  }

  private def deleteEuVatDetailsRoute(answers: UserAnswers): Call =
    answers.get(DeriveNumberOfEuVatRegisteredCountries) match {
      case Some(n) if n > 0 => euVatRoutes.AddEuVatDetailsController.onPageLoad(NormalMode)
      case _                => euVatRoutes.VatRegisteredInEuController.onPageLoad(NormalMode)
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
    case AddEuVatDetailsPage                      => addEuVatDetailsRoute(CheckMode)
    case VatRegisteredInEuPage                    => vatRegisteredInEuRoute(CheckMode)
    case EuCountryPage(index)                     => euCountryCheckRoute(index)
    case EuVatNumberPage(index)                   => euVatNumberCheckRoute(index)
    case HasFixedEstablishmentPage(index)         => hasFixedEstablishmentCheckRoute(index)
    case FixedEstablishmentTradingNamePage(index) => fixedEstablishmentTradingNameCheckRoute(index)
    case FixedEstablishmentAddressPage(index)     => _ => euVatRoutes.CheckEuVatDetailsAnswersController.onPageLoad(index)
    case _                                        => _ => routes.CheckYourAnswersController.onPageLoad()
  }

  private def hasTradingNameRoute(mode: Mode)(answers: UserAnswers): Call = answers.get(HasTradingNamePage) match {
    case Some(true)   => routes.TradingNameController.onPageLoad(mode, Index(0))
    case Some(false)  => routes.CheckYourAnswersController.onPageLoad()
    case None         => routes.JourneyRecoveryController.onPageLoad()
  }

  private def vatRegisteredInEuRoute(mode: Mode)(answers: UserAnswers): Call = answers.get(VatRegisteredInEuPage) match {
    case Some(true)   => euVatRoutes.EuCountryController.onPageLoad(mode, Index(0))
    case Some(false)  => routes.CheckYourAnswersController.onPageLoad()
    case None         => routes.JourneyRecoveryController.onPageLoad()
  }

  private def addEuVatDetailsRoute(mode: Mode)(answers: UserAnswers): Call =
    (answers.get(AddEuVatDetailsPage), answers.get(DeriveNumberOfEuVatRegisteredCountries)) match {
      case (Some(true), Some(size)) => euVatRoutes.EuCountryController.onPageLoad(mode, Index(size))
      case (Some(false), _ )        => routes.CheckYourAnswersController.onPageLoad()
      case (None, Some(n)) if n > 0 => euVatRoutes.AddEuVatDetailsController.onPageLoad(mode)
      case _                        => vatRegisteredInEuRoute(mode)(answers)
  }

  private def euCountryCheckRoute(index: Index)(answers: UserAnswers): Call =
    answers.get(pages.euVatDetails.EuVatNumberPage(index)) match {
      case Some(_) => euVatRoutes.CheckEuVatDetailsAnswersController.onPageLoad(index)
      case None    => euVatRoutes.EuVatNumberController.onPageLoad(CheckMode, index)
    }

  private def euVatNumberCheckRoute(index: Index)(answers: UserAnswers): Call =
    answers.get(pages.euVatDetails.HasFixedEstablishmentPage(index)) match {
      case Some(_) => euVatRoutes.CheckEuVatDetailsAnswersController.onPageLoad(index)
      case None    => euVatRoutes.HasFixedEstablishmentController.onPageLoad(CheckMode, index)
    }

  private def hasFixedEstablishmentCheckRoute(index: Index)(answers: UserAnswers): Call =
    (answers.get(pages.euVatDetails.HasFixedEstablishmentPage(index)), answers.get(pages.euVatDetails.FixedEstablishmentTradingNamePage(index))) match {
      case (Some(true), None) => euVatRoutes.FixedEstablishmentTradingNameController.onPageLoad(NormalMode, index)
      case _                  => euVatRoutes.CheckEuVatDetailsAnswersController.onPageLoad(index)
    }

  private def fixedEstablishmentTradingNameCheckRoute(index: Index)(answers: UserAnswers): Call =
    answers.get(pages.euVatDetails.FixedEstablishmentAddressPage(index)) match {
      case Some(_) => euVatRoutes.CheckEuVatDetailsAnswersController.onPageLoad(index)
      case None    => euVatRoutes.FixedEstablishmentAddressController.onPageLoad(CheckMode, index)
    }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode => normalRoutes(page)(userAnswers)
    case CheckMode  => checkRouteMap(page)(userAnswers)
  }
}
