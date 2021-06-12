/*
 * Copyright 2021 HM Revenue & Customs
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
import pages.{CurrentlyRegisteredInCountryPage, CurrentlyRegisteredInEuPage, QuestionPage}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.{DeriveNumberOfEuRegistrations, DeriveNumberOfEuVatRegistrations}

case object AddEuDetailsPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "addEuVatDetails"

  override protected def navigateInNormalMode(answers: UserAnswers): Call = {

    def noRoute: Call = answers.get(DeriveNumberOfEuVatRegistrations) match {
      case Some(size) if size == 1 => routes.CurrentlyRegisteredInCountryController.onPageLoad(NormalMode)
      case Some(size) if size > 1  => routes.CurrentlyRegisteredInEuController.onPageLoad(NormalMode)
      case _                       => prevRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode)
    }

    (answers.get(AddEuDetailsPage), answers.get(DeriveNumberOfEuRegistrations)) match {
      case (Some(true), Some(size)) => euRoutes.EuCountryController.onPageLoad(NormalMode, Index(size))
      case (Some(false), _)         => noRoute
      case _                        => routes.JourneyRecoveryController.onPageLoad()
    }
  }

  override protected def navigateInCheckMode(answers: UserAnswers): Call = {

    def noRoute: Call = answers.get(DeriveNumberOfEuVatRegistrations) match {
      case Some(size) if size == 1 =>
        if (answers.get(CurrentlyRegisteredInCountryPage).isDefined) {
          routes.CheckYourAnswersController.onPageLoad()
        } else {
          routes.CurrentlyRegisteredInCountryController.onPageLoad(CheckMode)
        }

      case Some(size) if size > 1 =>
        if (answers.get(CurrentlyRegisteredInEuPage).isDefined) {
          routes.CheckYourAnswersController.onPageLoad()
        } else {
          routes.CurrentlyRegisteredInEuController.onPageLoad(CheckMode)
        }

      case Some(size) if size == 0 =>
        routes.CheckYourAnswersController.onPageLoad()

      case _ =>
        routes.JourneyRecoveryController.onPageLoad()
    }

    (answers.get(AddEuDetailsPage), answers.get(DeriveNumberOfEuVatRegistrations)) match {
      case (Some(true), Some(size)) => euRoutes.EuCountryController.onPageLoad(CheckMode, Index(size))
      case (Some(false), _)         => noRoute
      case _                        => routes.JourneyRecoveryController.onPageLoad()
    }
  }
}
