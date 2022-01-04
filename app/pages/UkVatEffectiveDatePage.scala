/*
 * Copyright 2022 HM Revenue & Customs
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

package pages

import controllers.routes
import models.{NormalMode, UserAnswers}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import java.time.LocalDate

case object UkVatEffectiveDatePage extends QuestionPage[LocalDate] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "ukVatEffectiveDate"

  override protected def navigateInNormalMode(answers: UserAnswers): Call =
    (answers.get(BusinessBasedInNiPage), answers.vatInfo) match {
      case (_, Some(vatInfo)) if vatInfo.address.line1.nonEmpty           => routes.HasTradingNameController.onPageLoad(NormalMode)
      case (Some(true), None)                                             => routes.UkAddressController.onPageLoad(NormalMode)
      case (Some(false), None)                                            => routes.BusinessAddressInUkController.onPageLoad(NormalMode)
      case _                                                              => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInCheckMode(answers: UserAnswers): Call =
    routes.CheckYourAnswersController.onPageLoad()
}
