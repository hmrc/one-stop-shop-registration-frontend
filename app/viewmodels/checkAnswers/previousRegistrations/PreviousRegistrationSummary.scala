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

package viewmodels.checkAnswers.previousRegistrations

import controllers.previousRegistrations.routes
import models.{CheckMode, Index, Mode, UserAnswers}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import queries.AllPreviousRegistrationsQuery
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.ListItem
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object PreviousRegistrationSummary {

  def addToListRows(answers: UserAnswers, mode: Mode): Seq[ListItem] =
    answers.get(AllPreviousRegistrationsQuery).getOrElse(List.empty).zipWithIndex.map {
      case (details, index) =>
        ListItem(
          name = HtmlFormat.escape(details.previousEuCountry.name).toString,
          changeUrl = routes.PreviousEuCountryController.onPageLoad(mode, Index(index)).url,
          removeUrl = routes.DeletePreviousRegistrationController.onPageLoad(mode, Index(index)).url
        )
    }

  def checkAnswersRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AllPreviousRegistrationsQuery).map {
      previousRegistrations =>

        val value = previousRegistrations.map {
          details =>
            HtmlFormat.escape(details.previousEuCountry.name) + " - " + HtmlFormat.escape(details.previousEuVatNumber)
        }.mkString("<br/>")

        SummaryListRowViewModel(
          key = "previousRegistrations.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", routes.AddPreviousRegistrationController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("previousRegistrations.change.hidden"))
          )
        )
    }
}
