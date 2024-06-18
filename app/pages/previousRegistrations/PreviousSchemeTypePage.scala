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

package pages.previousRegistrations

import controllers.previousRegistrations.{routes => prevRegRoutes}
import models.{AmendMode, CheckMode, Index, NormalMode, PreviousSchemeType, RejoinMode, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case class PreviousSchemeTypePage(countryIndex: Index, schemeIndex: Index) extends QuestionPage[PreviousSchemeType] {

  override def path: JsPath = JsPath \ "previousRegistrations" \ countryIndex.position \ "previousSchemesDetails" \ schemeIndex.position \ toString

  override def toString: String = "previousSchemeType"

  override protected def navigateInNormalMode(answers: UserAnswers): Call = {
    if(answers.get(this).contains(PreviousSchemeType.OSS)) {
      prevRegRoutes.PreviousOssNumberController.onPageLoad(NormalMode, countryIndex, schemeIndex)
    } else {
      prevRegRoutes.PreviousIossSchemeController.onPageLoad(NormalMode, countryIndex, schemeIndex)
    }
  }
  override protected def navigateInCheckMode(answers: UserAnswers): Call = {
    if(answers.get(this).contains(PreviousSchemeType.OSS)) {
      prevRegRoutes.PreviousOssNumberController.onPageLoad(CheckMode, countryIndex, schemeIndex)
    } else {
      prevRegRoutes.PreviousIossSchemeController.onPageLoad(CheckMode, countryIndex, schemeIndex)
    }
  }

  override protected def navigateInAmendMode(answers: UserAnswers): Call = {
    if (answers.get(this).contains(PreviousSchemeType.OSS)) {
      prevRegRoutes.PreviousOssNumberController.onPageLoad(AmendMode, countryIndex, schemeIndex)
    } else {
      prevRegRoutes.PreviousIossSchemeController.onPageLoad(AmendMode, countryIndex, schemeIndex)
    }
  }

  override protected def navigateInRejoinMode(answers: UserAnswers): Call = {
    if (answers.get(this).contains(PreviousSchemeType.OSS)) {
      prevRegRoutes.PreviousOssNumberController.onPageLoad(RejoinMode, countryIndex, schemeIndex)
    } else {
      prevRegRoutes.PreviousIossSchemeController.onPageLoad(RejoinMode, countryIndex, schemeIndex)
    }
  }

  override def cleanup(value: Option[PreviousSchemeType], userAnswers: UserAnswers): Try[UserAnswers] = {

    for {
      updatedAnswers <- userAnswers.remove(PreviousIossNumberPage(countryIndex, schemeIndex))
      updateAnswers2 <- updatedAnswers.remove(PreviousIossSchemePage(countryIndex, schemeIndex))
      updateAnswers3 <- updateAnswers2.remove(PreviousSchemePage(countryIndex, schemeIndex))

    } yield updateAnswers3
  }


}
