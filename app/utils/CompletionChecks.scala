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

package utils

import models.euDetails.{EuConsumerSalesMethod, EuOptionalDetails, RegistrationType}
import models.previousRegistrations.PreviousRegistrationDetailsWithOptionalVatNumber
import models.requests.AuthenticatedDataRequest
import models.{CheckMode, Country, Index, Mode, PreviousSchemeType}
import pages._
import pages.euDetails.{EuSendGoodsAddressPage, EuSendGoodsTradingNamePage, EuTaxReferencePage, EuVatNumberPage, FixedEstablishmentAddressPage, FixedEstablishmentTradingNamePage, RegistrationTypePage, SellsGoodsToEUConsumerMethodPage, SellsGoodsToEUConsumersPage, TaxRegisteredInEuPage, VatRegisteredPage}
import pages.previousRegistrations.{PreviousSchemeTypePage, PreviouslyRegisteredPage}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import queries.previousRegistration.AllPreviousRegistrationsWithOptionalVatNumberQuery
import queries.{AllEuOptionalDetailsQuery, AllTradingNames, AllWebsites, EuOptionalDetailsQuery}

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

  def getIncompleteEuDetails(index: Index)(implicit request: AuthenticatedDataRequest[AnyContent]): Option[EuOptionalDetails] = {
    val isPartOfVatGroup = request.userAnswers.vatInfo.exists(_.partOfVatGroup)
    request.userAnswers
      .get(EuOptionalDetailsQuery(index))
      .find(details =>
        partOfVatGroup(isPartOfVatGroup, details) || notPartOfVatGroup(isPartOfVatGroup, details))
  }

  def getAllIncompleteEuDetails()(implicit request: AuthenticatedDataRequest[AnyContent]): Seq[EuOptionalDetails] = {
    val isPartOfVatGroup = request.userAnswers.vatInfo.exists(_.partOfVatGroup)
    request.userAnswers
      .get(AllEuOptionalDetailsQuery).map(
      _.filter(details =>
        partOfVatGroup(isPartOfVatGroup, details) ||
        notPartOfVatGroup(isPartOfVatGroup, details)
      )
    ).getOrElse(List.empty)
  }

  private def partOfVatGroup(isPartOfVatGroup: Boolean, details: EuOptionalDetails): Boolean = {
    isPartOfVatGroup && notSellingToEuConsumers(details) || sellsToEuConsumers(isPartOfVatGroup, details)
  }

  private def notPartOfVatGroup(isPartOfVatGroup: Boolean, details: EuOptionalDetails): Boolean = {
    !isPartOfVatGroup && notSellingToEuConsumers(details) || sellsToEuConsumers(isPartOfVatGroup, details)
  }

  private def notSellingToEuConsumers(details: EuOptionalDetails): Boolean = {
    details.sellsGoodsToEUConsumers.isEmpty ||
      (details.sellsGoodsToEUConsumers.contains(false) && details.vatRegistered.isEmpty) ||
      (details.vatRegistered.contains(true) && details.euVatNumber.isEmpty)
  }

  private def sellsToEuConsumers(isPartOfVatGroup: Boolean, details: EuOptionalDetails): Boolean = {
    (details.sellsGoodsToEUConsumers.contains(true) && details.sellsGoodsToEUConsumerMethod.isEmpty) ||
      (details.sellsGoodsToEUConsumerMethod.contains(EuConsumerSalesMethod.DispatchWarehouse) && details.registrationType.isEmpty) ||
      (details.registrationType.contains(RegistrationType.VatNumber) && details.euVatNumber.isEmpty) ||
      (details.registrationType.contains(RegistrationType.TaxId) && details.euTaxReference.isEmpty) ||
    fixedEstablishment(isPartOfVatGroup, details) || sendsGoods(details)

  }

  private def sendsGoods(details: EuOptionalDetails): Boolean = {
    (details.sellsGoodsToEUConsumerMethod.contains(EuConsumerSalesMethod.DispatchWarehouse) &&
      (details.registrationType.contains(RegistrationType.TaxId) || details.registrationType.contains(RegistrationType.VatNumber)) &&
      (details.euSendGoodsTradingName.isEmpty || details.euSendGoodsAddress.isEmpty))
  }

  private def fixedEstablishment(isPartOfVatGroup: Boolean, details: EuOptionalDetails): Boolean = {
    (!isPartOfVatGroup && details.sellsGoodsToEUConsumerMethod.contains(EuConsumerSalesMethod.FixedEstablishment) &&
      (details.registrationType.contains(RegistrationType.TaxId) || details.registrationType.contains(RegistrationType.VatNumber)) &&
      (details.fixedEstablishmentTradingName.isEmpty || details.fixedEstablishmentAddress.isEmpty))
  }

  def getAllIncompleteDeregisteredDetails()(implicit request: AuthenticatedDataRequest[AnyContent]): Seq[PreviousRegistrationDetailsWithOptionalVatNumber] = {

    request.userAnswers
      .get(AllPreviousRegistrationsWithOptionalVatNumberQuery).map(
      _.filter(scheme =>
        scheme.previousSchemesDetails.isEmpty || scheme.previousSchemesDetails.getOrElse(List.empty).exists(_.previousSchemeNumbers.isEmpty))
    ).getOrElse(List.empty)
  }

  def firstIndexedIncompleteDeregisteredCountry(incompleteCountries: Seq[Country])
                                               (implicit request: AuthenticatedDataRequest[AnyContent]):
  Option[(PreviousRegistrationDetailsWithOptionalVatNumber, Int)] = {
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
      incompletePreviousRegistrationRedirect(CheckMode) ++
      incompleteWebsiteUrlsRedirect
      ).headOption
  }

