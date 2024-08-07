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

package pages.euDetails

import controllers.euDetails.{routes => euRoutes}
import models.{AmendMode, CheckMode, NormalMode, RejoinMode, UserAnswers}
import pages.Page
import play.api.mvc.Call

case object CheckEuDetailsAnswersPage extends Page {

  override protected def navigateInNormalMode(answers: UserAnswers): Call =
    euRoutes.AddEuDetailsController.onPageLoad(NormalMode)

  override protected def navigateInCheckMode(answers: UserAnswers): Call =
    euRoutes.AddEuDetailsController.onPageLoad(CheckMode)

  override protected def navigateInCheckLoopMode(answers: UserAnswers): Call =
    euRoutes.AddEuDetailsController.onPageLoad(NormalMode)

  override protected def navigateInAmendMode(answers: UserAnswers): Call =
    euRoutes.AddEuDetailsController.onPageLoad(AmendMode)

  override protected def navigateInAmendLoopMode(answers: UserAnswers): Call =
    euRoutes.AddEuDetailsController.onPageLoad(AmendMode)

  override protected def navigateInRejoinMode(answers: UserAnswers): Call =
    euRoutes.AddEuDetailsController.onPageLoad(RejoinMode)

  override protected def navigateInRejoinLoopMode(answers: UserAnswers): Call =
    euRoutes.AddEuDetailsController.onPageLoad(RejoinMode)
}
