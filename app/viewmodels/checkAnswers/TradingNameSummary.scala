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
import queries.AllTradingNames
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.ListItem
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object TradingNameSummary  {

  def addToListRows(answers: UserAnswers, mode: Mode): Seq[ListItem] =
    answers.get(AllTradingNames).getOrElse(List.empty).zipWithIndex.map {
      case (name, index) =>
        ListItem(
          name      = HtmlFormat.escape(name).toString,
          changeUrl = routes.TradingNameController.onPageLoad(mode, Index(index)).url,
          removeUrl = routes.DeleteTradingNameController.onPageLoad(mode, Index(index)).url
        )
    }

  def checkAnswersRow(answers: UserAnswers, mode: Mode)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AllTradingNames).map {
      tradingNames =>

        val value = tradingNames.map {
          name =>
            HtmlFormat.escape(name)
        }.mkString("<br/>")

        SummaryListRowViewModel(
          key     = "tradingNames.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", routes.AddTradingNameController.onPageLoad(mode).url)
              .withVisuallyHiddenText(messages("tradingNames.change.hidden"))
          )
        )
    }
}
