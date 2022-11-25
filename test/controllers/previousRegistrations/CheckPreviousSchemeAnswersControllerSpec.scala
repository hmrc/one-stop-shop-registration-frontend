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

package controllers.previousRegistrations

import base.SpecBase
import models.{Country, Index, NormalMode, PreviousSchemeType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.previousRegistrations.{CheckPreviousSchemeAnswersPage, PreviousEuCountryPage, PreviouslyRegisteredPage, PreviousOssNumberPage, PreviousSchemeTypePage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import viewmodels.checkAnswers.previousRegistrations._
import viewmodels.govuk.SummaryListFluency
import views.html.previousRegistrations.CheckPreviousSchemeAnswersView

import scala.concurrent.Future

class CheckPreviousSchemeAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar with BeforeAndAfterEach {

  private val index                 = Index(0)
  private val country               = Country.euCountries.head
  private val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

  private val baseUserAnswers =
    basicUserAnswersWithVatInfo
      .set(PreviouslyRegisteredPage, true).success.value
      .set(PreviousEuCountryPage(index), country).success.value
      .set(PreviousSchemeTypePage(index, index), PreviousSchemeType.values.head).success.value
      .set(PreviousOssNumberPage(index, index), "123456789").success.value

  override def beforeEach(): Unit = {
    Mockito.reset(mockSessionRepository)
  }

  "CheckEuVatDetailsAnswersController" - {

    "must return OK and the correct view for a GET when answers are complete" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)
        val request = FakeRequest(GET, controllers.previousRegistrations.routes.CheckPreviousSchemeAnswersController.onPageLoad(NormalMode, index).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckPreviousSchemeAnswersView]
        val list = SummaryListViewModel(
          Seq(
            PreviousSchemeSummary.row(baseUserAnswers, index),
            PreviousSchemeNumberSummary.row(baseUserAnswers, index)
          ).flatten
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, NormalMode, index, country)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery if user answers are empty" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.previousRegistrations.routes.CheckPreviousSchemeAnswersController.onPageLoad(NormalMode, index).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "on a POST" - {

      "must redirect to the next page when answers are complete" in {

        val application =
          applicationBuilder(userAnswers = Some(baseUserAnswers))
            .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
            .build()

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        running(application) {
          val request = FakeRequest(POST, controllers.previousRegistrations.routes.CheckPreviousSchemeAnswersController.onSubmit(NormalMode, index).url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual CheckPreviousSchemeAnswersPage(index).navigate(NormalMode, baseUserAnswers).url
        }
      }

    }
  }
}
