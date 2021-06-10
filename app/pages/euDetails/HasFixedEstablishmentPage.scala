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
import controllers.routes
import models.{CheckLoopMode, CheckMode, Index, NormalMode, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case class HasFixedEstablishmentPage(index: Index) extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ "euVatDetails" \ index.position \ toString

  override def toString: String = "hasFixedEstablishment"

  override protected def navigateInNormalMode(answers: UserAnswers): Call =
    (answers.get(HasFixedEstablishmentPage(index)), answers.get(VatRegisteredPage(index))) match {
      case (Some(true), Some(true))  => euRoutes.FixedEstablishmentTradingNameController.onPageLoad(NormalMode, index)
      case (Some(true), Some(false)) => euRoutes.EuTaxReferenceController.onPageLoad(NormalMode, index)
      case (Some(false), _)          => euRoutes.CheckEuDetailsAnswersController.onPageLoad(index)
      case _                         => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInCheckMode(answers: UserAnswers): Call =
    (answers.get(HasFixedEstablishmentPage(index)), answers.get(VatRegisteredPage(index))) match {
      case (Some(true), Some(vatRegistered)) =>
        if (vatRegistered) {
          if (answers.get(FixedEstablishmentTradingNamePage(index)).isDefined) {
            FixedEstablishmentTradingNamePage(index).navigate(CheckMode, answers)
          } else {
            euRoutes.FixedEstablishmentTradingNameController.onPageLoad(CheckMode, index)
          }
        } else {
          if (answers.get(EuTaxReferencePage(index)).isDefined) {
            EuTaxReferencePage(index).navigate(CheckMode, answers)
          } else {
            euRoutes.EuTaxReferenceController.onPageLoad(CheckMode, index)
          }
        }

      case (Some(false), _) =>
        euRoutes.CheckEuDetailsAnswersController.onPageLoad(index)

      case _ =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInCheckLoopMode(answers: UserAnswers): Call =
    (answers.get(HasFixedEstablishmentPage(index)), answers.get(VatRegisteredPage(index))) match {
      case (Some(true), Some(vatRegistered)) =>
        if (vatRegistered) {
          if (answers.get(FixedEstablishmentTradingNamePage(index)).isDefined) {
            FixedEstablishmentTradingNamePage(index).navigate(CheckLoopMode, answers)
          } else {
            euRoutes.FixedEstablishmentTradingNameController.onPageLoad(CheckLoopMode, index)
          }
        } else {
          if (answers.get(EuTaxReferencePage(index)).isDefined) {
            EuTaxReferencePage(index).navigate(CheckLoopMode, answers)
          } else {
            euRoutes.EuTaxReferenceController.onPageLoad(CheckLoopMode, index)
          }
        }

      case (Some(false), _) =>
        euRoutes.CheckEuDetailsAnswersController.onPageLoad(index)

      case _ =>
        routes.JourneyRecoveryController.onPageLoad()
    }

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    if (value.contains(false)) {
      userAnswers
        .remove(FixedEstablishmentTradingNamePage(index))
        .flatMap(_.remove(FixedEstablishmentAddressPage(index)))
    } else {
      super.cleanup(value, userAnswers)
    }
}
