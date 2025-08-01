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

package services

import cats.implicits.*
import logging.Logging
import models.*
import models.domain.*
import models.previousRegistrations.NonCompliantDetails
import models.requests.AuthenticatedDataRequest
import pages.*
import queries.*
import queries.previousRegistration.AllPreviousRegistrationsQuery
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationValidationService @Inject()(
                                               dateService: DateService,
                                               registrationService: RegistrationService
                                             )
  extends EuTaxRegistrationValidations with PreviousRegistrationsValidations with Logging {


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
        getNonCompliantDetails(answers)
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
          niPresence,
          nonCompliantDetails
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
            dateOfFirstSale = answers.get(DateOfFirstSalePage),
            nonCompliantReturns = nonCompliantDetails.flatMap(_.nonCompliantReturns.map(_.toString)),
            nonCompliantPayments = nonCompliantDetails.flatMap(_.nonCompliantPayments.map(_.toString))
          )
      )
    }
  }

  private def getCompanyName(answers: UserAnswers): ValidationResult[String] = {
    answers.vatInfo match {
      case Some(vatInfo) =>
        vatInfo.organisationName match {
          case Some(organisationName) => organisationName.validNec
          case _ =>
            vatInfo.individualName match {
              case Some(individualName) => individualName.validNec
              case _ => DataMissingError(CheckVatDetailsPage).invalidNec
            }
        }
      case _ => DataMissingError(CheckVatDetailsPage).invalidNec
    }
  }

  private def getTradingNames(answers: UserAnswers): ValidationResult[List[String]] = {
    answers.get(HasTradingNamePage) match {
      case Some(true) =>
        answers.get(AllTradingNames) match {
          case Some(Nil) | None => DataMissingError(AllTradingNames).invalidNec
          case Some(list) => list.validNec
        }

      case Some(false) =>
        answers.get(AllTradingNames) match {
          case Some(Nil) | None => List.empty.validNec
          case Some(_) => DataMissingError(HasTradingNamePage).invalidNec
        }

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


    if (registrationService.eligibleSalesDifference(request.registration, answers)) {
      dateService.calculateCommencementDate(answers).map {
        case Some(calculatedCommencementDate) => calculatedCommencementDate.validNec
        case _ => DataMissingError(DateOfFirstSalePage).invalidNec
      }
    } else {
      request.registration match {
        case Some(registration) => Future.successful(registration.commencementDate.validNec)
        case _ => val exception = new IllegalStateException("We were expecting a registration here")
          logger.error(exception.getMessage, exception)
          throw exception
      }
    }


  }

  private def getContactDetails(answers: UserAnswers): ValidationResult[BusinessContactDetails] = {
    answers.get(BusinessContactDetailsPage) match {
      case Some(details) => details.validNec
      case None => DataMissingError(BusinessContactDetailsPage).invalidNec
    }
  }

  private def getBankDetails(answers: UserAnswers): ValidationResult[BankDetails] = {
    answers.get(BankDetailsPage) match {
      case Some(bankDetails) => bankDetails.validNec
      case None => DataMissingError(BankDetailsPage).invalidNec
    }
  }

  private def getWebsites(answers: UserAnswers): ValidationResult[List[String]] = {
    answers.get(HasWebsitePage) match {
      case Some(true) =>
        answers.get(AllWebsites) match {
          case Some(Nil) | None => DataMissingError(AllWebsites).invalidNec
          case Some(list) => list.validNec
        }

      case Some(false) =>
        answers.get(AllWebsites) match {
          case Some(Nil) | None => List.empty.validNec
          case Some(_) => DataMissingError(HasWebsitePage).invalidNec
        }

      case None =>
        DataMissingError(HasWebsitePage).invalidNec
    }
  }

  private def getOnlineMarketplace(answers: UserAnswers): ValidationResult[Boolean] = {
    answers.get(IsOnlineMarketplacePage) match {
      case Some(answer) => answer.validNec
      case None => DataMissingError(IsOnlineMarketplacePage).invalidNec
    }
  }

  private def getNiPresence(answers: UserAnswers): ValidationResult[Option[NiPresence]] = {
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

  private def getNonCompliantDetails(answers: UserAnswers): ValidationResult[Option[NonCompliantDetails]] = {
    answers.get(AllPreviousRegistrationsQuery) match {
      case None =>
        None.validNec

      case Some(allPreviousRegistrations) =>
        val maybeNonCompliantDetails = allPreviousRegistrations
          .flatMap(_.previousSchemesDetails)
          .flatMap(_.nonCompliantDetails)

        maybeNonCompliantDetails match {
          case Nil =>
            None.validNec
          case nonCompliantDetailsList =>
            Some(nonCompliantDetailsList.maxBy { nonCompliantDetails =>
              nonCompliantDetails.nonCompliantReturns.getOrElse(0) +
                nonCompliantDetails.nonCompliantPayments.getOrElse(0)
            }).validNec
        }
    }
  }
}
