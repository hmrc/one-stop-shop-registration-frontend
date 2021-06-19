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

package controllers.euDetails

import base.SpecBase
import models.{Country, Index, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.euDetails
import pages.euDetails.CheckEuDetailsAnswersPage
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import viewmodels.checkAnswers.euDetails.EuCountrySummary
import viewmodels.govuk.SummaryListFluency
import views.html.euDetails.CheckEuDetailsAnswersView

import scala.concurrent.Future

class CheckEuDetailsAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar with BeforeAndAfterEach {

  private val index                 = Index(0)
  private val country               = Country.euCountries.head
  private val baseUserAnswers       = emptyUserAnswers.set(euDetails.EuCountryPage(index), country).success.value
  private val mockSessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    Mockito.reset(mockSessionRepository)
  }

  "CheckEuVatDetailsAnswersController" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)
        val request = FakeRequest(GET, routes.CheckEuDetailsAnswersController.onPageLoad(NormalMode, index).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckEuDetailsAnswersView]
        val list = SummaryListViewModel(
          Seq(EuCountrySummary.row(baseUserAnswers, index)).flatten
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, NormalMode, index, country)(request, messages(application)).toString
      }
    }

    "on a POST" - {

      "must redirect to the next page" in {

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        running(application) {
          val request = FakeRequest(POST, routes.CheckEuDetailsAnswersController.onSubmit(NormalMode, index).url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual CheckEuDetailsAnswersPage.navigate(NormalMode, emptyUserAnswers).url
        }
      }
    }
  }
}
