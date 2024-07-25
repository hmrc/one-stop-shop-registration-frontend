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

package utils

import models.{AmendMode, Mode, RejoinMode}
import models.requests.AuthenticatedDataRequest
import play.api.mvc.AnyContent
import services.DateService

import java.time.Clock
import java.time.format.DateTimeFormatter

trait DateOfFirstSaleUtil {

  val dateService: DateService
  val clock: Clock

  def getEarliestDateAllowed(mode: Mode, dateTimeFormatter: DateTimeFormatter)(implicit request: AuthenticatedDataRequest[AnyContent]) = {
    if (mode == AmendMode || mode == RejoinMode) {
      request.registration.flatMap(_.submissionReceived) match {
        case Some(submissionReceived) =>
          dateService.earliestSaleAllowed(submissionReceived.atZone(clock.getZone).toLocalDate).format(dateTimeFormatter)
        case _ => dateService.earliestSaleAllowed().format(dateTimeFormatter)
      }
    } else {
      dateService.earliestSaleAllowed().format(dateTimeFormatter)
    }
  }

}
