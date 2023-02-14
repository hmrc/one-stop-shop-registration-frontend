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
import controllers.previousRegistrations.{routes => prevRegRoutes}
import controllers.routes
import models.{CheckMode, Index, NormalMode, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.DeriveNumberOfEuRegistrations

case object AddEuDetailsPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "addEuVatDetails"

  override protected def navigateInNormalMode(answers: UserAnswers): Call =
    (answers.get(AddEuDetailsPage), answers.get(DeriveNumberOfEuRegistrations)) match {
      case (Some(true), Some(size)) => euRoutes.EuCountryController.onPageLoad(NormalMode, Index(size))
      case (Some(false), _)         => prevRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode)
      case _                        => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInCheckMode(answers: UserAnswers): Call =
    (answers.get(AddEuDetailsPage), answers.get(DeriveNumberOfEuRegistrations)) match {
      case (Some(true), Some(size)) => euRoutes.EuCountryController.onPageLoad(CheckMode, Index(size))
      case (Some(false), _)         => routes.CheckYourAnswersController.onPageLoad()
      case _                        => routes.JourneyRecoveryController.onPageLoad()
    }
}
