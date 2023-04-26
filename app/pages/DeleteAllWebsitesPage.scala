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

import models.{CheckMode, NormalMode, UserAnswers}
import controllers.routes
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.{AllWebsites, DeriveNumberOfWebsites}

case object DeleteAllWebsitesPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ "websites"

  override def toString: String = "deleteAllWebsites"

  override protected def navigateInNormalMode(answers: UserAnswers): Call =
    answers.get(AllWebsites) match {
      case Some(websites) if websites.nonEmpty => routes.HasWebsiteController.onPageLoad(NormalMode)
      case _ => routes.CheckYourAnswersController.onPageLoad()
    }

  override protected def navigateInCheckMode(answers: UserAnswers): Call =
    (answers.get(DeleteAllWebsitesPage), answers.get(AllWebsites)) match {
      case (Some(true), _) => routes.HasWebsiteController.onPageLoad(CheckMode)
      case (Some(false), Some(websites)) if websites.nonEmpty => routes.CheckYourAnswersController.onPageLoad()
      case _ => routes.CheckYourAnswersController.onPageLoad()
    }

//  override protected def navigateInCheckMode(answers: UserAnswers): Call =
//    (answers.get(DeleteAllWebsitesPage), answers.get(AllWebsites)) match {
//      case (Some(true), Some(websites)) if websites.nonEmpty => routes.HasWebsiteController.onPageLoad(CheckMode)
//      case (Some(false), Some(websites)) if websites.nonEmpty => routes.CheckYourAnswersController.onPageLoad()
//      case _ => routes.CheckYourAnswersController.onPageLoad()
//    }
}
