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

import controllers.amend.routes as amendRoutes
import controllers.rejoin.routes as rejoinRoutes
import controllers.routes
import models.{AmendMode, CheckMode, Index, NormalMode, RejoinMode, UserAnswers}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.DeriveNumberOfWebsites

case object AddWebsitePage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "addWebsite"

  override protected def navigateInNormalMode(answers: UserAnswers): Call =
    (answers.get(AddWebsitePage), answers.get(DeriveNumberOfWebsites)) match {
      case (Some(true), Some(size)) => routes.WebsiteController.onPageLoad(NormalMode, Index(size))
      case (Some(false), _)         => routes.BusinessContactDetailsController.onPageLoad(NormalMode)
      case _                        => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInCheckMode(answers: UserAnswers): Call =
    (answers.get(AddWebsitePage), answers.get(DeriveNumberOfWebsites)) match {
      case (Some(true), Some(size)) => routes.WebsiteController.onPageLoad(CheckMode, Index(size))
      case (Some(false), _)         => routes.CheckYourAnswersController.onPageLoad()
      case _                        => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInAmendMode(answers: UserAnswers): Call =
    (answers.get(AddWebsitePage), answers.get(DeriveNumberOfWebsites)) match {
    case (Some(true), Some(size)) => routes.WebsiteController.onPageLoad(AmendMode, Index(size))
    case (Some(false), _)         => amendRoutes.ChangeYourRegistrationController.onPageLoad()
    case _                        => amendRoutes.AmendJourneyRecoveryController.onPageLoad()
  }

  override protected def navigateInRejoinMode(answers: UserAnswers): Call =
    (answers.get(AddWebsitePage), answers.get(DeriveNumberOfWebsites)) match {
      case (Some(true), Some(size)) => routes.WebsiteController.onPageLoad(RejoinMode, Index(size))
      case (Some(false), _)         => rejoinRoutes.RejoinRegistrationController.onPageLoad()
      case _                        => rejoinRoutes.RejoinJourneyRecoveryController.onPageLoad()
    }
}
