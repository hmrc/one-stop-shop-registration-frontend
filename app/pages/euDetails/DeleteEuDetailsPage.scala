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
import models.{AmendMode, CheckMode, Index, NormalMode, UserAnswers}
import pages.Page
import play.api.mvc.Call
import queries.DeriveNumberOfEuRegistrations

case class DeleteEuDetailsPage(index: Index) extends Page {

  override protected def navigateInNormalMode(answers: UserAnswers): Call =
    answers.get(DeriveNumberOfEuRegistrations) match {
      case Some(n) if n > 0 => euRoutes.AddEuDetailsController.onPageLoad(NormalMode)
      case _                => euRoutes.TaxRegisteredInEuController.onPageLoad(NormalMode)
    }

  override protected def navigateInCheckMode(answers: UserAnswers): Call =
    answers.get(DeriveNumberOfEuRegistrations) match {
      case Some(n) if n > 0 => euRoutes.AddEuDetailsController.onPageLoad(CheckMode)
      case _                => euRoutes.TaxRegisteredInEuController.onPageLoad(CheckMode)
    }

  override protected def navigateInAmendMode(answers: UserAnswers): Call =
    answers.get(DeriveNumberOfEuRegistrations) match {
      case Some(n) if n > 0 => euRoutes.AddEuDetailsController.onPageLoad(AmendMode)
      case _ => euRoutes.TaxRegisteredInEuController.onPageLoad(AmendMode)
    }
}
