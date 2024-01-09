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

package viewmodels.checkAnswers.euDetails

import controllers.euDetails.routes
import models.{AmendLoopMode, AmendMode, CheckLoopMode, CheckMode, Index, Mode, NormalMode, UserAnswers}
import pages.euDetails.EuSendGoodsTradingNamePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object EuSendGoodsTradingNameSummary {

  def row(answers: UserAnswers, index: Index, currentMode: Mode)(implicit messages: Messages): Option[SummaryListRow] = {

    val changeLinkMode = currentMode match {
      case NormalMode    => CheckLoopMode
      case CheckMode     => CheckMode
      case AmendMode     => AmendLoopMode
      case CheckLoopMode => CheckLoopMode
      case AmendLoopMode => AmendLoopMode
    }

    answers.get(EuSendGoodsTradingNamePage(index)).map {
      answer =>

        SummaryListRowViewModel(
          key = "euSendGoodsTradingName.checkYourAnswersLabel",
          value = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.EuSendGoodsTradingNameController.onPageLoad(changeLinkMode, index).url)
              .withVisuallyHiddenText(messages("euSendGoodsTradingName.change.hidden"))
          )
        )
    }
  }
}
