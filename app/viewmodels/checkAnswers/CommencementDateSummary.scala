/*
 * Copyright 2022 HM Revenue & Customs
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

import formats.Format.dateFormatter
import models.UserAnswers
import pages.{DateOfFirstSalePage, IsPlanningFirstEligibleSalePage}
import play.api.i18n.Messages
import services.DateService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import java.time.LocalDate
import javax.inject.Inject

class CommencementDateSummary @Inject()(dateService: DateService) {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    answers.get(DateOfFirstSalePage).isEmpty match {
      case true =>
        val startDate = LocalDate.now()
        answers.get(IsPlanningFirstEligibleSalePage).map {
          _ =>
            SummaryListRowViewModel(
              key = "commencementDate.checkYourAnswersLabel",
              value = ValueViewModel(startDate.format(dateFormatter)),
              actions = Seq.empty
            )
        }

      case _ =>
        answers.get(DateOfFirstSalePage).map {
          answer =>

            val startDate = dateService.startDateBasedOnFirstSale(answer)

            SummaryListRowViewModel(
              key = "commencementDate.checkYourAnswersLabel",
              value = ValueViewModel(startDate.format(dateFormatter)),
              actions = Seq.empty
            )
        }

    }
  }
}