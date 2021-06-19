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

package viewmodels.checkAnswers

import controllers.routes
import formats.Format.dateFormatter
import models.AlreadyMadeSales.{No, Yes}
import models.{CheckMode, UserAnswers}
import pages.AlreadyMadeSalesPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AlreadyMadeSalesSummary  {

  def answerRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AlreadyMadeSalesPage).map {
      alreadyMadeSales =>

        val value = alreadyMadeSales match {
          case Yes(_) => "site.yes"
          case No     => "site.no"
        }

        SummaryListRowViewModel(
          key     = "alreadyMadeSales.answer.checkYourAnswersLabel",
          value   = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", routes.AlreadyMadeSalesController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("alreadyMadeSales.change.hidden"))
          )
        )
    }

  def dateOfFirstSaleRow(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AlreadyMadeSalesPage).flatMap {
      alreadyMadeSales =>

        alreadyMadeSales match {
          case Yes(date) =>
            Some(
              SummaryListRowViewModel(
                key     = "alreadyMadeSales.firstSale.checkYourAnswersLabel",
                value   = ValueViewModel(date.format(dateFormatter)),
                actions = Seq(
                  ActionItemViewModel("site.change", routes.AlreadyMadeSalesController.onPageLoad(CheckMode).url)
                    .withVisuallyHiddenText(messages("alreadyMadeSales.change.hidden"))
                )
              )
            )

          case No =>
            None
        }
    }
}
