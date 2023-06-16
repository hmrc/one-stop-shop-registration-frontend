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
import controllers.routes
import models.{AmendLoopMode, AmendMode, CheckLoopMode, CheckMode, Index, NormalMode, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case class VatRegisteredPage(index: Index) extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ "euDetails" \ index.position \ toString

  override def toString: String = "vatRegistered"

  override protected def navigateInNormalMode(answers: UserAnswers): Call =
    answers.get(VatRegisteredPage(index)) match {
      case Some(true)  => euRoutes.EuVatNumberController.onPageLoad(NormalMode, index)
      case Some(false) => euRoutes.CannotAddCountryWithoutVatNumberController.onPageLoad(NormalMode, index)
      case None        => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInCheckMode(answers: UserAnswers): Call =
    answers.get(VatRegisteredPage(index)) match {
      case Some(true) =>
        if (answers.get(EuVatNumberPage(index)).isDefined) {
          EuVatNumberPage(index).navigate(CheckMode, answers)
        } else {
          euRoutes.EuVatNumberController.onPageLoad(CheckMode, index)
        }
      case Some(false) =>
          euRoutes.CannotAddCountryWithoutVatNumberController.onPageLoad(CheckMode, index)
      case None =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInAmendMode(answers: UserAnswers): Call =
    answers.get(VatRegisteredPage(index)) match {
      case Some(true) =>
        if (answers.get(EuVatNumberPage(index)).isDefined) {
          EuVatNumberPage(index).navigate(AmendMode, answers)
        } else {
          euRoutes.EuVatNumberController.onPageLoad(AmendMode, index)
        }
      case Some(false) =>
        euRoutes.CannotAddCountryWithoutVatNumberController.onPageLoad(AmendMode, index)
      case None =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInCheckLoopMode(answers: UserAnswers): Call =
    answers.get(VatRegisteredPage(index)) match {
      case Some(true) =>
        if (answers.get(EuVatNumberPage(index)).isDefined) {
          EuVatNumberPage(index).navigate(CheckLoopMode, answers)
        } else {
          euRoutes.EuVatNumberController.onPageLoad(CheckLoopMode, index)
        }
      case Some(false) =>
          euRoutes.CannotAddCountryWithoutVatNumberController.onPageLoad(CheckLoopMode, index)
      case None =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInAmendLoopMode(answers: UserAnswers): Call =
    answers.get(VatRegisteredPage(index)) match {
      case Some(true) =>
        if (answers.get(EuVatNumberPage(index)).isDefined) {
          EuVatNumberPage(index).navigate(AmendLoopMode, answers)
        } else {
          euRoutes.EuVatNumberController.onPageLoad(AmendLoopMode, index)
        }
      case Some(false) =>
        euRoutes.CannotAddCountryWithoutVatNumberController.onPageLoad(AmendLoopMode, index)
      case None =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    if (value contains false) {
      userAnswers.remove(EuVatNumberPage(index))
    } else {
      userAnswers.remove(EuTaxReferencePage(index))
    }
  }
}
