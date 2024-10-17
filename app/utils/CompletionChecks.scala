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

package utils

import models._
import models.euDetails._
import models.previousRegistrations.{PreviousRegistrationDetailsWithOptionalFields, PreviousRegistrationDetailsWithOptionalVatNumber}
import models.requests.AuthenticatedDataRequest
import pages._
import pages.previousRegistrations._
import play.api.mvc.{AnyContent, Result}
import play.api.mvc.Results.Redirect
import queries._
import queries.previousRegistration._
import utils.EuDetailsCompletionChecks._

import scala.concurrent.Future

trait CompletionChecks {


  protected def withCompleteDataModel[A](index: Index, data: Index => Option[A], onFailure: Option[A] => Result)
                                        (onSuccess: => Result): Result = {
    val incomplete = data(index)
    if (incomplete.isEmpty) {
      onSuccess
    } else {
      onFailure(incomplete)
    }
  }

  protected def withCompleteDataAsync[A](data: () => Seq[A], onFailure: Seq[A] => Future[Result])
                                        (onSuccess: => Future[Result]): Future[Result] = {

    val incomplete = data()
    if (incomplete.isEmpty) {
      onSuccess
    } else {
      onFailure(incomplete)
    }
  }

  def getAllIncompleteDeregisteredDetails()(implicit request: AuthenticatedDataRequest[AnyContent]): Seq[PreviousRegistrationDetailsWithOptionalFields] = {
    request.userAnswers
      .get(AllPreviousRegistrationsWithOptionalFieldsQuery).map(
        _.filter(scheme =>
          scheme.previousEuCountry.isEmpty ||
            scheme.previousSchemesDetails.isEmpty ||
            scheme.previousSchemesDetails.getOrElse(List.empty).exists(_.previousSchemeNumbers.isEmpty))
      ).getOrElse(List.empty)
  }

  def firstIndexedIncompleteDeregisteredCountry(incompleteIndexes: Seq[Int])
                                               (implicit request: AuthenticatedDataRequest[AnyContent]):
  Option[(PreviousRegistrationDetailsWithOptionalVatNumber, Int)] = {
    request.userAnswers.get(AllPreviousRegistrationsWithOptionalVatNumberQuery)
      .getOrElse(List.empty).zipWithIndex
      .find {
        case (_, index) => incompleteIndexes.contains(index)
      }
  }

  def firstIndexedIncompleteEuDetails(incompleteCountries: Seq[Country])
                                     (implicit request: AuthenticatedDataRequest[AnyContent]): Option[(EuOptionalDetails, Int)] = {
    request.userAnswers.get(AllEuOptionalDetailsQuery)
      .getOrElse(List.empty).zipWithIndex
      .find(indexedDetails => incompleteCountries.contains(indexedDetails._1.euCountry))
  }

  private def isTradingNamesValid()(implicit request: AuthenticatedDataRequest[AnyContent]): Boolean = {
    request.userAnswers.get(HasTradingNamePage).exists {
      case true => request.userAnswers.get(AllTradingNames).getOrElse(List.empty).nonEmpty
      case false => request.userAnswers.get(AllTradingNames).getOrElse(List.empty).isEmpty
    }
  }

  private def isAlreadyMadeSalesValid()(implicit request: AuthenticatedDataRequest[AnyContent]): Boolean = {
    request.userAnswers.get(HasMadeSalesPage).isDefined
  }

  private def isDateOfFirstSaleValid()(implicit request: AuthenticatedDataRequest[AnyContent]): Boolean = {
    request.userAnswers.get(HasMadeSalesPage).exists {
      case true => request.userAnswers.get(DateOfFirstSalePage).isDefined
      case _ => true
    }
  }

  private def isPreviouslyRegisteredValid()(implicit request: AuthenticatedDataRequest[AnyContent]): Boolean = {
    request.userAnswers.get(HasMadeSalesPage).exists {
      case false => request.userAnswers.get(PreviouslyRegisteredPage).isDefined
      case _ => true
    }
  }

