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

package services

import controllers.{SetActiveTraderResult, routes}
import models.core.Match
import models.domain.VatCustomerInfo
import models.previousRegistrations.{PreviousRegistrationDetailsWithOptionalVatNumber, SchemeDetailsWithOptionalVatNumber, SchemeNumbersWithOptionalVatNumber}
import models.requests.AuthenticatedDataRequest
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import repositories.AuthenticatedUserAnswersRepository
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
      case None => None.toFuture
      case redirectUrl => redirectUrl.toFuture
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
          Some(previousIntermediaryNumber),
        ))
      ), remaining) =>
        coreRegistrationValidationService.searchScheme(
          searchNumber = previousSchemeNumber,
          previousScheme = previousScheme,
          intermediaryNumber = Some(previousIntermediaryNumber),
          countryCode = countryCode
        ).flatMap { maybeMatch =>
          activeMatchRedirectUrl(maybeMatch).flatMap {
            case Some(result) =>
              Some(result).toFuture

            case _ =>
              revalidatePreviousSchemeDetails(countryCode, remaining)
          }
        }
        
      case ::(_, remaining) =>
        revalidatePreviousSchemeDetails(countryCode, allPreviousSchemeDetails)
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

  private def activeMatchRedirectUrl(maybeMatch: Option[Match])(implicit request: AuthenticatedDataRequest[_]): Future[Option[Result]] = {
    maybeMatch match {
      case Some(activeMatch) if activeMatch.isActiveTrader =>
        setActiveTraderResultAndRedirect(
          activeMatch = activeMatch,
          sessionRepository = authenticatedUserAnswersRepository,
          redirect = routes.AlreadyRegisteredController.onPageLoad()
        ).flatMap { result =>
          Some(result).toFuture
        }

      case Some(activeMatch) if activeMatch.isQuarantinedTrader(clock) =>
        Some(Redirect(routes.OtherCountryExcludedAndQuarantinedController.onPageLoad(activeMatch.memberState, activeMatch.getEffectiveDate).url)).toFuture

      case _ => None.toFuture
    }
  }


  private def revalidateUKVrn()(implicit hc: HeaderCarrier, request: AuthenticatedDataRequest[_]): Future[Option[Result]] = {
    if (checkVrnExpired(request.userAnswers.vatInfo)) {
      // TODO -> Redirect to new page? Expired VRN
      Some(Redirect(routes.JourneyRecoveryController.onPageLoad().url)).toFuture
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
