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
import controllers.amend.{routes => amendRoutes}
import controllers.rejoin.{routes => rejoinRoutes}
import controllers.routes
import models._
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case class SellsGoodsToEUConsumersPage(countryIndex: Index) extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ "euDetails" \ countryIndex.position \ toString

  override def toString: String = "sellsGoodsToEUConsumers"

  override protected def navigateInNormalMode(answers: UserAnswers): Call = {
    answers.get(SellsGoodsToEUConsumersPage(countryIndex)) match {
      case Some(true) =>
        euRoutes.SellsGoodsToEUConsumerMethodController.onPageLoad(NormalMode, countryIndex)
      case Some(false) =>
        euRoutes.SalesDeclarationNotRequiredController.onPageLoad(NormalMode, countryIndex)
      case _ => routes.JourneyRecoveryController.onPageLoad()
    }
  }

  override protected def navigateInCheckMode(answers: UserAnswers): Call = {
    answers.get(SellsGoodsToEUConsumersPage(countryIndex)) match {
      case Some(true) =>
        euRoutes.SellsGoodsToEUConsumerMethodController.onPageLoad(CheckMode, countryIndex)
      case Some(false) =>
        euRoutes.SalesDeclarationNotRequiredController.onPageLoad(CheckMode, countryIndex)
      case _ => routes.JourneyRecoveryController.onPageLoad()
    }
  }

  override protected def navigateInCheckLoopMode(answers: UserAnswers): Call = {
    answers.get(SellsGoodsToEUConsumersPage(countryIndex)) match {
      case Some(true) =>
        euRoutes.SellsGoodsToEUConsumerMethodController.onPageLoad(CheckLoopMode, countryIndex)
      case Some(false) =>
        euRoutes.SalesDeclarationNotRequiredController.onPageLoad(NormalMode, countryIndex)
      case _ => routes.JourneyRecoveryController.onPageLoad()
    }
  }

  override protected def navigateInAmendMode(answers: UserAnswers): Call = {
    answers.get(SellsGoodsToEUConsumersPage(countryIndex)) match {
      case Some(true) =>
        euRoutes.SellsGoodsToEUConsumerMethodController.onPageLoad(AmendMode, countryIndex)
      case Some(false) =>
        euRoutes.SalesDeclarationNotRequiredController.onPageLoad(AmendMode, countryIndex)
      case _ => amendRoutes.AmendJourneyRecoveryController.onPageLoad()
    }
  }

  override protected def navigateInAmendLoopMode(answers: UserAnswers): Call = {
    answers.get(SellsGoodsToEUConsumersPage(countryIndex)) match {
      case Some(true) =>
        euRoutes.SellsGoodsToEUConsumerMethodController.onPageLoad(AmendLoopMode, countryIndex)
      case Some(false) =>
        euRoutes.SalesDeclarationNotRequiredController.onPageLoad(AmendMode, countryIndex)
      case _ =>amendRoutes.AmendJourneyRecoveryController.onPageLoad()
    }
  }

  override protected def navigateInRejoinMode(answers: UserAnswers): Call = {
    answers.get(SellsGoodsToEUConsumersPage(countryIndex)) match {
      case Some(true) =>
        euRoutes.SellsGoodsToEUConsumerMethodController.onPageLoad(RejoinMode, countryIndex)
      case Some(false) =>
        euRoutes.SalesDeclarationNotRequiredController.onPageLoad(RejoinMode, countryIndex)
      case _ => rejoinRoutes.RejoinJourneyRecoveryController.onPageLoad()
    }
  }

  override protected def navigateInRejoinLoopMode(answers: UserAnswers): Call = {
    answers.get(SellsGoodsToEUConsumersPage(countryIndex)) match {
      case Some(true) =>
        euRoutes.SellsGoodsToEUConsumerMethodController.onPageLoad(RejoinLoopMode, countryIndex)
      case Some(false) =>
        euRoutes.SalesDeclarationNotRequiredController.onPageLoad(RejoinMode, countryIndex)
      case _ => rejoinRoutes.RejoinJourneyRecoveryController.onPageLoad()
    }
  }

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    value match {
      case Some(false) =>
        userAnswers.remove(RegistrationTypePage(countryIndex))
          .flatMap(_.remove(EuVatNumberPage(countryIndex)))
          .flatMap(_.remove(EuTaxReferencePage(countryIndex)))
          .flatMap(_.remove(SellsGoodsToEUConsumerMethodPage(countryIndex)))
          .flatMap(_.remove(FixedEstablishmentTradingNamePage(countryIndex)))
          .flatMap(_.remove(FixedEstablishmentAddressPage(countryIndex)))
          .flatMap(_.remove(EuSendGoodsTradingNamePage(countryIndex)))
          .flatMap(_.remove(EuSendGoodsAddressPage(countryIndex)))

      case _ => super.cleanup(value, userAnswers)
    }
  }

}

