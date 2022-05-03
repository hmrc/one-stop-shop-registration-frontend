/*
 * Copyright 2022 HM Revenue & Customs
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

import scala.concurrent.Future
import scala.util.Try

case class HasFixedEstablishmentPage(index: Index) extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ "euDetails" \ index.position \ toString

  override def toString: String = "hasFixedEstablishment"

  override protected def navigateInNormalMode(answers: UserAnswers): Call =
    answers.get(HasFixedEstablishmentPage(index)) match {
      case Some(true) =>
        if (answers.get(EuVatNumberPage(index)).isEmpty) {
          euRoutes.EuTaxReferenceController.onPageLoad(NormalMode, index)
        } else {
          euRoutes.FixedEstablishmentTradingNameController.onPageLoad(NormalMode, index)
        }
      case Some(false) => euRoutes.EuSendGoodsController.onPageLoad(NormalMode, index)
      case _ => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInCheckMode(answers: UserAnswers): Call = {
    val hasFixedEstablishment = answers.get(HasFixedEstablishmentPage(index))
    val euVatNumber = answers.get(EuVatNumberPage(index))
    val taxReference = answers.get(EuTaxReferencePage(index))
    val euSendGoods = answers.get(EuSendGoodsPage(index))

    (hasFixedEstablishment, euVatNumber, taxReference, euSendGoods) match {
      case (Some(true), Some(_), _, _) =>
        if (answers.get(FixedEstablishmentTradingNamePage(index)).isEmpty) {
          euRoutes.FixedEstablishmentTradingNameController.onPageLoad(CheckMode, index)
        } else {
          FixedEstablishmentTradingNamePage(index).navigate(CheckMode, answers)
        }
      case (Some(true), None, Some(_), _) =>
        EuTaxReferencePage(index).navigate(CheckMode, answers)
      case (Some(true), None, None, _) =>
        euRoutes.EuTaxReferenceController.onPageLoad(CheckMode, index)

      case (Some(false), _, _, Some(_)) =>
        EuSendGoodsPage(index).navigate(CheckMode, answers)
      case (Some(false), _, _, None) =>
        euRoutes.EuSendGoodsController.onPageLoad(CheckMode, index)
      case _ =>
        routes.JourneyRecoveryController.onPageLoad()

    }
  }

  override protected def navigateInCheckLoopMode(answers: UserAnswers): Call = {
    val hasFixedEstablishment = answers.get(HasFixedEstablishmentPage(index))
    val euVatNumber = answers.get(EuVatNumberPage(index))
    val taxReference = answers.get(EuTaxReferencePage(index))
    val euSendGoods = answers.get(EuSendGoodsPage(index))

    (hasFixedEstablishment, euVatNumber, taxReference, euSendGoods) match {
      case (Some(true), Some(_), _, _) =>
        if (answers.get(FixedEstablishmentTradingNamePage(index)).isEmpty) {
          euRoutes.FixedEstablishmentTradingNameController.onPageLoad(CheckLoopMode, index)
        } else {
          FixedEstablishmentTradingNamePage(index).navigate(CheckLoopMode, answers)
        }
      case (Some(true), None, Some(_), _) =>
        EuTaxReferencePage(index).navigate(CheckLoopMode, answers)
      case (Some(true), None, None, _) =>
        euRoutes.EuTaxReferenceController.onPageLoad(CheckLoopMode, index)

      case (Some(false), _, _, Some(_)) =>
        EuSendGoodsPage(index).navigate(CheckLoopMode, answers)
      case (Some(false), _, _, None) =>
        euRoutes.EuSendGoodsController.onPageLoad(CheckLoopMode, index)
      case _ =>
        routes.JourneyRecoveryController.onPageLoad()

    }
  }

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    if (value.contains(false)) {
      userAnswers
        .remove(FixedEstablishmentTradingNamePage(index))
        .flatMap(_.remove(FixedEstablishmentAddressPage(index)))
    } else {
      userAnswers
        .remove(EuSendGoodsPage(index))
        .flatMap(_.remove(EuSendGoodsTradingNamePage(index)))
        .flatMap(_.remove(EuSendGoodsAddressPage(index)))
    }
}
