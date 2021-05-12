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
import controllers.routes
import pages._
import models._
import queries.DeriveNumberOfEuVatRegisteredCountries

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => UserAnswers => Call = {
    case RegisteredCompanyNamePage             => _ => routes.HasTradingNameController.onPageLoad(NormalMode)
    case HasTradingNamePage                    => hasTradingNameRoute
    case TradingNamePage                       => _ => routes.PartOfVatGroupController.onPageLoad(NormalMode)
    case PartOfVatGroupPage                    => _ => routes.UkVatNumberController.onPageLoad(NormalMode)
    case UkVatNumberPage                       => _ => routes.UkVatEffectiveDateController.onPageLoad(NormalMode)
    case UkVatEffectiveDatePage                => _ => routes.UkVatRegisteredPostcodeController.onPageLoad(NormalMode)
    case UkVatRegisteredPostcodePage           => _ => routes.VatRegisteredInEuController.onPageLoad(NormalMode)
    case VatRegisteredInEuPage                 => isEuVatRegistered
    case VatRegisteredEuMemberStatePage(index) => _ => routes.EuVatNumberController.onPageLoad(NormalMode, index)
    case EuVatNumberPage(_)                    => _ => routes.AddAdditionalEuVatDetailsController.onPageLoad(NormalMode)
    case AddAdditionalEuVatDetailsPage         => hasAdditionalEuVatDetails
    case DeleteEuVatDetailsPage(_)             => deleteEuVatDetailsRoute
    case BusinessAddressPage                   => _ => routes.BusinessContactDetailsController.onPageLoad(NormalMode)
    case BusinessContactDetailsPage            => _ => routes.CheckYourAnswersController.onPageLoad()
    case _                                     => _ => routes.IndexController.onPageLoad()
  }

  private def hasTradingNameRoute(answers: UserAnswers): Call = answers.get(HasTradingNamePage) match {
    case Some(true)  => routes.TradingNameController.onPageLoad(NormalMode)
    case Some(false) => routes.PartOfVatGroupController.onPageLoad(NormalMode)
    case None        => routes.JourneyRecoveryController.onPageLoad()
  }

  private def isEuVatRegistered(answers: UserAnswers): Call = answers.get(VatRegisteredInEuPage) match {
    case Some(true)  => routes.VatRegisteredEuMemberStateController.onPageLoad(NormalMode, Index(0))
    case Some(false) => routes.BusinessAddressController.onPageLoad(NormalMode)
    case None        => routes.JourneyRecoveryController.onPageLoad()
  }

  private def hasAdditionalEuVatDetails(answers: UserAnswers): Call =
    (answers.get(AddAdditionalEuVatDetailsPage), answers.get(DeriveNumberOfEuVatRegisteredCountries)) match {
      case (Some(true), Some(size)) => routes.VatRegisteredEuMemberStateController.onPageLoad(NormalMode, Index(size))
      case (Some(false), _) => routes.BusinessAddressController.onPageLoad(NormalMode)
      case _        => routes.JourneyRecoveryController.onPageLoad()
  }

  private def deleteEuVatDetailsRoute(answers: UserAnswers): Call =
    answers.get(DeriveNumberOfEuVatRegisteredCountries) match {
      case Some(n) if n > 0 => routes.AddAdditionalEuVatDetailsController.onPageLoad(NormalMode)
      case _                => routes.VatRegisteredInEuController.onPageLoad(NormalMode)
    }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case HasTradingNamePage => hasTradingNameRoute(CheckMode)
    case AddAdditionalEuVatDetailsPage => AddAdditionalEuVatDetailsRoute(CheckMode)
    case VatRegisteredInEuPage => vatRegisteredInEuRoute(CheckMode)
    case VatRegisteredEuMemberStatePage(index) => VatRegisteredEuMemberStateRoute(CheckMode, index)
    case EuVatNumberPage(index) => EuVatNumberRoute(CheckMode, index)
    case _ => _ => routes.CheckYourAnswersController.onPageLoad()
  }

  private def hasTradingNameRoute(mode: Mode)(answers: UserAnswers): Call = answers.get(HasTradingNamePage) match {
    case Some(true) => routes.TradingNameController.onPageLoad(mode)
    case Some(false) => routes.CheckYourAnswersController.onPageLoad()
  }

  private def vatRegisteredInEuRoute(mode: Mode)(answers: UserAnswers): Call = answers.get(VatRegisteredInEuPage) match {
    case Some(true) => routes.VatRegisteredEuMemberStateController.onPageLoad(mode, Index(0))
    case Some(false) => routes.CheckYourAnswersController.onPageLoad()
  }

  private def AddAdditionalEuVatDetailsRoute(mode: Mode)(answers: UserAnswers): Call =
    (answers.get(AddAdditionalEuVatDetailsPage), answers.get(DeriveNumberOfEuVatRegisteredCountries)) match {
      case (Some(true), Some(size)) => routes.VatRegisteredEuMemberStateController.onPageLoad(mode, Index(size))
      case (Some(false), _ ) => routes.CheckYourAnswersController.onPageLoad()
      case (Some(_), Some(n)) if n > 0 => routes.AddAdditionalEuVatDetailsController.onPageLoad(mode)
      case _                => vatRegisteredInEuRoute(mode)(answers)
  }

  private def VatRegisteredEuMemberStateRoute(mode: Mode, index: Index)(answers: UserAnswers): Call = answers.get(VatRegisteredEuMemberStatePage(index)) match {
    case _ => routes.EuVatNumberController.onPageLoad(mode, index)
  }

  private def EuVatNumberRoute(mode: Mode, index: Index)(answers: UserAnswers): Call = answers.get(EuVatNumberPage(index)) match {
    case _ => routes.AddAdditionalEuVatDetailsController.onPageLoad(mode)
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}
