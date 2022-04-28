/*
 * Copyright 2022 HM Revenue & Customs
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

package utils

import models.{CheckMode, Country, Index}
import models.euDetails.EuOptionalDetails
import models.previousRegistrations.PreviousRegistrationDetailsWithOptionalVatNumber
import models.requests.AuthenticatedDataRequest
import pages.euDetails.TaxRegisteredInEuPage
import pages.previousRegistrations.PreviouslyRegisteredPage
import pages.{DateOfFirstSalePage, HasMadeSalesPage, HasTradingNamePage, HasWebsitePage, IsPlanningFirstEligibleSalePage}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import queries.{AllEuOptionalDetailsQuery, AllPreviousRegistrationsWithOptionalVatNumberQuery, AllTradingNames, AllWebsites, EuOptionalDetailsQuery}

import scala.concurrent.Future

trait CompletionChecks {


  protected def withCompleteDataModel[A](index: Index, data: Index => Option[A], onFailure: Option[A] => Result)
                                        (onSuccess: => Result)
                                        (implicit request: AuthenticatedDataRequest[AnyContent]): Result = {

    val incomplete = data(index)
    if (incomplete.isEmpty) {
      onSuccess
    } else {
      onFailure(incomplete)
    }
  }

  protected def withCompleteDataAsync[A](data: () => Seq[A], onFailure: Seq[A] => Future[Result])
                                        (onSuccess: => Future[Result])
                                        (implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] = {

    val incomplete = data()
    if (incomplete.isEmpty) {
      onSuccess
    } else {
      onFailure(incomplete)
    }
  }


  def getIncompleteEuDetails(index: Index)(implicit request: AuthenticatedDataRequest[AnyContent]): Option[EuOptionalDetails] = {
    request.userAnswers
      .get(EuOptionalDetailsQuery(index))
      .find(details =>
        details.vatRegistered.isEmpty ||
          details.hasFixedEstablishment.isEmpty ||
          (details.vatRegistered.contains(true) && details.euVatNumber.isEmpty) ||
          (details.hasFixedEstablishment.contains(true) &&
            (details.fixedEstablishmentTradingName.isEmpty || details.fixedEstablishmentAddress.isEmpty)) ||
          (details.hasFixedEstablishment.contains(false) && details.euSendGoods.isEmpty) ||
          (details.euSendGoods.contains(true) && (details.euSendGoodsTradingName.isEmpty ||
            (details.euTaxReference.isEmpty && details.euVatNumber.isEmpty)))
      )
  }

  def getAllIncompleteEuDetails()(implicit request: AuthenticatedDataRequest[AnyContent]): Seq[EuOptionalDetails] = {
    request.userAnswers
      .get(AllEuOptionalDetailsQuery).map(
      _.filter(details =>
        details.vatRegistered.isEmpty ||
          details.hasFixedEstablishment.isEmpty ||
          (details.vatRegistered.contains(true) && details.euVatNumber.isEmpty) ||
          (details.hasFixedEstablishment.contains(true) &&
            (details.fixedEstablishmentTradingName.isEmpty || details.fixedEstablishmentAddress.isEmpty)) ||
            (details.hasFixedEstablishment.contains(false) && details.euSendGoods.isEmpty) ||
          (details.euSendGoods.contains(true) && (details.euSendGoodsTradingName.isEmpty ||
            (details.euTaxReference.isEmpty && details.euVatNumber.isEmpty)))
      )
    ).getOrElse(List.empty)
  }

  def getAllIncompleteDeregisteredDetails()(implicit request: AuthenticatedDataRequest[AnyContent]): Seq[PreviousRegistrationDetailsWithOptionalVatNumber] = {
    request.userAnswers
      .get(AllPreviousRegistrationsWithOptionalVatNumberQuery).map(
      _.filter(_.previousEuVatNumber.isEmpty)
    ).getOrElse(List.empty)
  }

  def firstIndexedIncompleteDeregisteredCountry(incompleteCountries: Seq[Country])
                                               (implicit request: AuthenticatedDataRequest[AnyContent]): Option[(PreviousRegistrationDetailsWithOptionalVatNumber, Int)] = {
    request.userAnswers.get(AllPreviousRegistrationsWithOptionalVatNumberQuery)
      .getOrElse(List.empty).zipWithIndex
      .find(indexedDetails => incompleteCountries.contains(indexedDetails._1.previousEuCountry))
  }

  def firstIndexedIncompleteEuDetails(incompleteCountries: Seq[Country])
                                     (implicit request: AuthenticatedDataRequest[AnyContent]): Option[(EuOptionalDetails, Int)] = {
    request.userAnswers.get(AllEuOptionalDetailsQuery)
      .getOrElse(List.empty).zipWithIndex
      .find(indexedDetails => incompleteCountries.contains(indexedDetails._1.euCountry))
  }

  def isTradingNamesValid()(implicit request: AuthenticatedDataRequest[AnyContent]): Boolean = {
    request.userAnswers.get(HasTradingNamePage).exists {
      case true => request.userAnswers.get(AllTradingNames).getOrElse(List.empty).nonEmpty
      case false => true
    }
  }

  def isAlreadyMadeSalesValid()(implicit request: AuthenticatedDataRequest[AnyContent]): Boolean = {
    request.userAnswers.get(HasMadeSalesPage).exists {
      case true => request.userAnswers.get(DateOfFirstSalePage).isDefined
      case false => request.userAnswers.get(IsPlanningFirstEligibleSalePage).isDefined
    }
  }

  def hasWebsiteValid()(implicit request: AuthenticatedDataRequest[AnyContent]): Boolean = {
    request.userAnswers.get(HasWebsitePage).exists {
      case true => request.userAnswers.get(AllWebsites).getOrElse(List.empty).nonEmpty
      case false => true
    }
  }

  def isEuDetailsPopulated()(implicit request: AuthenticatedDataRequest[AnyContent]): Boolean = {
    request.userAnswers.get(TaxRegisteredInEuPage).exists {
      case true => request.userAnswers.get(AllEuOptionalDetailsQuery).isDefined
      case false => true
    }
  }

  def isDeregisteredPopulated()(implicit request: AuthenticatedDataRequest[AnyContent]): Boolean = {
    request.userAnswers.get(PreviouslyRegisteredPage).exists {
      case true => request.userAnswers.get(AllPreviousRegistrationsWithOptionalVatNumberQuery).isDefined
      case false => true
    }
  }

  def validate()(implicit request: AuthenticatedDataRequest[AnyContent]): Boolean = {
    getAllIncompleteDeregisteredDetails.isEmpty &&
      getAllIncompleteEuDetails.isEmpty &&
      isTradingNamesValid &&
      isAlreadyMadeSalesValid &&
      hasWebsiteValid &&
      isEuDetailsPopulated &&
      isDeregisteredPopulated
  }

  def getFirstValidationErrorRedirect()(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] = {
    (incompleteTradingNameRedirect ++
      incompleteEligibleSalesRedirect ++
      emptyEuDetailsRedirect ++
      incompleteEuDetailsRedirect ++
      emptyDeregisteredRedirect ++
      incompleteDeregisteredCountryRedirect ++
      incompleteWebsiteUrlsRedirect
      ).headOption
  }

  private def incompleteEuDetailsRedirect()(implicit request: AuthenticatedDataRequest[AnyContent]) =
    firstIndexedIncompleteEuDetails(getAllIncompleteEuDetails().map(
    _.euCountry
  )).map(
    incompleteCountry =>
      Redirect(controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(CheckMode, Index(incompleteCountry._2)))
  )

  private def incompleteDeregisteredCountryRedirect()(implicit request: AuthenticatedDataRequest[AnyContent]) =
    firstIndexedIncompleteDeregisteredCountry(getAllIncompleteDeregisteredDetails().map(
    _.previousEuCountry
  )).map(
    incompleteCountry =>
      Redirect(controllers.previousRegistrations.routes.PreviousEuVatNumberController.onPageLoad(CheckMode, Index(incompleteCountry._2)))
  )

  private def incompleteTradingNameRedirect()(implicit request: AuthenticatedDataRequest[AnyContent]) = if(!isTradingNamesValid) {
    Some(Redirect(controllers.routes.HasTradingNameController.onPageLoad(CheckMode)))
  } else {
    None
  }

  private def incompleteEligibleSalesRedirect()(implicit request: AuthenticatedDataRequest[AnyContent]) = if(!isAlreadyMadeSalesValid) {
    Some(Redirect(controllers.routes.HasMadeSalesController.onPageLoad(CheckMode)))
  } else {
    None
  }

  private def incompleteWebsiteUrlsRedirect()(implicit request: AuthenticatedDataRequest[AnyContent]) = if(!hasWebsiteValid) {
    Some(Redirect(controllers.routes.HasWebsiteController.onPageLoad(CheckMode)))
  } else {
    None
  }

  private def emptyEuDetailsRedirect()(implicit request: AuthenticatedDataRequest[AnyContent]) = if(!isEuDetailsPopulated)  {
    Some(Redirect(controllers.euDetails.routes.TaxRegisteredInEuController.onPageLoad(CheckMode)))
  } else {
    None
  }

  private def emptyDeregisteredRedirect()(implicit request: AuthenticatedDataRequest[AnyContent]) = if(!isDeregisteredPopulated)  {
    Some(Redirect(controllers.previousRegistrations.routes.PreviouslyRegisteredController.onPageLoad(CheckMode)))
  } else {
    None
  }

}

