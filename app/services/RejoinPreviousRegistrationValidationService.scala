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

import logging.Logging
import models.Country
import models.domain.{PreviousRegistration, PreviousRegistrationLegacy, PreviousRegistrationNew, PreviousSchemeDetails}
import models.requests.AuthenticatedOptionalDataRequest
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RejoinPreviousRegistrationValidationService @Inject()(coreRegistrationValidationService: CoreRegistrationValidationService)
                                                           (implicit ec: ExecutionContext) extends Logging {

  def validatePreviousRegistrations(previousRegistrations: Seq[PreviousRegistration])
                                   (implicit hc: HeaderCarrier, request: AuthenticatedOptionalDataRequest[_]): Future[Option[Result]] = {
    previousRegistrations match {
      case head +: tail => validateSinglePreviousRegistration(head).flatMap {
        case None => validatePreviousRegistrations(tail)
        case result => Future.successful(result)
      }
      case _ => Future.successful(None)
    }
  }

  private def validateSinglePreviousRegistration(previousRegistration: PreviousRegistration)
                                                (implicit hc: HeaderCarrier, request: AuthenticatedOptionalDataRequest[_]): Future[Option[Result]] = {
    previousRegistration match {
      case previousRegistrationNew: PreviousRegistrationNew =>
        validateSchemeDetails(previousRegistrationNew.previousSchemesDetails, previousRegistrationNew.country)
      case _: PreviousRegistrationLegacy => Future.successful(None)
    }
  }

  private def validateSchemeDetails(previousSchemeDetails: Seq[PreviousSchemeDetails], country: Country)
                                   (implicit hc: HeaderCarrier, request: AuthenticatedOptionalDataRequest[_]): Future[Option[Result]] = {
    previousSchemeDetails match {
      case head +: tail => validateSinglePreviousSchemeDetails(head, country).flatMap {
        case None => validateSchemeDetails(tail, country)
        case result => Future.successful(result)
      }
      case _ => Future.successful(None)
    }
  }

  private def validateSinglePreviousSchemeDetails(previousSchemeDetails: PreviousSchemeDetails, country: Country)
                                                 (implicit hc: HeaderCarrier, request: AuthenticatedOptionalDataRequest[_]): Future[Option[Result]] = {
    coreRegistrationValidationService.searchScheme(
      searchNumber = previousSchemeDetails.previousSchemeNumbers.previousSchemeNumber,
      previousScheme = previousSchemeDetails.previousScheme,
      intermediaryNumber = previousSchemeDetails.previousSchemeNumbers.previousIntermediaryNumber,
      countryCode = country.code
    ).map {
      case Some(activeMatch) if coreRegistrationValidationService.isActiveTrader(activeMatch) =>
        Some(Redirect(controllers.rejoin.routes.RejoinAlreadyRegisteredOtherCountryController.onPageLoad(activeMatch.memberState)))
      case Some(activeMatch) if coreRegistrationValidationService.isQuarantinedTrader(activeMatch) =>
        Some(Redirect(
          controllers.rejoin.routes.CannotRejoinQuarantinedCountryController.onPageLoad(activeMatch.memberState, activeMatch.exclusionEffectiveDate match {
            case Some(date) => date.toString
            case _ =>
              val e = new IllegalStateException(s"MatchType ${activeMatch.matchType} didn't include an expected exclusion effective date")
              logger.error(s"Must have an Exclusion Effective Date ${e.getMessage}", e)
              throw e
          })
        ))
      case _ => None
    }
  }
}
