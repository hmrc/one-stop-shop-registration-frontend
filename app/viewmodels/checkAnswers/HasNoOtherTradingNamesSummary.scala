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

package viewmodels.checkAnswers

import controllers.routes
import models.*
import pages.HasNoOtherTradingNamesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, SummaryListRow}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

import javax.inject.Inject

class HasNoOtherTradingNamesSummary @Inject() {

  def row(answers: UserAnswers, mode: Mode, isExcluded: Boolean = false)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(HasNoOtherTradingNamesPage).map {
      hasNoOtherTradingNames =>
        val value = if (hasNoOtherTradingNames) "site.yes" else "site.no"

        val actions: Seq[ActionItem] = if (isExcluded & mode.isInAmend) {
          Seq.empty
        } else {
          Seq(
            ActionItemViewModel("site.change", routes.HasNoOtherTradingNamesController.onPageLoad(mode).url)
              .withVisuallyHiddenText(messages("hasNoOtherTradingNames.change.hidden"))
          )
        }

        SummaryListRowViewModel(
          key     = messages("hasNoOtherTradingNames.checkYourAnswersLabel"),
          value   = ValueViewModel(value),
          actions = actions
        )
    }

  def amendedAnswersRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(HasNoOtherTradingNamesPage).map {
      hasNoOtherTradingNames =>
        val value = if (hasNoOtherTradingNames) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key     = KeyViewModel("hasNoOtherTradingNames.checkYourAnswersLabel").withCssClass("govuk-!-width-one-half"),
          value   = ValueViewModel(value),
        )
    }
}
