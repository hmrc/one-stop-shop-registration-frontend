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

import base.SpecBase
import formats.Format.dateFormatter
import org.scalatestplus.mockito.MockitoSugar
import pages.DateOfFirstSalePage
import play.api.test.Helpers._
import services.DateService
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.summarylist._

class CommencementDateSummarySpec extends SpecBase with MockitoSugar {

  ".row" - {

    "when the scheme has started" - {

      "must return a view model when DateOfFirstSale has been answered" in {

        val answers = emptyUserAnswers.set(DateOfFirstSalePage, arbitraryDate).success.value
        val app = applicationBuilder(Some(answers), Some(stubClockAtArbitraryDate)).build()

        running(app) {
          val viewmodel   = app.injector.instanceOf[CommencementDateSummary]
          val dateService = app.injector.instanceOf[DateService]
          val msgs        = messages(app)

          val row = viewmodel.row(answers)(msgs).value
          row mustEqual SummaryListRowViewModel(
            key     = KeyViewModel(Text(msgs("commencementDate.checkYourAnswersLabel"))),
            value   = ValueViewModel(Text(dateService.startDateBasedOnFirstSale(arbitraryDate).format(dateFormatter))),
            actions = Seq.empty
          )
        }
      }

      "must return None when DateOfFirstSale has not been answered" in {

        val app = applicationBuilder(Some(emptyUserAnswers), Some(stubClockAtArbitraryDate)).build()

        running(app) {
          val viewmodel = app.injector.instanceOf[CommencementDateSummary]
          val msgs      = messages(app)

          viewmodel.row(emptyUserAnswers)(msgs) must not be defined
        }
      }
    }
  }
}
