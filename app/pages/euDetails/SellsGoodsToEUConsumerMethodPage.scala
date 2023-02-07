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

import models.{Index, NormalMode}
import controllers.euDetails.{routes => euRoutes}
import models.euDetails.EUConsumerSalesMethod
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class SellsGoodsToEUConsumerMethodPage(countryIndex: Index) extends QuestionPage[EUConsumerSalesMethod] {

  override def path: JsPath = JsPath \ "euDetails" \ countryIndex.position \ toString

  override def toString: String = "sellsGoodsToEUConsumerMethod"

  def navigate(answers: EUConsumerSalesMethod): Call = answers match {
      case EUConsumerSalesMethod.FixedEstablishment =>
        euRoutes.CannotAddCountryController.onPageLoad()
      case EUConsumerSalesMethod.DispatchWarehouse =>
        euRoutes.RegistrationTypeController.onPageLoad(NormalMode, countryIndex)
      case _ => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  //TODO cleanup if fixed establishment selected?
}
