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
import models.{AmendMode, CheckMode, Index, NormalMode, RejoinMode, UserAnswers}
import play.api.mvc.Call
import queries.DeriveNumberOfWebsites

case class DeleteWebsitePage(index: Index) extends Page {

  override protected def navigateInNormalMode(answers: UserAnswers): Call =
    answers.get(DeriveNumberOfWebsites) match {
      case Some(n) if n > 0 => routes.AddWebsiteController.onPageLoad(NormalMode)
      case _                => routes.HasWebsiteController.onPageLoad(NormalMode)
    }

  override protected def navigateInCheckMode (answers: UserAnswers): Call =
    answers.get(DeriveNumberOfWebsites) match {
      case Some(n) if n > 0 => routes.AddWebsiteController.onPageLoad(CheckMode)
      case _                => routes.HasWebsiteController.onPageLoad(CheckMode)
    }

  override protected def navigateInAmendMode(answers: UserAnswers): Call =
    answers.get(DeriveNumberOfWebsites) match {
      case Some(n) if n > 0 => routes.AddWebsiteController.onPageLoad(AmendMode)
      case _ => routes.HasWebsiteController.onPageLoad(AmendMode)
    }

  override protected def navigateInRejoinMode(answers: UserAnswers): Call =
    answers.get(DeriveNumberOfWebsites) match {
      case Some(n) if n > 0 => routes.AddWebsiteController.onPageLoad(RejoinMode)
      case _ => routes.HasWebsiteController.onPageLoad(RejoinMode)
    }
}
