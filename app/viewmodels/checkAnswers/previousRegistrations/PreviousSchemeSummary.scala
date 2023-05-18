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

package viewmodels.checkAnswers.previousRegistrations

import controllers.previousRegistrations.routes
import models.{Index, Mode, PreviousScheme, UserAnswers}
import pages.previousRegistrations.PreviousSchemePage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object PreviousSchemeSummary {

  def row(answers: UserAnswers, countryIndex: Index, schemeIndex: Index, existingPreviousSchemes: Seq[PreviousScheme], mode: Mode)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(PreviousSchemePage(countryIndex, schemeIndex)).map {
      answer =>

        val isExistingScheme = existingPreviousSchemes.contains(answer)

        val value = ValueViewModel(
          HtmlContent(
            HtmlFormat.escape(messages(s"previousScheme.$answer"))
          )
        )

        SummaryListRowViewModel(
          key = "previousScheme.checkYourAnswersLabel",
          value = value,
          actions = if (!isExistingScheme) {
            Seq(
              ActionItemViewModel("site.remove", routes.DeletePreviousSchemeController.onPageLoad(mode, countryIndex, schemeIndex).url)
                .withVisuallyHiddenText(messages("site.remove.hidden"))
            )
          } else {
            Seq.empty
          }
        )
    }

}
