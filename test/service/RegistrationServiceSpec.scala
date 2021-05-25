/*
 * Copyright 2021 HM Revenue & Customs
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

package service

import base.SpecBase
import models.UserAnswers
import models.requests.RegistrationRequest
import play.api.libs.json.Json
import testutils.WireMockHelper

import java.time.Instant
import javax.inject.Inject

class RegistrationServiceSpec @Inject()(
                                       registrationService: RegistrationService
                                       ) extends SpecBase with WireMockHelper {

//  private val instant = Instant.now
//  private val userAnswers = UserAnswers("id", Json.obj("foo" -> "bar"), Instant.ofEpochSecond(1))

  "fromUserAnswers" - {

    "must return a Registration request when user answers are provided" in {

      val registrationRequestOpt = registrationService.fromUserAnswers(emptyUserAnswers)

      registrationRequestOpt mustBe Some(RegistrationRequest)
    }

  }

}
