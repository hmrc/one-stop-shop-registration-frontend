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

import controllers.amend.{routes => amendRoutes}
import controllers.euDetails.{routes => euRoutes}
import controllers.routes
import models.{AmendMode, CheckMode, Index, NormalMode, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object TaxRegisteredInEuPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "taxRegisteredInEu"

  override protected def navigateInNormalMode(answers: UserAnswers): Call =
    answers.get(TaxRegisteredInEuPage) match {
      case Some(true)  => euRoutes.EuCountryController.onPageLoad(NormalMode, Index(0))
      case Some(false) => routes.IsOnlineMarketplaceController.onPageLoad(NormalMode)
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInCheckMode(answers: UserAnswers): Call = {
    answers.get(TaxRegisteredInEuPage) match {
      case Some(true) =>
        if(answers.get(EuCountryPage(Index(0))).isDefined) {
          routes.CheckYourAnswersController.onPageLoad()
        } else {
          euRoutes.EuCountryController.onPageLoad(CheckMode, Index(0))
        }

      case Some(false) =>
        if(answers.get(EuCountryPage(Index(0))).isDefined) {
          euRoutes.DeleteAllEuDetailsController.onPageLoad(CheckMode)
        } else {
          routes.CheckYourAnswersController.onPageLoad()
        }

      case None =>
        routes.JourneyRecoveryController.onPageLoad()
    }
  }

  override protected def navigateInAmendMode(answers: UserAnswers): Call = {
    answers.get(TaxRegisteredInEuPage) match {
      case Some(true) =>
        if (answers.get(EuCountryPage(Index(0))).isDefined) {
          amendRoutes.ChangeYourRegistrationController.onPageLoad()
        } else {
          euRoutes.EuCountryController.onPageLoad(AmendMode, Index(0))
        }

      case Some(false) =>
        if (answers.get(EuCountryPage(Index(0))).isDefined) {
          euRoutes.DeleteAllEuDetailsController.onPageLoad(AmendMode)
        } else {
          amendRoutes.ChangeYourRegistrationController.onPageLoad()
        }

      case None =>
        amendRoutes.AmendJourneyRecoveryController.onPageLoad()
    }
  }

}