//  private def incompleteEuDetailsRedirect()(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] =
//    firstIndexedIncompleteEuDetails(getAllIncompleteEuDetails().map(
//      _.euCountry
//    )).map(
//      incompleteCountry =>
//        Redirect(controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(CheckMode, Index(incompleteCountry._2)))
//    )

  //TODO - Change method name when done
  private def incompleteEuDetailsRedirect(mode: Mode)
                                            (implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] = {
    val isPartOfVatGroup = request.userAnswers.vatInfo.exists(_.partOfVatGroup)
    firstIndexedIncompleteEuDetails(getAllIncompleteEuDetails().map(_.euCountry)) match {
      case Some(incompleteCountry) =>
        request.userAnswers.get(SellsGoodsToEUConsumersPage(Index(incompleteCountry._2))) match {
          case Some(true) =>
            request.userAnswers.get(SellsGoodsToEUConsumerMethodPage(Index(incompleteCountry._2))) match {
              case Some(EuConsumerSalesMethod.FixedEstablishment) =>
                fixedEstablishmentRedirect(mode, isPartOfVatGroup, incompleteCountry)
              case Some(EuConsumerSalesMethod.DispatchWarehouse) =>
                dispatchWarehouseRedirect(mode, incompleteCountry)
              case None =>
                Some(Redirect(controllers.euDetails.routes.SellsGoodsToEUConsumerMethodController.onPageLoad(mode, Index(incompleteCountry._2))))
            }
          case Some(false) =>
            notSellingGoodsRedirect(mode, incompleteCountry)
          case None =>
            Some(Redirect(controllers.euDetails.routes.SellsGoodsToEUConsumersController.onPageLoad(CheckMode, Index(incompleteCountry._2))))

        }
    }
  }

  private def fixedEstablishmentRedirect(
                                          mode: Mode,
                                          isPartOfVatGroup: Boolean,
                                          incompleteCountry: (EuOptionalDetails, Int)
                                        )(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] = {
    if (isPartOfVatGroup) {
      // TODO - redirect in controller - create Page instead???
      ???
    } else {
      request.userAnswers.get(RegistrationTypePage(Index(incompleteCountry._2))) match {
        case Some(RegistrationType.VatNumber) =>
          request.userAnswers.get(EuVatNumberPage(Index(incompleteCountry._2))) match {
            case Some(_) =>
              fixedEstablishingTradeDetailsRedirect(mode, incompleteCountry)
            case None =>
              Some(Redirect(controllers.euDetails.routes.EuVatNumberController.onPageLoad(mode, Index(incompleteCountry._2))))
          }
        case Some(RegistrationType.TaxId) =>
          request.userAnswers.get(EuTaxReferencePage(Index(incompleteCountry._2))) match {
            case Some(_) =>
              fixedEstablishingTradeDetailsRedirect(mode, incompleteCountry)
          }
        case None =>
          Some(Redirect(controllers.euDetails.routes.RegistrationTypeController.onPageLoad(mode, Index(incompleteCountry._2))))
      }
    }
  }

  private def fixedEstablishingTradeDetailsRedirect(
                                                     mode: Mode,
                                                     incompleteCountry: (EuOptionalDetails, Int)
                                                   )(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] = {
    request.userAnswers.get(FixedEstablishmentTradingNamePage(Index(incompleteCountry._2))) match {
      case Some(_) =>
        request.userAnswers.get(FixedEstablishmentAddressPage(Index(incompleteCountry._2))) match {
          case Some(_) =>
            Some(Redirect(controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(mode, Index(incompleteCountry._2))))
          case None =>
            Some(Redirect(controllers.euDetails.routes.FixedEstablishmentAddressController.onPageLoad(mode, Index(incompleteCountry._2))))
        }
      case None =>
        Some(Redirect(controllers.euDetails.routes.FixedEstablishmentTradingNameController.onPageLoad(mode, Index(incompleteCountry._2))))
    }
  }

  private def dispatchWarehouseRedirect(
                                         mode: Mode,
                                         incompleteCountry: (EuOptionalDetails, Int)
                                       )(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] = {
    request.userAnswers.get(RegistrationTypePage(Index(incompleteCountry._2))) match {
      case Some(RegistrationType.VatNumber) =>
        request.userAnswers.get(EuVatNumberPage(Index(incompleteCountry._2))) match {
          case Some(_) =>
            dispatchWarehouseTradeDetailsRedirect(mode, incompleteCountry)
          case None =>
            Some(Redirect(controllers.euDetails.routes.EuVatNumberController.onPageLoad(mode, Index(incompleteCountry._2))))
        }
      case Some(RegistrationType.TaxId) =>
        request.userAnswers.get(EuTaxReferencePage(Index(incompleteCountry._2))) match {
          case Some(_) =>
            dispatchWarehouseTradeDetailsRedirect(mode, incompleteCountry)
          case None =>
            Some(Redirect(controllers.euDetails.routes.EuTaxReferenceController.onPageLoad(mode, Index(incompleteCountry._2))))
        }
    }
  }

  private def dispatchWarehouseTradeDetailsRedirect(
                                                     mode: Mode,
                                                     incompleteCountry: (EuOptionalDetails, Int)
                                                   )(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] = {
    request.userAnswers.get(EuSendGoodsTradingNamePage(Index(incompleteCountry._2))) match {
      case Some(_) =>
        request.userAnswers.get(EuSendGoodsAddressPage(Index(incompleteCountry._2))) match {
          case Some(_) =>
            Some(Redirect(controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(mode, Index(incompleteCountry._2))))
          case None =>
            Some(Redirect(controllers.euDetails.routes.EuSendGoodsAddressController.onPageLoad(mode, Index(incompleteCountry._2))))
        }
      case None =>
        Some(Redirect(controllers.euDetails.routes.EuSendGoodsTradingNameController.onPageLoad(mode, Index(incompleteCountry._2))))
    }
  }

  private def notSellingGoodsRedirect(
                                       mode: Mode,
                                       incompleteCountry: (EuOptionalDetails, Int)
                                     )(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] = {
    request.userAnswers.get(VatRegisteredPage(Index(incompleteCountry._2))) match {
      case Some(true) =>
        request.userAnswers.get(EuVatNumberPage(Index(incompleteCountry._2))) match {
          case Some(_) =>
            Some(Redirect(controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(mode, Index(incompleteCountry._2))))
          case None =>
            Some(Redirect(controllers.euDetails.routes.EuVatNumberController.onPageLoad(mode, Index(incompleteCountry._2))))
        }
      case Some(false) =>
        Some(Redirect(controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(mode, Index(incompleteCountry._2))))
      case None =>
        Some(Redirect(controllers.euDetails.routes.VatRegisteredController.onPageLoad(mode, Index(incompleteCountry._2))))
    }
  }

  def incompletePreviousRegistrationRedirect(mode: Mode)
                                            (implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] =
    firstIndexedIncompleteDeregisteredCountry(getAllIncompleteDeregisteredDetails().map(_.previousEuCountry)) match {
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
          mode, Index(incompleteCountry._2), Index(incompleteCountry._2))))

      case None => None

    }

  private def incompleteTradingNameRedirect()(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] = if (!isTradingNamesValid) {
    Some(Redirect(controllers.routes.HasTradingNameController.onPageLoad(CheckMode)))
  } else {
    None
  }

  private def incompleteEligibleSalesRedirect()(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] = if (!isAlreadyMadeSalesValid) {
    Some(Redirect(controllers.routes.HasMadeSalesController.onPageLoad(CheckMode)))
  } else {
    None
  }

  private def incompleteWebsiteUrlsRedirect()(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] = if (!hasWebsiteValid) {
    Some(Redirect(controllers.routes.HasWebsiteController.onPageLoad(CheckMode)))
  } else {
    None
  }

  private def emptyEuDetailsRedirect()(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] = if (!isEuDetailsPopulated) {
    Some(Redirect(controllers.euDetails.routes.TaxRegisteredInEuController.onPageLoad(CheckMode)))
  } else {
    None
  }

  private def emptyDeregisteredRedirect()(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] = if (!isDeregisteredPopulated) {
    Some(Redirect(controllers.previousRegistrations.routes.PreviouslyRegisteredController.onPageLoad(CheckMode)))
  } else {
    None
  }

}

