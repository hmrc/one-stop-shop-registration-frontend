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

package viewmodels.checkAnswers.euDetails

import controllers.euDetails.routes
import models.{CheckLoopMode, CheckMode, Index, Mode, NormalMode, UserAnswers}
import pages.euDetails
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object FixedEstablishmentAddressSummary {

  def row(answers: UserAnswers, index: Index, currentMode: Mode)(implicit messages: Messages): Option[SummaryListRow] = {

    val changeLinkMode = currentMode match {
      case NormalMode    => CheckLoopMode
      case CheckMode     => CheckMode
      case CheckLoopMode => CheckLoopMode
    }

    answers.get(euDetails.FixedEstablishmentAddressPage(index)).map {
      answer =>

        val value = Seq(
          Some(HtmlFormat.escape(answer.line1).toString),
          answer.line2.map(HtmlFormat.escape),
          Some(HtmlFormat.escape(answer.townOrCity).toString),
          answer.county.map(HtmlFormat.escape),
          answer.postCode.map(HtmlFormat.escape)
        ).flatten.mkString("<br/>")

        SummaryListRowViewModel(
          key = "fixedEstablishmentAddress.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", routes.FixedEstablishmentAddressController.onPageLoad(changeLinkMode, index).url)
              .withVisuallyHiddenText(messages("fixedEstablishmentAddress.change.hidden"))
          )
        )
    }
  }
}