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

package pages.euDetails

import controllers.euDetails.{routes => euRoutes}
import models.{CheckMode, Index, InternationalAddress, NormalMode, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class EuSendGoodsAddressPage(index: Index) extends QuestionPage[InternationalAddress] {

  override def path: JsPath = JsPath \ "euDetails" \ index.position \ toString

  override def toString: String = "euSendGoodsAddress"

  override protected def navigateInNormalMode(answers: UserAnswers): Call =
    euRoutes.CheckEuDetailsAnswersController.onPageLoad(NormalMode, index)

  override protected def navigateInCheckMode(answers: UserAnswers): Call =
    euRoutes.CheckEuDetailsAnswersController.onPageLoad(CheckMode, index)

  override protected def navigateInCheckLoopMode(answers: UserAnswers): Call =
    euRoutes.CheckEuDetailsAnswersController.onPageLoad(NormalMode, index)
}
