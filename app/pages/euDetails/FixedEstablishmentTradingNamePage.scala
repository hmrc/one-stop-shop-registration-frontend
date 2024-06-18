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

package pages.euDetails

import controllers.euDetails.{routes => euRoutes}
import models.{AmendLoopMode, AmendMode, CheckLoopMode, CheckMode, Index, NormalMode, RejoinLoopMode, RejoinMode, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class FixedEstablishmentTradingNamePage(index: Index) extends QuestionPage[String] {

  override def path: JsPath = JsPath \ "euDetails" \ index.position \ toString

  override def toString: String = "fixedEstablishmentTradingName"

  override protected def navigateInNormalMode(answers: UserAnswers): Call =
    euRoutes.FixedEstablishmentAddressController.onPageLoad(NormalMode, index)

  override protected def navigateInCheckMode(answers: UserAnswers): Call =
    if(answers.get(FixedEstablishmentAddressPage(index)).isDefined) {
      FixedEstablishmentAddressPage(index).navigate(CheckMode, answers)
    } else {
      euRoutes.FixedEstablishmentAddressController.onPageLoad(CheckMode, index)
    }

  override protected def navigateInCheckLoopMode(answers: UserAnswers): Call =
    if(answers.get(FixedEstablishmentAddressPage(index)).isDefined) {
      FixedEstablishmentAddressPage(index).navigate(CheckLoopMode, answers)
    } else {
      euRoutes.FixedEstablishmentAddressController.onPageLoad(CheckLoopMode, index)
    }

  override protected def navigateInAmendMode(answers: UserAnswers): Call =
    euRoutes.FixedEstablishmentAddressController.onPageLoad(AmendMode, index)

  override protected def navigateInAmendLoopMode(answers: UserAnswers): Call =
    if (answers.get(FixedEstablishmentAddressPage(index)).isDefined) {
      FixedEstablishmentAddressPage(index).navigate(AmendLoopMode, answers)
    } else {
      euRoutes.FixedEstablishmentAddressController.onPageLoad(AmendLoopMode, index)
    }

  override protected def navigateInRejoinMode(answers: UserAnswers): Call =
    euRoutes.FixedEstablishmentAddressController.onPageLoad(RejoinMode, index)

  override protected def navigateInRejoinLoopMode(answers: UserAnswers): Call =
    if (answers.get(FixedEstablishmentAddressPage(index)).isDefined) {
      FixedEstablishmentAddressPage(index).navigate(RejoinLoopMode, answers)
    } else {
      euRoutes.FixedEstablishmentAddressController.onPageLoad(RejoinLoopMode, index)
    }

}
