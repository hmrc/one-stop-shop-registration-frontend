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

package controllers.actions

import config.FrontendAppConfig
import controllers.routes
import logging.Logging
import models.{AmendMode, Mode, RejoinMode}
import models.core.Match
import models.requests.AuthenticatedDataRequest
import play.api.mvc.{ActionFilter, Result}
import play.api.mvc.Results.Redirect
import services.CoreRegistrationValidationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckOtherCountryRegistrationFilterImpl @Inject()(
                                                         mode: Option[Mode],
                                                         service: CoreRegistrationValidationService,
                                                         appConfig: FrontendAppConfig,
                                                         clock: Clock
                                                       )(implicit val executionContext: ExecutionContext)
  extends ActionFilter[AuthenticatedDataRequest] with Logging {

  override protected def filter[A](request: AuthenticatedDataRequest[A]): Future[Option[Result]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    implicit val req: AuthenticatedDataRequest[A] = request

    def getEffectiveDate(activeMatch: Match) = {
      activeMatch.exclusionEffectiveDate match {
        case Some(date) => date
        case _ =>
          val e = new IllegalStateException(s"Exclusion status code ${activeMatch.exclusionStatusCode} didn't include an expected exclusion effective date")
          logger.error(s"Must have an Exclusion Effective Date ${e.getMessage}", e)
          throw e
      }
    }

    if (appConfig.otherCountryRegistrationValidationEnabled && !mode.contains(AmendMode) || !mode.contains(RejoinMode)) {
      service.searchUkVrn(request.vrn).map {

        case Some(activeMatch) if activeMatch.isActiveTrader =>
          Some(Redirect(routes.AlreadyRegisteredOtherCountryController.onPageLoad(activeMatch.memberState)))

        case Some(activeMatch) if activeMatch.isQuarantinedTrader(clock) =>
          Some(Redirect(
            routes.OtherCountryExcludedAndQuarantinedController.onPageLoad(activeMatch.memberState, activeMatch.getEffectiveDate)
          ))

        case _ => None
      }
    } else {
      Future.successful(None)
    }
  }
}

class CheckOtherCountryRegistrationFilter @Inject()(
                                                     service: CoreRegistrationValidationService,
                                                     appConfig: FrontendAppConfig,
                                                     clock: Clock
                                                   )(implicit val executionContext: ExecutionContext) {
  def apply(mode: Option[Mode]): CheckOtherCountryRegistrationFilterImpl = {
    new CheckOtherCountryRegistrationFilterImpl(mode, service, appConfig, clock)
  }
}