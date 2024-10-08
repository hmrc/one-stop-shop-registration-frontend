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

package viewmodels.checkAnswers.previousRegistrations

import controllers.previousRegistrations.routes
import models.{AmendMode, Country, Mode, RejoinMode, UserAnswers}
import pages.previousRegistrations.PreviouslyRegisteredPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import queries.previousRegistration.AllPreviousRegistrationsQuery
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object PreviouslyRegisteredSummary {

  def row(answers: UserAnswers, mode: Mode)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(PreviouslyRegisteredPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key = "previouslyRegistered.checkYourAnswersLabel",
          value = ValueViewModel(value),
          actions = if((mode != AmendMode && mode != RejoinMode) ||
            (mode == AmendMode && answers.get(AllPreviousRegistrationsQuery).isEmpty) ||
          (mode == RejoinMode && answers.get(AllPreviousRegistrationsQuery).isEmpty))
          {
            Seq(
              ActionItemViewModel("site.change", routes.PreviouslyRegisteredController.onPageLoad(mode).url)
                .withVisuallyHiddenText(messages("previouslyRegistered.change.hidden"))
            )
          } else {
            Seq.empty
          }
        )
    }

  def amendedAnswersRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(PreviouslyRegisteredPage).map {
      answer =>

        val value = if (answer) "site.yes" else "site.no"

        SummaryListRowViewModel(
          key = KeyViewModel("previouslyRegistered.checkYourAnswersLabel").withCssClass("govuk-!-width-one-half"),
          value = ValueViewModel(value)
        )
    }

  def changedAnswersRow(changedAnswers: Seq[Country])(implicit messages: Messages): Option[SummaryListRow] =

    if (changedAnswers.nonEmpty) {
      val value = changedAnswers.map {
        details =>
          HtmlFormat.escape(details.name)
      }.mkString("<br/>")

      Some(
        SummaryListRowViewModel(
          key = KeyViewModel("previousRegistrations.checkYourAnswersLabel.changed").withCssClass("govuk-!-width-one-half"),
          value = ValueViewModel(HtmlContent(value)),
        )
      )
    } else {
      None
    }
}
