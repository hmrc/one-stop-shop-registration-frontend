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

package viewmodels.checkAnswers

import base.SpecBase
import formats.Format.dateFormatter
import models.requests.AuthenticatedDataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import pages.DateOfFirstSalePage
import play.api.inject.bind
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DateService
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.govuk.summarylist._

import scala.concurrent.{ExecutionContext, Future}

class CommencementDateSummarySpec extends SpecBase with MockitoSugar {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  private val request = AuthenticatedDataRequest(FakeRequest("GET", "/"), testCredentials, vrn, None, emptyUserAnswers)
  private implicit val dataRequest: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest(request, testCredentials, vrn, None, emptyUserAnswers)
  private val mockDateService = mock[DateService]

  ".row" - {

    "when the scheme has started" - {

      "must return a view model when DateOfFirstSale has been answered" in {

        when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(arbitraryDate)

        val answers = emptyUserAnswers
          .set(DateOfFirstSalePage, arbitraryDate).success.value

        val app = applicationBuilder(Some(answers), Some(stubClockAtArbitraryDate))
          .overrides(bind[DateService].toInstance(mockDateService))
          .build()

        running(app) {
          val viewmodel   = app.injector.instanceOf[CommencementDateSummary]
          val msgs        = messages(app)

          val row = viewmodel.row(answers)(msgs, ec, hc, dataRequest).futureValue
          row mustEqual SummaryListRowViewModel(
            key     = KeyViewModel(Text(msgs("commencementDate.checkYourAnswersLabel"))),
            value   = ValueViewModel(Text(answers.get(DateOfFirstSalePage).get.format(dateFormatter))),
            actions = Seq.empty
          )
        }
      }
    }
  }
}
