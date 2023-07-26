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

package pages.previousRegistrations

import controllers.previousRegistrations.{routes => prevRegRoutes}
import controllers.amend.{routes => amendRoutes}
import controllers.routes
import models.{AmendMode, CheckMode, Index, NormalMode, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.previousRegistration.DeriveNumberOfPreviousRegistrations

case object PreviouslyRegisteredPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "previouslyRegistered"

  override protected def navigateInNormalMode(answers: UserAnswers): Call =
    answers.get(PreviouslyRegisteredPage) match {
      case Some(true)  => prevRegRoutes.PreviousEuCountryController.onPageLoad(NormalMode, Index(0))
      case Some(false) => routes.CommencementDateController.onPageLoad(NormalMode)
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInCheckMode(answers: UserAnswers): Call =
    (answers.get(PreviouslyRegisteredPage), answers.get(DeriveNumberOfPreviousRegistrations)) match {
      case (Some(true), Some(size)) if size > 0   => routes.CheckYourAnswersController.onPageLoad()
      case (Some(true), _)                        => prevRegRoutes.PreviousEuCountryController.onPageLoad(CheckMode, Index(0))
      case (Some(false), Some(size)) if size > 0  => routes.DeleteAllPreviousRegistrationsController.onPageLoad()
      case (Some(false), _)                       => routes.CheckYourAnswersController.onPageLoad()
      case _                                      => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInAmendMode(answers: UserAnswers): Call =
    (answers.get(PreviouslyRegisteredPage), answers.get(DeriveNumberOfPreviousRegistrations)) match {
      case (Some(true), Some(size)) if size > 0 => amendRoutes.ChangeYourRegistrationController.onPageLoad()
      case (Some(true), _) => prevRegRoutes.PreviousEuCountryController.onPageLoad(AmendMode, Index(0))
      case (Some(false), _) => amendRoutes.ChangeYourRegistrationController.onPageLoad()
      case _ => amendRoutes.AmendJourneyRecoveryController.onPageLoad()
    }

}
