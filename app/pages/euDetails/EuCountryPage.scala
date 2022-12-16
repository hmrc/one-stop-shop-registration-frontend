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
import models.{CheckLoopMode, CheckMode, Country, Index, NormalMode, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class EuCountryPage(index: Index) extends QuestionPage[Country] {

  override def path: JsPath = JsPath \ "euDetails" \ index.position \ toString

  override def toString: String = "euCountry"

  override protected def navigateInNormalMode(answers: UserAnswers): Call = {
    val isPartOfVatGroup = answers.vatInfo.exists(_.partOfVatGroup)
    if (isPartOfVatGroup) {
      euRoutes.EuVatNumberController.onPageLoad(NormalMode, index)
    } else {
      euRoutes.VatRegisteredController.onPageLoad(NormalMode, index)
    }

  }

  override protected def navigateInCheckMode(answers: UserAnswers): Call = {
    val isPartOfVatGroup = answers.vatInfo.exists(_.partOfVatGroup)
    if (isPartOfVatGroup) {
      answers.get(EuVatNumberPage(index)) match {
        case Some(_) => EuVatNumberPage(index).navigate(CheckMode, answers)
        case None => euRoutes.EuVatNumberController.onPageLoad(CheckMode, index)
      }
    } else {
      answers.get(VatRegisteredPage(index)) match {
        case Some(_) => VatRegisteredPage(index).navigate(CheckMode, answers)
        case None => euRoutes.VatRegisteredController.onPageLoad(CheckMode, index)
      }
    }
  }

  override protected def navigateInCheckLoopMode(answers: UserAnswers): Call = {
    val isPartOfVatGroup = answers.vatInfo.exists(_.partOfVatGroup)
    if (isPartOfVatGroup) {
      answers.get(EuVatNumberPage(index)) match {
        case Some(_) => EuVatNumberPage(index).navigate(CheckLoopMode, answers)
        case None => euRoutes.EuVatNumberController.onPageLoad(CheckLoopMode, index)
      }
    } else {
      answers.get(VatRegisteredPage(index)) match {
        case Some(_) => VatRegisteredPage(index).navigate(CheckLoopMode, answers)
        case None => euRoutes.VatRegisteredController.onPageLoad(CheckLoopMode, index)
      }
    }
  }
}