  private def hasWebsiteValid()(implicit request: AuthenticatedDataRequest[AnyContent]): Boolean = {
    request.userAnswers.get(HasWebsitePage).exists {
      case true => request.userAnswers.get(AllWebsites).getOrElse(List.empty).nonEmpty
      case false => request.userAnswers.get(AllWebsites).getOrElse(List.empty).isEmpty
    }
  }

  private def isDeregisteredPopulated()(implicit request: AuthenticatedDataRequest[AnyContent]): Boolean = {
    request.userAnswers.get(PreviouslyRegisteredPage).isDefined
  }

  private def arePreviousRegistrationsValid()(implicit request: AuthenticatedDataRequest[AnyContent]): Boolean = {
    request.userAnswers.get(PreviouslyRegisteredPage).exists {
      case true => val maybePreviousRegistrations = request.userAnswers.get(AllPreviousRegistrationsWithOptionalVatNumberQuery)
        maybePreviousRegistrations match {
          case Some(previousRegistrations) if previousRegistrations.nonEmpty =>
            true
          case _ =>
            false
        }
      case false => request.userAnswers.get(AllPreviousRegistrationsWithOptionalVatNumberQuery).getOrElse(List.empty).isEmpty
    }
  }

  private def isOnlineMarketplacePopulated()(implicit request: AuthenticatedDataRequest[AnyContent]): Boolean = {
    request.userAnswers.get(IsOnlineMarketplacePage).isDefined
  }

  private def isContactDetailsPopulated()(implicit request: AuthenticatedDataRequest[AnyContent]): Boolean = {
    request.userAnswers.get(BusinessContactDetailsPage).isDefined
  }

  private def isBankDetailsPopulated()(implicit request: AuthenticatedDataRequest[AnyContent]): Boolean = {
    request.userAnswers.get(BankDetailsPage).isDefined
  }

  def validate()(implicit request: AuthenticatedDataRequest[AnyContent]): Boolean = {
    getAllIncompleteDeregisteredDetails().isEmpty &&
      getAllIncompleteEuDetails().isEmpty &&
      isTradingNamesValid() &&
      isAlreadyMadeSalesValid() &&
      isDateOfFirstSaleValid() &&
      isPreviouslyRegisteredValid() &&
      hasWebsiteValid() &&
      isEuDetailsPopulated() &&
      isDeregisteredPopulated() &&
      arePreviousRegistrationsValid() &&
      isOnlineMarketplacePopulated() &&
      isContactDetailsPopulated() &&
      isBankDetailsPopulated()
  }

  def validateHybridReversal()(implicit request: AuthenticatedDataRequest[AnyContent]): Boolean = {
    isAlreadyMadeSalesValid()
  }

  def getFirstValidationErrorRedirect(mode: Mode)(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] = {
    (incompleteTradingNameRedirect(mode) ++
      incompleteEligibleSalesRedirect(mode) ++
      incompleteDateOfFirstSaleRedirect(mode) ++
      emptyEuDetailsRedirect(mode) ++
      incompleteEuDetailsRedirect(mode) ++
      emptyDeregisteredRedirect(mode) ++
      incompletePreviousRegistrationRedirect(mode) ++
      emptyHasOnlineMarketplace(mode) ++
      incompleteWebsiteUrlsRedirect(mode) ++
      emptyContactDetails(mode) ++
      emptyBankDetails(mode)
      ).headOption
  }

