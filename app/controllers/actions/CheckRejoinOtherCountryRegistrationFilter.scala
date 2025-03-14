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
import logging.Logging
import models.core.MatchType
import models.requests.AuthenticatedDataRequest
import models.{Mode, RejoinLoopMode, RejoinMode}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import services.CoreRegistrationValidationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckRejoinOtherCountryRegistrationFilterImpl(mode: Option[Mode],
                                                    coreRegistrationValidationService: CoreRegistrationValidationService,
                                                    appConfig: FrontendAppConfig)
                                                   (implicit val executionContext: ExecutionContext)
  extends ActionFilter[AuthenticatedDataRequest] with Logging {

  private val exclusionStatusCode = 4

  override protected def filter[A](request: AuthenticatedDataRequest[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    if (appConfig.otherCountryRegistrationValidationEnabled && Seq(RejoinMode, RejoinLoopMode).exists(mode.contains)) {
      coreRegistrationValidationService.searchUkVrn(request.vrn)(hc, request).map {
        case Some(activeMatch) if activeMatch.matchType == MatchType.OtherMSNETPActiveNETP || activeMatch.matchType == MatchType.FixedEstablishmentActiveNETP =>
          Some(Redirect(controllers.rejoin.routes.RejoinAlreadyRegisteredOtherCountryController.onPageLoad(activeMatch.memberState)))

        case Some(activeMatch) if activeMatch.exclusionStatusCode.contains(exclusionStatusCode) ||
          activeMatch.matchType == MatchType.OtherMSNETPQuarantinedNETP ||
          activeMatch.matchType == MatchType.FixedEstablishmentQuarantinedNETP =>
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
    } else {
      Future.successful(None)
    }
  }
}

class CheckRejoinOtherCountryRegistrationFilter @Inject()(coreRegistrationValidationService: CoreRegistrationValidationService, appConfig: FrontendAppConfig)
                                                         (implicit val executionContext: ExecutionContext) {

  def apply(mode: Option[Mode]): CheckRejoinOtherCountryRegistrationFilterImpl = new CheckRejoinOtherCountryRegistrationFilterImpl(mode, coreRegistrationValidationService, appConfig)
}

