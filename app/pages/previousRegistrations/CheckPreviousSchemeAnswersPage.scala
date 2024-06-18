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

package pages.previousRegistrations

import models.{AmendMode, CheckMode, Index, NormalMode, RejoinMode, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.previousRegistration.DeriveNumberOfPreviousSchemes

case class CheckPreviousSchemeAnswersPage(index: Index) extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "CheckPreviousSchemeAnswers"

  override protected def navigateInNormalMode(answers: UserAnswers): Call =
    (answers.get(CheckPreviousSchemeAnswersPage(index)), answers.get(DeriveNumberOfPreviousSchemes(index))) match {
      case (Some(true), Some(size)) => controllers.previousRegistrations.routes.PreviousSchemeController.onPageLoad(NormalMode, index, Index(size))
      case (Some(false), _) => controllers.previousRegistrations.routes.AddPreviousRegistrationController.onPageLoad(NormalMode)
      case _ => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInCheckMode(answers: UserAnswers): Call =
    (answers.get(CheckPreviousSchemeAnswersPage(index)), answers.get(DeriveNumberOfPreviousSchemes(index))) match {
      case (Some(true), Some(size)) => controllers.previousRegistrations.routes.PreviousSchemeController.onPageLoad(CheckMode, index, Index(size))
      case (Some(false), _) => controllers.previousRegistrations.routes.AddPreviousRegistrationController.onPageLoad(CheckMode)
      case _ => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInAmendMode(answers: UserAnswers): Call =
    (answers.get(CheckPreviousSchemeAnswersPage(index)), answers.get(DeriveNumberOfPreviousSchemes(index))) match {
      case (Some(true), Some(size)) => controllers.previousRegistrations.routes.PreviousSchemeController.onPageLoad(AmendMode, index, Index(size))
      case (Some(false), _) => controllers.previousRegistrations.routes.AddPreviousRegistrationController.onPageLoad(AmendMode)
      case _ => controllers.amend.routes.AmendJourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInRejoinMode(answers: UserAnswers): Call =
    (answers.get(CheckPreviousSchemeAnswersPage(index)), answers.get(DeriveNumberOfPreviousSchemes(index))) match {
      case (Some(true), Some(size)) => controllers.previousRegistrations.routes.PreviousSchemeController.onPageLoad(RejoinMode, index, Index(size))
      case (Some(false), _) => controllers.previousRegistrations.routes.AddPreviousRegistrationController.onPageLoad(RejoinMode)
      case _ => controllers.rejoin.routes.RejoinJourneyRecoveryController.onPageLoad()
    }
}
