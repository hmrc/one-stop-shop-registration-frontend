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
import controllers.amend.{routes => amendRoutes}
import controllers.rejoin.{routes => rejoinRoutes}
import models.{AmendMode, CheckMode, Index, NormalMode, RejoinMode, UserAnswers}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.AllWebsites

case object HasWebsitePage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "hasWebsite"

  override protected def navigateInNormalMode(answers: UserAnswers): Call = answers.get(HasWebsitePage) match {
    case Some(true)  => routes.WebsiteController.onPageLoad(NormalMode, Index(0))
    case Some(false) => routes.BusinessContactDetailsController.onPageLoad(NormalMode)
    case None        => routes.JourneyRecoveryController.onPageLoad()
  }

  override protected def navigateInCheckMode(answers: UserAnswers): Call =
    (answers.get(HasWebsitePage), answers.get(AllWebsites)) match {
      case (Some(true), Some(websites)) if websites.nonEmpty  => routes.AddWebsiteController.onPageLoad(CheckMode)
      case (Some(true), _)                                    => routes.WebsiteController.onPageLoad(CheckMode, Index(0))
      case (Some(false), Some(websites)) if websites.nonEmpty => routes.DeleteAllWebsitesController.onPageLoad(CheckMode)
      case (Some(false), _)                                   => routes.CheckYourAnswersController.onPageLoad()
      case _                                                  => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInAmendMode(answers: UserAnswers): Call =
    (answers.get(HasWebsitePage), answers.get(AllWebsites)) match {
      case (Some(true), Some(websites)) if websites.nonEmpty  => routes.AddWebsiteController.onPageLoad(AmendMode)
      case (Some(true), _)                                    => routes.WebsiteController.onPageLoad(AmendMode, Index(0))
      case (Some(false), Some(websites)) if websites.nonEmpty => routes.DeleteAllWebsitesController.onPageLoad(AmendMode)
      case (Some(false), _)                                   => amendRoutes.ChangeYourRegistrationController.onPageLoad()
      case _                                                  => amendRoutes.AmendJourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInRejoinMode(answers: UserAnswers): Call =
    (answers.get(HasWebsitePage), answers.get(AllWebsites)) match {
      case (Some(true), Some(websites)) if websites.nonEmpty  => routes.AddWebsiteController.onPageLoad(RejoinMode)
      case (Some(true), _)                                    => routes.WebsiteController.onPageLoad(RejoinMode, Index(0))
      case (Some(false), Some(websites)) if websites.nonEmpty => routes.DeleteAllWebsitesController.onPageLoad(RejoinMode)
      case (Some(false), _)                                   => rejoinRoutes.RejoinRegistrationController.onPageLoad()
      case _                                                  => rejoinRoutes.RejoinJourneyRecoveryController.onPageLoad()
    }
}
