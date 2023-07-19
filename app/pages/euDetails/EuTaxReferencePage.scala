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
import models.euDetails.EuConsumerSalesMethod
import models.{AmendLoopMode, AmendMode, CheckLoopMode, CheckMode, Index, NormalMode, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class EuTaxReferencePage(countryIndex: Index) extends QuestionPage[String] {

  override def path: JsPath = JsPath \ "euDetails" \ countryIndex.position \ toString

  override def toString: String = "euTaxReference"

  override protected def navigateInNormalMode(answers: UserAnswers): Call =
    (answers.vatInfo.exists(_.partOfVatGroup), answers.get(SellsGoodsToEUConsumerMethodPage(countryIndex))) match {
      case (true, Some(EuConsumerSalesMethod.DispatchWarehouse)) =>
        euRoutes.EuSendGoodsTradingNameController.onPageLoad(NormalMode, countryIndex)
      case (false, Some(EuConsumerSalesMethod.FixedEstablishment)) =>
        euRoutes.FixedEstablishmentTradingNameController.onPageLoad(NormalMode, countryIndex)
      case (false, Some(EuConsumerSalesMethod.DispatchWarehouse)) =>
        euRoutes.EuSendGoodsTradingNameController.onPageLoad(NormalMode, countryIndex)
      case _ => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInCheckMode(answers: UserAnswers): Call =
    (answers.vatInfo.exists(_.partOfVatGroup), answers.get(SellsGoodsToEUConsumerMethodPage(countryIndex))) match {
      case (true, Some(EuConsumerSalesMethod.DispatchWarehouse)) =>
        answers.get(EuSendGoodsTradingNamePage(countryIndex)) match {
          case Some(_) => EuSendGoodsTradingNamePage(countryIndex).navigate(CheckMode, answers)
          case None => euRoutes.EuSendGoodsTradingNameController.onPageLoad(CheckMode, countryIndex)
        }
      case (false, Some(EuConsumerSalesMethod.FixedEstablishment)) =>
        answers.get(FixedEstablishmentTradingNamePage(countryIndex)) match {
          case Some(_) => FixedEstablishmentTradingNamePage(countryIndex).navigate(CheckMode, answers)
          case None => euRoutes.FixedEstablishmentTradingNameController.onPageLoad(CheckMode, countryIndex)
        }
      case (false, Some(EuConsumerSalesMethod.DispatchWarehouse)) =>
        answers.get(EuSendGoodsTradingNamePage(countryIndex)) match {
          case Some(_) => EuSendGoodsTradingNamePage(countryIndex).navigate(CheckMode, answers)
          case None => euRoutes.EuSendGoodsTradingNameController.onPageLoad(CheckMode, countryIndex)
        }
      case _ => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInCheckLoopMode(answers: UserAnswers): Call =
    (answers.vatInfo.exists(_.partOfVatGroup), answers.get(SellsGoodsToEUConsumerMethodPage(countryIndex))) match {
      case (true, Some(EuConsumerSalesMethod.DispatchWarehouse)) =>
        answers.get(EuSendGoodsTradingNamePage(countryIndex)) match {
          case Some(_) => EuSendGoodsTradingNamePage(countryIndex).navigate(CheckLoopMode, answers)
          case None => euRoutes.EuSendGoodsTradingNameController.onPageLoad(CheckLoopMode, countryIndex)
        }
      case (false, Some(EuConsumerSalesMethod.FixedEstablishment)) =>
        answers.get(FixedEstablishmentTradingNamePage(countryIndex)) match {
          case Some(_) => FixedEstablishmentTradingNamePage(countryIndex).navigate(CheckLoopMode, answers)
          case None => euRoutes.FixedEstablishmentTradingNameController.onPageLoad(CheckLoopMode, countryIndex)
        }
      case (false, Some(EuConsumerSalesMethod.DispatchWarehouse)) =>
        answers.get(EuSendGoodsTradingNamePage(countryIndex)) match {
          case Some(_) =>EuSendGoodsTradingNamePage(countryIndex).navigate(CheckLoopMode, answers)
          case None => euRoutes.EuSendGoodsTradingNameController.onPageLoad(CheckLoopMode, countryIndex)
        }
      case _ => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInAmendMode(answers: UserAnswers): Call =
    (answers.vatInfo.exists(_.partOfVatGroup), answers.get(SellsGoodsToEUConsumerMethodPage(countryIndex))) match {
      case (true, Some(EuConsumerSalesMethod.DispatchWarehouse)) =>
        euRoutes.EuSendGoodsTradingNameController.onPageLoad(AmendMode, countryIndex)
      case (false, Some(EuConsumerSalesMethod.FixedEstablishment)) =>
        euRoutes.FixedEstablishmentTradingNameController.onPageLoad(AmendMode, countryIndex)
      case (false, Some(EuConsumerSalesMethod.DispatchWarehouse)) =>
        euRoutes.EuSendGoodsTradingNameController.onPageLoad(AmendMode, countryIndex)
      case _ => routes.AmendJourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInAmendLoopMode(answers: UserAnswers): Call =
    (answers.vatInfo.exists(_.partOfVatGroup), answers.get(SellsGoodsToEUConsumerMethodPage(countryIndex))) match {
      case (true, Some(EuConsumerSalesMethod.DispatchWarehouse)) =>
        answers.get(EuSendGoodsTradingNamePage(countryIndex)) match {
          case Some(_) => EuSendGoodsTradingNamePage(countryIndex).navigate(AmendLoopMode, answers)
          case None => euRoutes.EuSendGoodsTradingNameController.onPageLoad(AmendLoopMode, countryIndex)
        }
      case (false, Some(EuConsumerSalesMethod.FixedEstablishment)) =>
        answers.get(FixedEstablishmentTradingNamePage(countryIndex)) match {
          case Some(_) => FixedEstablishmentTradingNamePage(countryIndex).navigate(AmendLoopMode, answers)
          case None => euRoutes.FixedEstablishmentTradingNameController.onPageLoad(AmendLoopMode, countryIndex)
        }
      case (false, Some(EuConsumerSalesMethod.DispatchWarehouse)) =>
        answers.get(EuSendGoodsTradingNamePage(countryIndex)) match {
          case Some(_) => EuSendGoodsTradingNamePage(countryIndex).navigate(AmendLoopMode, answers)
          case None => euRoutes.EuSendGoodsTradingNameController.onPageLoad(AmendLoopMode, countryIndex)
        }
      case _ => routes.AmendJourneyRecoveryController.onPageLoad()
    }

}
