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
import models.{Index, NormalMode, UserAnswers}
import play.api.i18n.Messages
import queries.EuVatDetailsQuery
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AddAdditionalEuVatDetailsSummary  {

  def rows(answers: UserAnswers)(implicit messages: Messages): List[SummaryListRow] =
    answers.get(EuVatDetailsQuery).getOrElse(List.empty).zipWithIndex.map {
      case (details, index) =>
        SummaryListRowViewModel(
          key     = KeyViewModel(details.vatRegisteredEuMemberState).withCssClass("hmrc-add-to-a-list__identifier--light"),
          value   = ValueViewModel(details.euVatNumber),
          actions = Seq(
            ActionItemViewModel("site.change", routes.VatRegisteredEuMemberStateController.onPageLoad(NormalMode, Index(index)).url)
              .withVisuallyHiddenText(messages("vatRegisteredEuMemberState.change.hidden", details.vatRegisteredEuMemberState)),
            ActionItemViewModel("site.remove", routes.IndexController.onPageLoad().url) // TODO: Change this!
              .withVisuallyHiddenText(messages("vatRegisteredEuMemberState.remove.hidden", details.vatRegisteredEuMemberState))
          )
        )
    }
}
