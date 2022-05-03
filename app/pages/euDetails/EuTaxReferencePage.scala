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
import controllers.routes
import models.{CheckLoopMode, CheckMode, Index, NormalMode, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class EuTaxReferencePage(index: Index) extends QuestionPage[String] {

  override def path: JsPath = JsPath \ "euDetails" \ index.position \ toString

  override def toString: String = "euTaxReference"

  override protected def navigateInNormalMode(answers: UserAnswers): Call = {
    answers.get(HasFixedEstablishmentPage(index)) match {
      case Some(true) => euRoutes.FixedEstablishmentTradingNameController.onPageLoad(NormalMode, index)
      case Some(false) => euRoutes.EuSendGoodsTradingNameController.onPageLoad(NormalMode, index)
      case _ => routes.JourneyRecoveryController.onPageLoad()
    }
  }

  override protected def navigateInCheckMode(answers: UserAnswers): Call =
    answers.get(HasFixedEstablishmentPage(index)) match {
      case Some(true) =>
        if(answers.get(FixedEstablishmentTradingNamePage(index)).isEmpty) {
          euRoutes.FixedEstablishmentTradingNameController.onPageLoad(CheckMode, index)
        } else {
          FixedEstablishmentTradingNamePage(index).navigate(CheckMode, answers)
        }
      case Some(false) =>
        if(answers.get(EuSendGoodsTradingNamePage(index)).isEmpty) {
          euRoutes.EuSendGoodsTradingNameController.onPageLoad(CheckMode, index)
        } else {
          EuSendGoodsTradingNamePage(index).navigate(CheckMode, answers)
        }
      case _ => routes.JourneyRecoveryController.onPageLoad()
  }

  override protected def navigateInCheckLoopMode(answers: UserAnswers): Call =
    answers.get(HasFixedEstablishmentPage(index)) match {
      case Some(true) =>
        if(answers.get(FixedEstablishmentTradingNamePage(index)).isEmpty) {
          euRoutes.FixedEstablishmentTradingNameController.onPageLoad(CheckLoopMode, index)
        } else {
          FixedEstablishmentTradingNamePage(index).navigate(CheckLoopMode, answers)
        }
      case Some(false) =>
        if(answers.get(EuSendGoodsTradingNamePage(index)).isEmpty) {
          euRoutes.EuSendGoodsTradingNameController.onPageLoad(CheckLoopMode, index)
        } else {
          EuSendGoodsTradingNamePage(index).navigate(CheckLoopMode, answers)
        }
      case _ => routes.JourneyRecoveryController.onPageLoad()
    }
}
