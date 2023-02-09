/*
 * Copyright 2023 HM Revenue & Customs
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
import models.{CheckLoopMode, CheckMode, Country, Index, NormalMode, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class EuCountryPage(countryIndex: Index) extends QuestionPage[Country] {

  override def path: JsPath = JsPath \ "euDetails" \ countryIndex.position \ toString

  override def toString: String = "euCountry"

  override protected def navigateInNormalMode(answers: UserAnswers): Call = {
    euRoutes.SellsGoodsToEUConsumersController.onPageLoad(NormalMode, countryIndex)
  }

  override protected def navigateInCheckMode(answers: UserAnswers): Call = {
    answers.get(EuVatNumberPage(countryIndex)) match {
      case Some(_) => EuVatNumberPage(countryIndex).navigate(CheckMode, answers)
      case None => euRoutes.EuVatNumberController.onPageLoad(CheckMode, countryIndex)
    }
  }

  override protected def navigateInCheckLoopMode(answers: UserAnswers): Call = {
    answers.get(EuVatNumberPage(countryIndex)) match {
      case Some(_) => EuVatNumberPage(countryIndex).navigate(CheckLoopMode, answers)
      case None => euRoutes.EuVatNumberController.onPageLoad(CheckLoopMode, countryIndex)
    }
  }
}