  def incompletePreviousRegistrationRedirect(mode: Mode)
                                            (implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] =
    firstIndexedIncompleteDeregisteredCountry(getAllIncompleteDeregisteredDetails().zipWithIndex.map(_._2)) match {
      case Some(incompleteCountry) if incompleteCountry._1.previousSchemesDetails.isDefined =>
        incompleteCountry._1.previousSchemesDetails.getOrElse(List.empty).zipWithIndex.find(_._1.previousSchemeNumbers.isEmpty) match {
          case Some(schemeDetails) =>
            request.userAnswers.get(PreviousSchemeTypePage(Index(incompleteCountry._2), Index(schemeDetails._2))) match {
              case Some(PreviousSchemeType.OSS) =>
                Some(Redirect(controllers.previousRegistrations.routes.PreviousOssNumberController.onPageLoad(
                  mode, Index(incompleteCountry._2), Index(schemeDetails._2))))
              case Some(PreviousSchemeType.IOSS) =>
                schemeDetails._1.previousScheme match {
                  case Some(_) =>
                    Some(Redirect(controllers.previousRegistrations.routes.PreviousIossNumberController.onPageLoad(
                      mode, Index(incompleteCountry._2), Index(schemeDetails._2))))
                  case None =>
                    Some(Redirect(controllers.previousRegistrations.routes.PreviousIossSchemeController.onPageLoad(
                      mode, Index(incompleteCountry._2), Index(schemeDetails._2))))
                }
              case None => None
            }
          case None => None
        }

      case Some(incompleteCountry) =>
        Some(Redirect(controllers.previousRegistrations.routes.PreviousSchemeController.onPageLoad(
          mode, Index(incompleteCountry._2), Index(0))))

      case None =>
        request.userAnswers.get(PreviouslyRegisteredPage) match {
          case Some(true) => Some(Redirect(controllers.previousRegistrations.routes.AddPreviousRegistrationController.onPageLoad(mode)))
          case _ => None
        }
    }

  def incompleteCountryEuDetailsRedirect(mode: Mode)(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] = {

    firstIndexedIncompleteEuDetails(getAllIncompleteEuDetails().map(_.euCountry)) match {
      case Some((incompleteCountry, index)) =>
        val defaultRedirect = Some(Redirect(controllers.euDetails.routes.EuCountryController.onPageLoad(mode, Index(index))))

        incompleteCountry.euVatNumber match {
          case Some(vatNumber) =>
            CountryWithValidationDetails.euCountriesWithVRNValidationRules.find(_.country.code == incompleteCountry.euCountry.code) match {
              case Some(validationRule) if !vatNumber.matches(validationRule.vrnRegex) =>
                Some(Redirect(controllers.euDetails.routes.EuVatNumberController.onPageLoad(mode, Index(index))))
              case _ => defaultRedirect
            }
          case _ => defaultRedirect
        }
      case _ => None
    }

  }

  private def incompleteTradingNameRedirect(mode: Mode)(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] = if (!isTradingNamesValid()) {
    Some(Redirect(controllers.routes.HasTradingNameController.onPageLoad(mode)))
  } else {
    None
  }

  private def incompleteEligibleSalesRedirect(mode: Mode)(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] = if (!isAlreadyMadeSalesValid()) {
    Some(Redirect(controllers.routes.HasMadeSalesController.onPageLoad(mode)))
  } else {
    None
  }

  private def incompleteDateOfFirstSaleRedirect(mode: Mode)(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] = if (!isDateOfFirstSaleValid()) {
    Some(Redirect(controllers.routes.DateOfFirstSaleController.onPageLoad(mode)))
  } else {
    None
  }

  private def incompleteWebsiteUrlsRedirect(mode: Mode)(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] = if (!hasWebsiteValid()) {
    Some(Redirect(controllers.routes.HasWebsiteController.onPageLoad(mode)))
  } else {
    None
  }

  private def emptyDeregisteredRedirect(mode: Mode)(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] = if (!isDeregisteredPopulated()) {
    Some(Redirect(controllers.previousRegistrations.routes.PreviouslyRegisteredController.onPageLoad(mode)))
  } else {
    None
  }

  private def emptyHasOnlineMarketplace(mode: Mode)(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] =
    if (!isOnlineMarketplacePopulated()) {
      Some(Redirect(controllers.routes.IsOnlineMarketplaceController.onPageLoad(mode)))
    } else {
      None
    }

  private def emptyContactDetails(mode: Mode)(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] =
    if (!isContactDetailsPopulated()) {
      Some(Redirect(controllers.routes.BusinessContactDetailsController.onPageLoad(mode)))
    } else {
      None
    }

  private def emptyBankDetails(mode: Mode)(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] =
    if (!isBankDetailsPopulated()) {
      Some(Redirect(controllers.routes.BankDetailsController.onPageLoad(mode)))
    } else {
      None
    }

}


