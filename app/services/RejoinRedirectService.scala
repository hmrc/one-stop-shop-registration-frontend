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
import models.core.Match
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

import java.time.Clock

object RejoinRedirectService extends Logging {

  def redirectOnMatch(maybeMatch: Option[Match], clock: Clock): Option[Result] = maybeMatch match {
    case Some(activeMatch) if activeMatch.isActiveTrader =>
      Some(Redirect(controllers.rejoin.routes.RejoinAlreadyRegisteredOtherCountryController.onPageLoad(activeMatch.memberState)))
    case Some(activeMatch) if activeMatch.isQuarantinedTrader(clock) =>
      Some(Redirect(controllers.rejoin.routes.CannotRejoinQuarantinedCountryController.onPageLoad(
        activeMatch.memberState, activeMatch.getEffectiveDate)))
    case _ => None
  }

}
