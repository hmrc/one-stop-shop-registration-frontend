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
import models.euDetails.EuConsumerSalesMethod
import models.{Index, Mode, NormalMode, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case class SellsGoodsToEUConsumerMethodPage(countryIndex: Index) extends QuestionPage[EuConsumerSalesMethod] {

  override def path: JsPath = JsPath \ "euDetails" \ countryIndex.position \ toString

  override def toString: String = "sellsGoodsToEUConsumerMethod"

  override def navigate(mode: Mode, answers: UserAnswers): Call = {
    (answers.vatInfo.exists(_.partOfVatGroup), answers.get(this)) match {
      case (true, Some(EuConsumerSalesMethod.FixedEstablishment)) =>
        euRoutes.CannotAddCountryController.onPageLoad(countryIndex)
      case (true, Some(EuConsumerSalesMethod.DispatchWarehouse)) =>
        euRoutes.RegistrationTypeController.onPageLoad(mode, countryIndex)
      case (false, Some(EuConsumerSalesMethod.FixedEstablishment)) =>
        euRoutes.RegistrationTypeController.onPageLoad(mode, countryIndex)
      case (false, Some(EuConsumerSalesMethod.DispatchWarehouse)) =>
        euRoutes.RegistrationTypeController.onPageLoad(mode, countryIndex)
      case _ => controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  }

  private def cleanupCommonData(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.remove(RegistrationTypePage(countryIndex))
      .flatMap(_.remove(EuVatNumberPage(countryIndex)))
      .flatMap(_.remove(EuTaxReferencePage(countryIndex)))
  }

  override def cleanup(value: Option[EuConsumerSalesMethod], userAnswers: UserAnswers): Try[UserAnswers] = {
    value match {
      case Some(EuConsumerSalesMethod.DispatchWarehouse) =>
        cleanupCommonData(userAnswers)
        .flatMap(_.remove(FixedEstablishmentTradingNamePage(countryIndex)))
        .flatMap(_.remove(FixedEstablishmentAddressPage(countryIndex)))
      case Some(EuConsumerSalesMethod.FixedEstablishment) =>
        cleanupCommonData(userAnswers)
          .flatMap(_.remove(EuSendGoodsTradingNamePage(countryIndex)))
          .flatMap(_.remove(EuSendGoodsAddressPage(countryIndex)))
      case None => super.cleanup(value, userAnswers)
    }
  }

}
