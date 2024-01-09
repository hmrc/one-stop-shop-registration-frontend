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
import models.{AmendMode, CheckMode, Index, NormalMode, UserAnswers}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.AllWebsites

import scala.util.Try

case class WebsitePage(index: Index) extends QuestionPage[String] {

  override def path: JsPath = JsPath \ "websites" \ index.position

  override val toString: String = "website"

  override protected def navigateInNormalMode(answers: UserAnswers): Call =
    routes.AddWebsiteController.onPageLoad(NormalMode)

  override protected def navigateInCheckMode(answers: UserAnswers): Call =
    routes.AddWebsiteController.onPageLoad(CheckMode)

  override protected def navigateInAmendMode(answers: UserAnswers): Call =
    routes.AddWebsiteController.onPageLoad(AmendMode)

  override def cleanup(value: Option[String], userAnswers: UserAnswers): Try[UserAnswers] = {
    if (userAnswers.get(AllWebsites).exists(_.isEmpty)) {
      userAnswers.remove(AllWebsites)
    } else {
      super.cleanup(value, userAnswers)
    }
  }
}
