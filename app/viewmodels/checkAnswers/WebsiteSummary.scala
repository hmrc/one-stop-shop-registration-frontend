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

package viewmodels.checkAnswers

import controllers.routes
import models.{Index, Mode, UserAnswers}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import queries.AllWebsites
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.ListItem
import viewmodels.ListItemWrapper
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object WebsiteSummary {

  def addToListRows(answers: UserAnswers, mode: Mode): Seq[ListItemWrapper] =
    answers.get(AllWebsites).getOrElse(List.empty).zipWithIndex.map {
      case (website, index) =>
        ListItemWrapper(
          ListItem(
            name = HtmlFormat.escape(website).toString,
            changeUrl = routes.WebsiteController.onPageLoad(mode, Index(index)).url,
            removeUrl = routes.DeleteWebsiteController.onPageLoad(mode, Index(index)).url
          ),
          removeButtonEnabled = true
        )
    }

  def checkAnswersRow(answers: UserAnswers, mode: Mode)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AllWebsites).map {
      websites =>

        val value = websites.map {
          name =>
            HtmlFormat.escape(name)
        }.mkString("<br/>")

        SummaryListRowViewModel(
          key = "websites.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", routes.AddWebsiteController.onPageLoad(mode).url)
              .withVisuallyHiddenText(messages("websites.change.hidden"))
          )
        )
    }
}
