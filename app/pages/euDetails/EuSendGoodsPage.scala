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

import scala.util.Try

case class EuSendGoodsPage(index: Index) extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ "euDetails" \ index.position \ toString

  override def toString: String = "euSendGoods"

  override protected def navigateInNormalMode(answers: UserAnswers): Call =
    answers.get(EuSendGoodsPage(index)) match {
      case Some(true) =>
        if (answers.get(EuVatNumberPage(index)).isEmpty) {
          euRoutes.EuTaxReferenceController.onPageLoad(NormalMode, index)
        } else {
          euRoutes.EuSendGoodsTradingNameController.onPageLoad(NormalMode, index)
        }
      case Some(false) => euRoutes.CheckEuDetailsAnswersController.onPageLoad(NormalMode, index)
      case _ => routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def navigateInCheckMode(answers: UserAnswers): Call =
    {
      val sendGoods = answers.get(EuSendGoodsPage(index))
      val euVatNumber = answers.get(EuVatNumberPage(index))
      val euTaxReference = answers.get(EuTaxReferencePage(index))
      val euSendGoodsTradingName = answers.get(EuSendGoodsTradingNamePage(index))

      (sendGoods, euVatNumber, euTaxReference, euSendGoodsTradingName) match {
        case (Some(true), Some(_), _, None) =>
          euRoutes.EuSendGoodsTradingNameController.onPageLoad(CheckMode, index)
        case (Some(true), Some(_), _, Some(_)) =>
          EuSendGoodsTradingNamePage(index).navigate(CheckMode, answers)
        case (Some(true), None, None, _) =>
          euRoutes.EuTaxReferenceController.onPageLoad(CheckMode, index)
        case (Some(true), None, Some(_), _) =>
          EuTaxReferencePage(index).navigate(CheckMode, answers)
        case (Some(false), _, _, _) =>
          euRoutes.CheckEuDetailsAnswersController.onPageLoad(CheckMode, index)
        case _ => routes.JourneyRecoveryController.onPageLoad()
      }
    }

  override protected def navigateInCheckLoopMode(answers: UserAnswers): Call =
  {
    val sendGoods = answers.get(EuSendGoodsPage(index))
    val euVatNumber = answers.get(EuVatNumberPage(index))
    val euTaxReference = answers.get(EuTaxReferencePage(index))
    val euSendGoodsTradingName = answers.get(EuSendGoodsTradingNamePage(index))

    (sendGoods, euVatNumber, euTaxReference, euSendGoodsTradingName) match {
      case (Some(true), Some(_), _, None) =>
        euRoutes.EuSendGoodsTradingNameController.onPageLoad(CheckLoopMode, index)
      case (Some(true), Some(_), _, Some(_)) =>
        EuSendGoodsTradingNamePage(index).navigate(CheckLoopMode, answers)
      case (Some(true), None, None, _) =>
        euRoutes.EuTaxReferenceController.onPageLoad(CheckLoopMode, index)
      case (Some(true), None, Some(_), _) =>
        EuTaxReferencePage(index).navigate(CheckLoopMode, answers)
      case (Some(false), _, _, _) =>
        euRoutes.CheckEuDetailsAnswersController.onPageLoad(NormalMode, index)
      case _ => routes.JourneyRecoveryController.onPageLoad()
    }
  }

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    if (value.contains(false)) {
      userAnswers
        .remove(EuSendGoodsTradingNamePage(index))
        .flatMap(_.remove(EuSendGoodsAddressPage(index)))
    } else {
      super.cleanup(value, userAnswers)
    }
}
