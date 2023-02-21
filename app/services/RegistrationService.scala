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

import cats.data.Validated
import cats.data.Validated.Invalid
import cats.implicits._
import models._
import models.domain.EuTaxIdentifierType.{Other, Vat}
import models.domain._
import models.euDetails.{EuConsumerSalesMethod, RegistrationType}
import models.previousRegistrations.PreviousSchemeNumbers
import pages._
import pages.euDetails._
import pages.previousRegistrations.{PreviousEuCountryPage, PreviousOssNumberPage, PreviousSchemePage, PreviouslyRegisteredPage}
import queries._
import queries.previousRegistration.{AllPreviousRegistrationsRawQuery, AllPreviousSchemesRawQuery}
import uk.gov.hmrc.domain.Vrn

import java.time.LocalDate
import javax.inject.Inject

class RegistrationService @Inject()(dateService: DateService) {

  def fromUserAnswers(answers: UserAnswers, vrn: Vrn): ValidationResult[Registration] =
    (
      getCompanyName(answers),
      getTradingNames(answers),
      getVatDetails(answers),
      getEuTaxRegistrations(answers),
      getCommencementDate(answers),
      getContactDetails(answers),
      getWebsites(answers),
      getPreviousRegistrations(answers),
      getBankDetails(answers),
      getOnlineMarketplace(answers),
      getNiPresence(answers),
      ).mapN(
      (
        name,
        tradingNames,
        vatDetails,
        euRegistrations,
        startDate,
        contactDetails,
        websites,
        previousRegistrations,
        bankDetails,
        isOnlineMarketplace,
        niPresence
      ) =>
        Registration(
          vrn = vrn,
          registeredCompanyName = name,
          tradingNames = tradingNames,
          vatDetails = vatDetails,
          euRegistrations = euRegistrations.map {
            case Some(list) => list
            case _ => ???
          },
          contactDetails = contactDetails,
          websites = websites,
          commencementDate = startDate,
          previousRegistrations = previousRegistrations,
          bankDetails = bankDetails,
          isOnlineMarketplace = isOnlineMarketplace,
          niPresence = niPresence,
          dateOfFirstSale = answers.get(DateOfFirstSalePage)
        )
    )

  private def getCompanyName(answers: UserAnswers): ValidationResult[String] =
    answers.vatInfo match {
      case Some(vatInfo) => vatInfo.organisationName.validNec
      case _ => DataMissingError(CheckVatDetailsPage).invalidNec
    }

  private def getTradingNames(answers: UserAnswers): ValidationResult[List[String]] = {
    answers.get(HasTradingNamePage) match {
      case Some(true) =>
        answers.get(AllTradingNames) match {
          case Some(Nil) | None => DataMissingError(AllTradingNames).invalidNec
          case Some(list) => list.validNec
        }

      case Some(false) =>
        List.empty.validNec

      case None =>
        DataMissingError(HasTradingNamePage).invalidNec
    }
  }

  private def getVatDetails(answers: UserAnswers): ValidationResult[VatDetails] = {
    answers.vatInfo.map(
      vatInfo =>
        VatDetails(
          vatInfo.registrationDate,
          vatInfo.address,
          vatInfo.partOfVatGroup,
          VatDetailSource.Etmp
        ).validNec
    ).getOrElse(
      DataMissingError(CheckVatDetailsPage).invalidNec
    )
  }

  private def getCommencementDate(answers: UserAnswers): ValidationResult[LocalDate] =
    answers.get(DateOfFirstSalePage) match {
      case Some(startDate) => dateService.startDateBasedOnFirstSale(startDate).validNec
      case None => answers.get(IsPlanningFirstEligibleSalePage) match {
        case Some(true) => LocalDate.now().validNec
        case _ => DataMissingError(IsPlanningFirstEligibleSalePage).invalidNec
      }
    }

  private def getContactDetails(answers: UserAnswers): ValidationResult[BusinessContactDetails] =
    answers.get(BusinessContactDetailsPage) match {
      case Some(details) => details.validNec
      case None => DataMissingError(BusinessContactDetailsPage).invalidNec
    }

