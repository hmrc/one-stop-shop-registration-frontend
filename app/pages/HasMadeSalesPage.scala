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

package pages

import controllers.routes
import models.{CheckMode, NormalMode, UserAnswers}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object HasMadeSalesPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "hasMadeSales"

  override protected def navigateInNormalMode(answers: UserAnswers): Call = answers.get(HasMadeSalesPage) match {
    case Some(true)      => routes.DateOfFirstSaleController.onPageLoad(NormalMode)
    case Some(false)     => routes.IsPlanningFirstEligibleSaleController.onPageLoad(NormalMode)
    case _               => routes.JourneyRecoveryController.onPageLoad()
  }

  override protected def navigateInCheckMode(answers: UserAnswers): Call = answers.get(HasMadeSalesPage) match {
    case Some(true)      => routes.DateOfFirstSaleController.onPageLoad(CheckMode)
    case Some(false)     => routes.IsPlanningFirstEligibleSaleController.onPageLoad(CheckMode)
    case _               => routes.JourneyRecoveryController.onPageLoad()
  }
}
