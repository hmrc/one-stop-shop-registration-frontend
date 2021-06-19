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
import models.{AlreadyMadeSales, CheckMode, NormalMode, UserAnswers}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try


case object AlreadyMadeSalesPage extends QuestionPage[AlreadyMadeSales] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "alreadyMadeSales"

  override def navigateInNormalMode(answers: UserAnswers): Call = answers.get(AlreadyMadeSalesPage) match {
    case Some(x) if x.answer => routes.CommencementDateController.onPageLoad(NormalMode)
    case Some(_)             => routes.IntendToSellGoodsThisQuarterController.onPageLoad(NormalMode)
    case None                => routes.JourneyRecoveryController.onPageLoad()
  }

  override protected def navigateInCheckMode(answers: UserAnswers): Call =
    (answers.get(AlreadyMadeSalesPage), answers.get(IntendToSellGoodsThisQuarterPage)) match {
      case (Some(x), _) if x.answer        => routes.CommencementDateController.onPageLoad(CheckMode)
      case (Some(x), None) if !x.answer    => routes.IntendToSellGoodsThisQuarterController.onPageLoad(CheckMode)
      case (Some(x), Some(_)) if !x.answer => routes.CheckYourAnswersController.onPageLoad()
      case _                               => routes.JourneyRecoveryController.onPageLoad()
    }

  override def cleanup(value: Option[AlreadyMadeSales], userAnswers: UserAnswers): Try[UserAnswers] = value match {
    case Some(x) if !x.answer => super.cleanup(value, userAnswers)
    case _                    => userAnswers.remove(IntendToSellGoodsThisQuarterPage)
  }
}
