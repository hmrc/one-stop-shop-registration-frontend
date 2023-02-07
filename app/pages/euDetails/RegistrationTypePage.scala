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

import models.euDetails.RegistrationType
import controllers.euDetails.routes
import models.{Index, NormalMode}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class RegistrationTypePage(countryIndex: Index) extends QuestionPage[RegistrationType] {

  override def path: JsPath = JsPath \ "euDetails" \ countryIndex.position \ toString

  override def toString: String = "registrationType"

  def navigate(answer: RegistrationType): Call = answer match {
    case RegistrationType.VatNumber =>
      routes.EuVatNumberController.onPageLoad(NormalMode, countryIndex)
    case _ =>
      routes.EuTaxReferenceController.onPageLoad(NormalMode, countryIndex)

  }

}