  private def getBankDetails(answers: UserAnswers): ValidationResult[BankDetails] =
    answers.get(BankDetailsPage) match {
      case Some(bankDetails) => bankDetails.validNec
      case None => DataMissingError(BankDetailsPage).invalidNec
    }

  private def getPreviousRegistrations(answers: UserAnswers): ValidationResult[List[PreviousRegistration]] = {
    answers.get(PreviouslyRegisteredPage) match {
      case Some(true) =>
        answers.get(AllPreviousRegistrationsRawQuery) match {
          case None =>
            DataMissingError(AllPreviousRegistrationsRawQuery).invalidNec
          case Some(details) =>
            details.value.zipWithIndex.map {
              case (_, index) =>
                processPreviousRegistration(answers, Index(index))
            }.toList.sequence
        }

      case Some(false) =>
        List.empty.validNec

      case None =>
        DataMissingError(PreviouslyRegisteredPage).invalidNec
    }
  }

  private def processPreviousRegistration(answers: UserAnswers, index: Index): ValidationResult[PreviousRegistration] = {
    (
      getPreviousCountry(answers, index),
      getPreviousSchemes(answers, index)
      ).mapN((previousCountry, previousSchemes) =>
      PreviousRegistration(previousCountry, previousSchemes)
    )
  }

  private def getPreviousCountry(answers: UserAnswers, countryIndex: Index): ValidationResult[Country] =
    answers.get(PreviousEuCountryPage(countryIndex)) match {
      case Some(country) =>
        country.validNec
      case None =>
        DataMissingError(PreviousEuCountryPage(countryIndex)).invalidNec
    }

  private def getPreviousScheme(answers: UserAnswers, countryIndex: Index, schemeIndex: Index): ValidationResult[PreviousScheme] =
    answers.get(PreviousSchemePage(countryIndex, schemeIndex)) match {
      case Some(scheme) =>
        scheme.validNec
      case None =>
        DataMissingError(PreviousSchemePage(countryIndex, schemeIndex)).invalidNec
    }

  private def getPreviousSchemeNumber(answers: UserAnswers, countryIndex: Index, schemeIndex: Index): ValidationResult[PreviousSchemeNumbers] =
    answers.get(PreviousOssNumberPage(countryIndex, schemeIndex)) match {
      case Some(vatNumber) =>
        vatNumber.validNec
      case None =>
        DataMissingError(PreviousOssNumberPage(countryIndex, schemeIndex)).invalidNec
    }

  private def getPreviousSchemes(answers: UserAnswers, countryIndex: Index): ValidationResult[List[PreviousSchemeDetails]] = {
    answers.get(AllPreviousSchemesRawQuery(countryIndex)) match {
      case None =>
        DataMissingError(AllPreviousSchemesRawQuery(countryIndex)).invalidNec
      case Some(previousSchemes) =>
        previousSchemes.value.zipWithIndex.map {
          case (_, index) =>
            processPreviousSchemes(answers, countryIndex, Index(index))
        }.toList.sequence
    }
  }

  private def processPreviousSchemes(answers: UserAnswers, countryIndex: Index, schemeIndex: Index): ValidationResult[PreviousSchemeDetails] = {
    (
      getPreviousScheme(answers, countryIndex, schemeIndex),
      getPreviousSchemeNumber(answers, countryIndex, schemeIndex)
      ).mapN((previousScheme, previousSchemeNumber) =>
      PreviousSchemeDetails(previousScheme, previousSchemeNumber)
    )
  }

  private def getEuTaxRegistrations(answers: UserAnswers): ValidationResult[List[Option[EuTaxRegistration]]] = {
    answers.get(TaxRegisteredInEuPage) match {
      case Some(true) =>
        answers.get(AllEuDetailsRawQuery) match {
          case None =>
            DataMissingError(AllEuDetailsRawQuery).invalidNec
          case Some(euDetails) =>
            euDetails.value.zipWithIndex.map {
              case (_, index) =>
                processEuDetail(answers, Index(index))
            }.toList.sequence
        }

      case Some(false) =>
        List.empty.validNec

      case None =>
        DataMissingError(TaxRegisteredInEuPage).invalidNec
    }
  }

