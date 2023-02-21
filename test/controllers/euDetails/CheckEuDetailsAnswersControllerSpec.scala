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

package controllers.euDetails

import base.SpecBase
import models.{Country, Index, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.euDetails
import pages.euDetails.{CheckEuDetailsAnswersPage, EuCountryPage, HasFixedEstablishmentPage, VatRegisteredPage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import viewmodels.checkAnswers.euDetails.{HasFixedEstablishmentSummary, VatRegisteredSummary}
import viewmodels.govuk.SummaryListFluency
import views.html.euDetails.CheckEuDetailsAnswersView

import scala.concurrent.Future

class CheckEuDetailsAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar with BeforeAndAfterEach {

  private val index                 = Index(0)
  private val country               = Country.euCountries.head
  private val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

  private val baseUserAnswers =
    basicUserAnswers
      .set(euDetails.EuCountryPage(index), Country.euCountries.head).success.value
      .set(euDetails.VatRegisteredPage(index), true).success.value

  private val answersNotRegisteredNoEstablishment = baseUserAnswers.set(EuCountryPage(index), country).success.value
    .set(VatRegisteredPage(index), false).success.value
    .set(HasFixedEstablishmentPage(index), false).success.value

  override def beforeEach(): Unit = {
    Mockito.reset(mockSessionRepository)
  }

  "CheckEuVatDetailsAnswersController" - {

    "must return OK and the correct view for a GET when answers are complete" in {

      val application = applicationBuilder(userAnswers = Some(answersNotRegisteredNoEstablishment)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)
        val request = FakeRequest(GET, routes.CheckEuDetailsAnswersController.onPageLoad(NormalMode, index).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckEuDetailsAnswersView]
        val list = SummaryListViewModel(
          Seq(VatRegisteredSummary.row(answersNotRegisteredNoEstablishment, index, NormalMode), HasFixedEstablishmentSummary.row(answersNotRegisteredNoEstablishment, index, NormalMode)).flatten
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, NormalMode, index, country)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when answers aren't complete" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckEuDetailsAnswersController.onPageLoad(NormalMode, index).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckEuDetailsAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, NormalMode, index, country, true)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery if user answers are empty" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckEuDetailsAnswersController.onPageLoad(NormalMode, index).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "on a POST" - {

      "must redirect to the next page when answers are complete" in {

        val application =
          applicationBuilder(userAnswers = Some(answersNotRegisteredNoEstablishment))
            .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
            .build()

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        running(application) {
          val request = FakeRequest(POST, routes.CheckEuDetailsAnswersController.onSubmit(NormalMode, index, false).url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual CheckEuDetailsAnswersPage.navigate(NormalMode, answersNotRegisteredNoEstablishment).url
        }
      }

      "must redirect to the Eu Country page when answers aren't complete and the prompt has been shown" in {

        val application =
          applicationBuilder(userAnswers = Some(baseUserAnswers))
            .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
            .build()

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        running(application) {
          val request = FakeRequest(POST, routes.CheckEuDetailsAnswersController.onSubmit(NormalMode, index, true).url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.EuCountryController.onPageLoad(NormalMode, index).url
        }
      }

      "must refresh the page when answers aren't complete and the prompt has not been shown" in {

        val application =
          applicationBuilder(userAnswers = Some(baseUserAnswers))
            .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
            .build()

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        running(application) {
          val request = FakeRequest(POST, routes.CheckEuDetailsAnswersController.onSubmit(NormalMode, index, false).url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.CheckEuDetailsAnswersController.onPageLoad(NormalMode, index).url
        }
      }
    }
  }
}
