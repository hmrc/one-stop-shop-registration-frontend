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
import models.{CheckMode, Index, NormalMode, UserAnswers}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import queries.AllEuDetailsQuery
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.ListItem
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object EuDetailsSummary {

  def addToListRows(answers: UserAnswers): Seq[ListItem] =
    answers.get(AllEuDetailsQuery).getOrElse(List.empty).zipWithIndex.map {
      case (details, index) =>
        ListItem(
          name = HtmlFormat.escape(details.euCountry.name).toString,
          changeUrl = routes.CheckEuDetailsAnswersController.onPageLoad(CheckMode, Index(index)).url,
          removeUrl = routes.DeleteEuDetailsController.onPageLoad(NormalMode, Index(index)).url
        )
    }

  def checkAnswersRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AllEuDetailsQuery).map {
      euVatDetails =>

        val value = euVatDetails.map {
          details =>
            HtmlFormat.escape(details.euCountry.name)
        }.mkString("<br/>")

        SummaryListRowViewModel(
          key = "euVatDetails.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", routes.AddEuDetailsController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("euVatDetails.change.hidden"))
          )
        )
    }
}