  private def processEuDetail(answers: UserAnswers, index: Index): ValidationResult[Option[EuTaxRegistration]] = {
    answers.get(EuCountryPage(index)) match {
      case Some(country) =>
        answers.get(SellsGoodsToEUConsumersPage(index)) match {
          case Some(true) =>
            sellsGoodsToEuConsumers(answers, country, index)
          case Some(false) =>
            doesNotSellGoodsToEuConsumers(answers, country, index).map(Some(_))
          case None => DataMissingError(SellsGoodsToEUConsumersPage(index)).invalidNec
        }
      case None =>
        DataMissingError(EuCountryPage(index)).invalidNec
    }
  }

  private def sellsGoodsToEuConsumers(answers: UserAnswers, country: Country, index: Index): ValidationResult[EuTaxRegistration] = {
    (answers.vatInfo.exists(_.partOfVatGroup), answers.get(SellsGoodsToEUConsumerMethodPage(index))) match {
      case (true, Some(EuConsumerSalesMethod.DispatchWarehouse)) =>
        getRegistrationWithDispatchWarehouse(answers, country, index)
      case (true, Some(EuConsumerSalesMethod.FixedEstablishment)) =>
//       TODO DataMissingError()
      ???
      case (true, None) =>
        DataMissingError(SellsGoodsToEUConsumerMethodPage(index)).invalidNec
      case (false, Some(EuConsumerSalesMethod.FixedEstablishment)) =>
        getRegistrationWithFixedEstablishment(answers, country, index)
      case (false, Some(EuConsumerSalesMethod.DispatchWarehouse)) =>
        getRegistrationWithDispatchWarehouse(answers, country, index)
      case (_, None) =>
        DataMissingError(SellsGoodsToEUConsumerMethodPage(index)).invalidNec
    }
  }

  private def doesNotSellGoodsToEuConsumers(answers: UserAnswers, country: Country, index: Index): ValidationResult[EuTaxRegistration] = {
    answers.get(VatRegisteredPage(index)) match {
      case Some(true) =>
        getEuVatNumber(answers, index).map(
          vatNumber => EuVatRegistration(country, vatNumber)
        )
      case Some(false) =>
        Validated.Valid(RegistrationWithoutTaxId(country))

      case None =>
        DataMissingError(VatRegisteredPage(index)).invalidNec
    }
  }

  private def getRegistrationWithFixedEstablishment(answers: UserAnswers, country: Country, index: Index): ValidationResult[EuTaxRegistration] =
    (
      getEuTaxIdentifier(answers, index),
      getFixedEstablishment(answers, index)
      ).mapN(
      (taxIdentifier, fixedEstablishment) =>
        RegistrationWithFixedEstablishment(country, taxIdentifier, fixedEstablishment)
    )

  private def getRegistrationWithDispatchWarehouse(answers: UserAnswers, country: Country, index: Index): ValidationResult[EuTaxRegistration] =
    (
      getEuTaxIdentifier(answers, index),
      getEuSendGoods(answers, index)
      ).mapN(
      (taxIdentifier, dispatchWarehouse) =>
        RegistrationWithoutFixedEstablishmentWithTradeDetails(country, taxIdentifier, dispatchWarehouse)
    )

  private def getEuTaxIdentifier(answers: UserAnswers, index: Index): ValidationResult[EuTaxIdentifier] = {
    answers.get(SellsGoodsToEUConsumersPage(index)) match {
      case Some(true) =>
        answers.get(RegistrationTypePage(index)) match {
          case Some(RegistrationType.VatNumber) =>
            getEuVatNumber(answers, index).map(v => EuTaxIdentifier(Vat, Some(v)))
          case Some(RegistrationType.TaxId) =>
            getEuTaxId(answers, index).map(v => EuTaxIdentifier(Other, Some(v)))
          case None => DataMissingError(RegistrationTypePage(index)).invalidNec
        }

      case Some(false) =>
        answers.get(VatRegisteredPage(index)) match {
          case Some(true) =>
            getEuVatNumber(answers, index).map(v => EuTaxIdentifier(Vat, Some(v)))
          case Some(false) =>
            EuTaxIdentifier(Other, None).validNec
          case None =>
            DataMissingError(VatRegisteredPage(index)).invalidNec
        }

      case None => DataMissingError(SellsGoodsToEUConsumersPage(index)).invalidNec
    }
  }

