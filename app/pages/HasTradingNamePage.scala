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

package pages

import controllers.routes
import controllers.amend.{routes => amendRoutes}
import models.{AmendMode, CheckMode, Index, NormalMode, UserAnswers}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.AllTradingNames

import scala.util.Try

case object HasTradingNamePage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "hasTradingName"

  override protected def navigateInNormalMode(answers: UserAnswers): Call = answers.get(HasTradingNamePage) match {
      case Some(true)  => routes.TradingNameController.onPageLoad(NormalMode, Index(0))
      case Some(false) => routes.HasMadeSalesController.onPageLoad(NormalMode)
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInCheckMode(answers: UserAnswers): Call =
    (answers.get(HasTradingNamePage), answers.get(AllTradingNames)) match {
      case (Some(true), Some(tradingNames)) if tradingNames.nonEmpty => routes.CheckYourAnswersController.onPageLoad()
      case (Some(true), _)                                           => routes.TradingNameController.onPageLoad(CheckMode, Index(0))
      case (Some(false), _)                                          => routes.CheckYourAnswersController.onPageLoad()
      case _                                                         => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInAmendMode(answers: UserAnswers): Call =
    (answers.get(HasTradingNamePage), answers.get(AllTradingNames)) match {
      case (Some(true), Some(tradingNames)) if tradingNames.nonEmpty => amendRoutes.ChangeYourRegistrationController.onPageLoad()
      case (Some(true), _)                                           => routes.TradingNameController.onPageLoad(AmendMode, Index(0))
      case (Some(false), _)                                          => amendRoutes.ChangeYourRegistrationController.onPageLoad()
      case _                                                         => routes.JourneyRecoveryController.onPageLoad()
    }

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(false) => userAnswers.remove(AllTradingNames)
      case _           => super.cleanup(value, userAnswers)
    }
}
