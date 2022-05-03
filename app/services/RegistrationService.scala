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

package services

import cats.implicits._
import models._
import models.domain.EuTaxIdentifierType.{Other, Vat}
import models.domain._
import pages._
import pages.euDetails._
import pages.previousRegistrations.{PreviousEuCountryPage, PreviousEuVatNumberPage, PreviouslyRegisteredPage}
import queries._
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
      getNiPresence(answers)
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
          vrn                   = vrn,
          registeredCompanyName = name,
          tradingNames          = tradingNames,
          vatDetails            = vatDetails,
          euRegistrations       = euRegistrations,
          contactDetails        = contactDetails,
          websites              = websites,
          commencementDate      = startDate,
          previousRegistrations = previousRegistrations,
          bankDetails           = bankDetails,
          isOnlineMarketplace   = isOnlineMarketplace,
          niPresence            = niPresence,
          dateOfFirstSale       = answers.get(DateOfFirstSalePage)
        )
    )

  private def getCompanyName(answers: UserAnswers): ValidationResult[String] =
    answers.vatInfo match {
      case Some(VatCustomerInfo(_, _, _, Some(organisationName))) =>
        organisationName.validNec
      case _ =>
        answers.get(RegisteredCompanyNamePage) match {
          case Some(name) => name.validNec
          case None       => DataMissingError(RegisteredCompanyNamePage).invalidNec
        }
    }

  private def getTradingNames(answers: UserAnswers): ValidationResult[List[String]] = {
    answers.get(HasTradingNamePage) match {
      case Some(true) =>
        answers.get(AllTradingNames) match {
          case Some(Nil) | None => DataMissingError(AllTradingNames).invalidNec
          case Some(list)       => list.validNec
        }

      case Some(false) =>
        List.empty.validNec

      case None =>
        DataMissingError(HasTradingNamePage).invalidNec
    }
  }

  private def getAddress(answers: UserAnswers): ValidationResult[Address] =
    answers.vatInfo match {
      case Some(VatCustomerInfo(address, _, _, _)) => address.validNec
      case None =>
        answers.get(BusinessAddressInUkPage) match {
          case Some(true) =>
            answers.get(UkAddressPage) match {
              case Some(address) => address.validNec
              case None          => DataMissingError(UkAddressPage).invalidNec
            }
          case Some(false) =>
            answers.get(InternationalAddressPage) match {
              case Some(address) => address.validNec
              case None          => DataMissingError(InternationalAddressPage).invalidNec
            }
          case None =>
            answers.get(UkAddressPage) match {
              case Some(address) => address.validNec
              case None          => DataMissingError(BusinessAddressInUkPage).invalidNec
            }
        }
    }

  private def getRegistrationDate(answers: UserAnswers): ValidationResult[LocalDate] =
    answers.vatInfo match {
      case Some(VatCustomerInfo(_, Some(registrationDate), _, _)) =>
        registrationDate.validNec
      case _ =>
        answers.get(UkVatEffectiveDatePage) match {
          case Some(date) => date.validNec
          case None       => DataMissingError(UkVatEffectiveDatePage).invalidNec
        }
    }

  private def getPartOfVatGroup(answers: UserAnswers): ValidationResult[Boolean] =
    answers.vatInfo match {
      case Some(VatCustomerInfo(_, _, Some(partOfVatGroup), _)) =>
        partOfVatGroup.validNec
      case _ =>
        answers.get(PartOfVatGroupPage) match {
          case Some(answer) => answer.validNec
          case None         => DataMissingError(PartOfVatGroupPage).invalidNec
        }
    }

  private def getVatDetailSource(answers: UserAnswers): ValidationResult[VatDetailSource] =
    answers.vatInfo match {
      case Some(VatCustomerInfo(_, Some(_), Some(_), Some(_))) => VatDetailSource.Etmp.validNec
      case Some(_)                                             => VatDetailSource.Mixed.validNec
      case None                                                => VatDetailSource.UserEntered.validNec
    }

  private def getVatDetails(answers: UserAnswers): ValidationResult[VatDetails] =
    (
      getRegistrationDate(answers),
      getAddress(answers),
      getPartOfVatGroup(answers),
      getVatDetailSource(answers)
    ).mapN(VatDetails.apply)

  private def getCommencementDate(answers: UserAnswers): ValidationResult[LocalDate] =
    answers.get(DateOfFirstSalePage) match {
      case Some(startDate) => dateService.startDateBasedOnFirstSale(startDate).validNec
      case None            => answers.get(IsPlanningFirstEligibleSalePage) match {
        case Some(true)  => LocalDate.now().validNec
        case _           => DataMissingError(IsPlanningFirstEligibleSalePage).invalidNec
      }
    }

  private def getContactDetails(answers: UserAnswers): ValidationResult[BusinessContactDetails] =
    answers.get(BusinessContactDetailsPage) match {
      case Some(details) => details.validNec
      case None          => DataMissingError(BusinessContactDetailsPage).invalidNec
    }

  private def getBankDetails(answers: UserAnswers): ValidationResult[BankDetails] =
    answers.get(BankDetailsPage) match {
      case Some(bankDetails) => bankDetails.validNec
      case None              => DataMissingError(BankDetailsPage).invalidNec
    }

  private def getPreviousRegistrations(answers: UserAnswers): ValidationResult[List[PreviousRegistration]] = {
    answers.get(PreviouslyRegisteredPage) match {
      case Some(true) =>
        answers.get(AllPreviousRegistrationsRawQuery) match {
          case None =>
            DataMissingError(AllPreviousRegistrationsRawQuery).invalidNec
          case Some(details) =>
            details.value.zipWithIndex.map {
              case(_, index) =>
                processPreviousRegistration(answers, Index(index))
            }.toList.sequence
        }

      case Some(false) =>
        List.empty.validNec

      case None =>
        DataMissingError(PreviouslyRegisteredPage).invalidNec
    }
  }

  private def processPreviousRegistration(answers: UserAnswers, index: Index): ValidationResult[PreviousRegistration] =
    answers.get(PreviousEuCountryPage(index)) match {
      case Some(country) =>
        answers.get(PreviousEuVatNumberPage(index)) match {
          case Some(vatNumber) =>
            PreviousRegistration(country, vatNumber).validNec
          case None =>
            DataMissingError(PreviousEuVatNumberPage(index)).invalidNec
        }
      case None =>
        DataMissingError(PreviousEuCountryPage(index)).invalidNec
    }

  private def getEuTaxRegistrations(answers: UserAnswers): ValidationResult[List[EuTaxRegistration]] = {
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

  private def processEuDetail(answers: UserAnswers, index: Index): ValidationResult[EuTaxRegistration] = {
    answers.get(EuCountryPage(index)) match {
      case Some(country) =>
        answers.get(HasFixedEstablishmentPage(index)) match {
          case Some(true) =>
            getRegistrationWithFixedEstablishment(answers, country, index)

          case Some(false) =>
            answers.get(VatRegisteredPage(index)) match {
              case Some(_) =>
                getEuVatRegistration(answers, country, index)
              case None =>
                DataMissingError(VatRegisteredPage(index)).invalidNec
            }

          case None =>
            DataMissingError(HasFixedEstablishmentPage(index)).invalidNec
        }

      case None =>
        DataMissingError(EuCountryPage(index)).invalidNec
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

  private def getEuTaxIdentifier(answers: UserAnswers, index: Index): ValidationResult[EuTaxIdentifier] =
    answers.get(VatRegisteredPage(index)) match {
      case Some(true) =>
        getEuVatNumber(answers, index).map(EuTaxIdentifier(Vat, _))

      case Some(false) =>
        answers.get(EuTaxReferencePage(index)) match {
          case Some(value) => EuTaxIdentifier(Other, value).validNec
          case None        => DataMissingError(EuTaxReferencePage(index)).invalidNec
        }

      case None =>
        DataMissingError(VatRegisteredPage(index)).invalidNec
    }

  private def getEuVatRegistration(answers: UserAnswers, country: Country, index: Index): ValidationResult[EuTaxRegistration] = {
    (
      getEuTaxIdentifier(answers, index),
      getEuSendGoods(answers, index),
      getEuSendGoodsTradingName(answers, index),
      getEuSendGoodsAddress(answers, index)
    ).mapN {
      case (euTaxIdentifier, euSendGoods, euSendGoodsTradingName, euSendGoodsAddress) =>
        if(euSendGoods){
          RegistrationWithoutFixedEstablishment(country, euTaxIdentifier, euSendGoods)
        } else {
          RegistrationWithoutFixedEstablishmentSendingGoods(country, euTaxIdentifier, euSendGoods, euSendGoodsTradingName, euSendGoodsAddress)
        }
    }
  }

  private def getEuVatNumber(answers: UserAnswers, index: Index): ValidationResult[String] =
    answers.get(EuVatNumberPage(index)) match {
      case Some(vatNumber) => vatNumber.validNec
      case None            => DataMissingError(EuVatNumberPage(index)).invalidNec
    }

  private def getEuSendGoods(answers: UserAnswers, index: Index): ValidationResult[Boolean] =
    answers.get(pages.euDetails.EuSendGoodsPage(index)) match {
      case Some(sendsGoods) => sendsGoods.validNec
      case None            => DataMissingError(pages.euDetails.EuSendGoodsPage(index)).invalidNec
    }

  private def getFixedEstablishment(answers: UserAnswers, index: Index): ValidationResult[FixedEstablishment] =
    (
      getFixedEstablishmentTradingName(answers, index),
      getFixedEstablishmentAddress(answers, index)
    ).mapN(FixedEstablishment.apply)

  private def getFixedEstablishmentTradingName(answers: UserAnswers, index: Index): ValidationResult[String] =
    answers.get(FixedEstablishmentTradingNamePage(index)) match {
      case Some(name) => name.validNec
      case None       => DataMissingError(FixedEstablishmentTradingNamePage(index)).invalidNec
    }

  private def getFixedEstablishmentAddress(answers: UserAnswers, index: Index): ValidationResult[InternationalAddress] =
    answers.get(FixedEstablishmentAddressPage(index)) match {
      case Some(address) => address.validNec
      case None          => DataMissingError(FixedEstablishmentAddressPage(index)).invalidNec
    }

  private def getWebsites(answers: UserAnswers): ValidationResult[List[String]] =
    answers.get(HasWebsitePage) match {
      case Some(true) =>
        answers.get(AllWebsites) match {
          case Some(Nil) | None => DataMissingError(AllWebsites).invalidNec
          case Some(list)       => list.validNec
        }

      case Some(false) =>
        List.empty.validNec

      case None =>
        DataMissingError(HasWebsitePage).invalidNec
    }

  private def getOnlineMarketplace(answers: UserAnswers): ValidationResult[Boolean] =
    answers.get(IsOnlineMarketplacePage) match {
      case Some(answer) => answer.validNec
      case None         => DataMissingError(IsOnlineMarketplacePage).invalidNec
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
              case None         => None.validNec
            }

          case None =>
            None.validNec
        }

      case None =>
        None.validNec
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
}