  private def getEuVatNumber(answers: UserAnswers, index: Index): ValidationResult[String] =
    answers.get(EuVatNumberPage(index)) match {
      case Some(vatNumber) => vatNumber.validNec
      case None => DataMissingError(EuVatNumberPage(index)).invalidNec
    }

  private def getEuTaxId(answers: UserAnswers, index: Index): ValidationResult[String] =
    answers.get(EuTaxReferencePage(index)) match {
      case Some(taxId) => taxId.validNec
      case None => DataMissingError(EuTaxReferencePage(index)).invalidNec
    }

  private def getFixedEstablishment(answers: UserAnswers, index: Index): ValidationResult[TradeDetails] =
    (
      getFixedEstablishmentTradingName(answers, index),
      getFixedEstablishmentAddress(answers, index)
      ).mapN(TradeDetails.apply)

  private def getEuSendGoods(answers: UserAnswers, index: Index): ValidationResult[TradeDetails] =
    (
      getEuSendGoodsTradingName(answers, index),
      getEuSendGoodsAddress(answers, index)
      ).mapN(TradeDetails.apply)

  private def getFixedEstablishmentTradingName(answers: UserAnswers, index: Index): ValidationResult[String] =
    answers.get(FixedEstablishmentTradingNamePage(index)) match {
      case Some(name) => name.validNec
      case None => DataMissingError(FixedEstablishmentTradingNamePage(index)).invalidNec
    }

  private def getFixedEstablishmentAddress(answers: UserAnswers, index: Index): ValidationResult[InternationalAddress] =
    answers.get(FixedEstablishmentAddressPage(index)) match {
      case Some(address) => address.validNec
      case None => DataMissingError(FixedEstablishmentAddressPage(index)).invalidNec
    }

  private def getEuSendGoodsTradingName(userAnswers: UserAnswers, index: Index): ValidationResult[String] = {
    userAnswers.get(EuSendGoodsTradingNamePage(index)) match {
      case Some(answer) => answer.validNec
      case None => DataMissingError(EuSendGoodsTradingNamePage(index)).invalidNec
    }
  }

  private def getEuSendGoodsAddress(userAnswers: UserAnswers, index: Index): ValidationResult[InternationalAddress] = {
    userAnswers.get(EuSendGoodsAddressPage(index)) match {
      case Some(answer) => answer.validNec
      case None => DataMissingError(EuSendGoodsAddressPage(index)).invalidNec
    }
  }

  private def getWebsites(answers: UserAnswers): ValidationResult[List[String]] =
    answers.get(HasWebsitePage) match {
      case Some(true) =>
        answers.get(AllWebsites) match {
          case Some(Nil) | None => DataMissingError(AllWebsites).invalidNec
          case Some(list) => list.validNec
        }

      case Some(false) =>
        List.empty.validNec

      case None =>
        DataMissingError(HasWebsitePage).invalidNec
    }

  private def getOnlineMarketplace(answers: UserAnswers): ValidationResult[Boolean] =
    answers.get(IsOnlineMarketplacePage) match {
      case Some(answer) => answer.validNec
      case None => DataMissingError(IsOnlineMarketplacePage).invalidNec
    }

  private def getNiPresence(answers: UserAnswers): ValidationResult[Option[NiPresence]] =
    answers.get(BusinessBasedInNiPage) match {
      case Some(true) =>
        Some(PrincipalPlaceOfBusinessInNi).validNec

      case Some(false) =>
        answers.get(HasFixedEstablishmentInNiPage) match {
          case Some(true) =>
            Some(FixedEstablishmentInNi).validNec

          case Some(false) =>
            answers.get(SalesChannelsPage) match {
              case Some(answer) => Some(NoPresence(answer)).validNec
              case None => None.validNec
            }

          case None =>
            None.validNec
        }

      case None =>
        None.validNec
    }

}
