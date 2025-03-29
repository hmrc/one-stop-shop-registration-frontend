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

package pages

import controllers.routes
import models.{CheckVatDetails, NormalMode, UserAnswers}
import models.CheckVatDetails._
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.AllTradingNames

case object CheckVatDetailsPage extends QuestionPage[CheckVatDetails] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "checkVatDetails"

  override def navigateInNormalMode(answers: UserAnswers): Call =
    (answers.get(CheckVatDetailsPage), answers.vatInfo, answers.get(AllTradingNames)) match {
      case (Some(Yes), Some(vatInfo), Some(tradingNames)) if tradingNames.nonEmpty     => routes.AddTradingNameController.onPageLoad(NormalMode)
      case (Some(Yes), Some(vatInfo), _) if vatInfo.address.line1.nonEmpty             => routes.HasTradingNameController.onPageLoad(NormalMode)
      case (Some(WrongAccount), _, _)                                                  => routes.UseOtherAccountController.onPageLoad()
      case (Some(DetailsIncorrect), _, _)                                              => routes.UpdateVatDetailsController.onPageLoad()
      case _                                                                           => routes.JourneyRecoveryController.onPageLoad()
    }
}
