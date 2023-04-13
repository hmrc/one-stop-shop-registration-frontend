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

import cats.implicits._
import models._
import models.domain._
import models.requests.AuthenticatedDataRequest
import pages._
import queries._
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationValidationService @Inject()(dateService: DateService) extends EuTaxRegistrationValidations with PreviousRegistrationsValidations {


  def fromUserAnswers(answers: UserAnswers, vrn: Vrn)
                     (
                       implicit ec: ExecutionContext,
                       hc: HeaderCarrier,
                       request: AuthenticatedDataRequest[_]
                     ): Future[ValidationResult[Registration]] = {
    getCommencementDate(answers).map { validationCommencementDate =>
      (
        getCompanyName(answers),
        getTradingNames(answers),
        getVatDetails(answers),
        getEuTaxRegistrations(answers),
        validationCommencementDate,
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
            euRegistrations = euRegistrations,
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
    }
  }

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

  private def getCommencementDate(answers: UserAnswers)
                                 (
                                   implicit ec: ExecutionContext,
                                   hc: HeaderCarrier,
                                   request: AuthenticatedDataRequest[_]
                                 ): Future[ValidationResult[LocalDate]] = {

    dateService.calculateCommencementDate(answers).map { calculatedCommencementDate =>
      calculatedCommencementDate.validNec
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
