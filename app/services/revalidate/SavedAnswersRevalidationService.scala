/*
 * Copyright 2026 HM Revenue & Customs
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

package services.revalidate

import controllers.revalidation.routes
import models.PreviousScheme
import models.core.Match
import models.domain.VatCustomerInfo
import models.euDetails.EuOptionalDetails
import models.previousRegistrations.{PreviousRegistrationDetailsWithOptionalVatNumber, SchemeDetailsWithOptionalVatNumber, SchemeNumbersWithOptionalVatNumber}
import models.requests.AuthenticatedDataRequest
import pages.euDetails.TaxRegisteredInEuPage
import pages.previousRegistrations.PreviouslyRegisteredPage
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import queries.AllEuOptionalDetailsQuery
import queries.previousRegistration.AllPreviousRegistrationsWithOptionalVatNumberQuery
import repositories.AuthenticatedUserAnswersRepository
import services.CoreRegistrationValidationService
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureSyntax.FutureOps

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SavedAnswersRevalidationService @Inject()(
                                                 coreRegistrationValidationService: CoreRegistrationValidationService,
                                                 authenticatedUserAnswersRepository: AuthenticatedUserAnswersRepository,
                                                 clock: Clock
                                               )(implicit ec: ExecutionContext) extends SetActiveTraderResult {

  def revalidateSavedUserAnswers()(implicit hc: HeaderCarrier, request: AuthenticatedDataRequest[_]): Future[Option[Result]] = {
    revalidateUKVrn().flatMap {
      case None =>
        checkPreviousRegistrations().flatMap {
          case None =>
            checkEuDetails()

          case redirectUrl => redirectUrl.toFuture
        }

      case redirectUrl => redirectUrl.toFuture
    }
  }

  private def checkEuDetails()(implicit hc: HeaderCarrier, request: AuthenticatedDataRequest[_]): Future[Option[Result]] = {
    request.userAnswers.get(TaxRegisteredInEuPage) match {
      case Some(true) =>
        val euDetails: List[EuOptionalDetails] = request.userAnswers.get(AllEuOptionalDetailsQuery).getOrElse(List.empty)
        checkAllEuDetails(euDetails)

      case _ => None.toFuture
    }
  }

  private def checkAllEuDetails(
                                 allEuDetails: List[EuOptionalDetails]
                               )(implicit hc: HeaderCarrier, request: AuthenticatedDataRequest[_]): Future[Option[Result]] = {
    allEuDetails match {
      case ::(euDetails, remaining) =>
        revalidateEuDetails(euDetails).flatMap {
          case Some(redirectUrl) =>
            Some(redirectUrl).toFuture

          case _ =>
            checkAllEuDetails(remaining)
        }

      case Nil => None.toFuture
    }
  }

  private def revalidateEuDetails(
                                   euDetails: EuOptionalDetails
                                 )(implicit hc: HeaderCarrier, request: AuthenticatedDataRequest[_]): Future[Option[Result]] = {
    euDetails.euVatNumber match {
      case Some(euVatNumber) =>
        revalidateEuVrn(euVatNumber, euDetails.euCountry.code, !euDetails.sellsGoodsToEUConsumers.getOrElse(false))

      case _ =>
        euDetails.euTaxReference match {
          case Some(euTaxReference) =>
            revalidateEuTaxReference(euTaxReference, euDetails.euCountry.code)

          case _ => None.toFuture
        }
    }
  }

  private def revalidateEuTaxReference(
                                        euTaxReference: String,
                                        countryCode: String
                                      )(implicit hc: HeaderCarrier, request: AuthenticatedDataRequest[_]): Future[Option[Result]] = {
    coreRegistrationValidationService.searchEuTaxId(euTaxReference, countryCode).flatMap { maybeActiveMatch =>
      activeMatchRedirectUrl(maybeActiveMatch)
    }
  }

  private def revalidateEuVrn(
                               euVrn: String,
                               countryCode: String,
                               isOtherMS: Boolean
                             )(implicit hc: HeaderCarrier, request: AuthenticatedDataRequest[_]): Future[Option[Result]] = {
    coreRegistrationValidationService.searchEuVrn(euVrn, countryCode, isOtherMS).flatMap { maybeActiveMatch =>
      activeMatchRedirectUrl(maybeActiveMatch)
    }
  }

  private def checkPreviousRegistrations()(implicit hc: HeaderCarrier, request: AuthenticatedDataRequest[_]): Future[Option[Result]] = {
    request.userAnswers.get(PreviouslyRegisteredPage) match {
      case Some(true) =>
        val allPreviousRegistrations: List[PreviousRegistrationDetailsWithOptionalVatNumber] =
          request.userAnswers.get(AllPreviousRegistrationsWithOptionalVatNumberQuery).getOrElse(List.empty)

        revalidateAllPreviousRegistrations(allPreviousRegistrations)

      case _ => None.toFuture
    }
  }

  private def revalidatePreviousSchemeDetails(
                                               countryCode: String,
                                               allPreviousSchemeDetails: List[SchemeDetailsWithOptionalVatNumber]
                                             )(implicit hc: HeaderCarrier, request: AuthenticatedDataRequest[_]): Future[Option[Result]] = {
    allPreviousSchemeDetails match {
      case Nil => None.toFuture

      case ::(SchemeDetailsWithOptionalVatNumber(
        Some(previousScheme),
        Some(SchemeNumbersWithOptionalVatNumber(
          Some(previousSchemeNumber),
          previousIntermediaryNumber,
        ))
      ), remaining) =>
        coreRegistrationValidationService.searchScheme(
          searchNumber = previousSchemeNumber,
          previousScheme = previousScheme,
          intermediaryNumber = previousIntermediaryNumber,
          countryCode = countryCode
        ).flatMap { maybeMatch =>
          activeMatchRedirectUrl(maybeMatch, Some(previousScheme)).flatMap {
            case Some(result) =>
              Some(result).toFuture

            case _ =>
              revalidatePreviousSchemeDetails(countryCode, remaining)
          }
        }

      case ::(_, remaining) =>
        revalidatePreviousSchemeDetails(countryCode, remaining)
    }
  }

  private def revalidateAllPreviousRegistrations(
                                                  allPreviousRegistrations: List[PreviousRegistrationDetailsWithOptionalVatNumber]
                                                )(implicit hc: HeaderCarrier, request: AuthenticatedDataRequest[_]): Future[Option[Result]] = {
    allPreviousRegistrations match {
      case Nil => None.toFuture

      case ::(PreviousRegistrationDetailsWithOptionalVatNumber(
        country,
        Some(optionalSchemeDetails)
      ), remaining) =>
        revalidatePreviousSchemeDetails(
          countryCode = country.code,
          allPreviousSchemeDetails = optionalSchemeDetails
        ).flatMap {
          case Some(result) =>
            Some(result).toFuture

          case _ =>
            revalidateAllPreviousRegistrations(remaining)
        }

      case ::(_, remaining) =>
        revalidateAllPreviousRegistrations(remaining)
    }
  }

  private def activeMatchRedirectUrl(
                                      maybeMatch: Option[Match],
                                      previousScheme: Option[PreviousScheme] = None
                                    )(implicit request: AuthenticatedDataRequest[_]): Future[Option[Result]] = {
    (maybeMatch, previousScheme) match {
      case (Some(activeMatch), Some(previousScheme)) if activeMatch.isActiveTrader && PreviousScheme.OSSU == previousScheme =>
        setActiveTraderResultAndRedirect(
          activeMatch = activeMatch,
          sessionRepository = authenticatedUserAnswersRepository,
          redirect = routes.RevalidateAlreadyRegisteredController.onPageLoad()
        ).flatMap { result =>
          Some(result).toFuture
        }

      case (Some(activeMatch), None) if activeMatch.isActiveTrader =>
        setActiveTraderResultAndRedirect(
          activeMatch = activeMatch,
          sessionRepository = authenticatedUserAnswersRepository,
          redirect = routes.RevalidateAlreadyRegisteredController.onPageLoad()
        ).flatMap { result =>
          Some(result).toFuture
        }

      case (Some(activeMatch), _)if activeMatch.isQuarantinedTrader(clock) =>
        Some(Redirect(routes.RevalidateQuarantinedTraderController.onPageLoad(
          exclusionExpiryDate = activeMatch.getEffectiveDate
        ).url)).toFuture

      case _ => None.toFuture
    }
  }


  private def revalidateUKVrn()(implicit hc: HeaderCarrier, request: AuthenticatedDataRequest[_]): Future[Option[Result]] = {
    if (checkVrnExpired(request.userAnswers.vatInfo)) {
      Some(Redirect(routes.RevalidateVrnExpiredController.onPageLoad().url)).toFuture
    } else {
      coreRegistrationValidationService.searchUkVrn(request.vrn).flatMap { maybeMatch =>
        activeMatchRedirectUrl(maybeMatch)
      }
    }
  }

  private def checkVrnExpired(vatCustomerInfo: Option[VatCustomerInfo]): Boolean = {
    vatCustomerInfo match {
      case Some(vatInfo) =>
        vatInfo.deregistrationDecisionDate.exists(!_.isAfter(LocalDate.now(clock)))

      case _ => false
    }
  }
}
