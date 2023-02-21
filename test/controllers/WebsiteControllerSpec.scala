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

package controllers

import base.SpecBase
import forms.WebsiteFormProvider
import models.{Index, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import pages.WebsitePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import views.html.WebsiteView

import scala.concurrent.Future

class WebsiteControllerSpec extends SpecBase with MockitoSugar {

  private val index = Index(0)

  private val formProvider = new WebsiteFormProvider()
  private val form = formProvider(index, Seq.empty)

  private lazy val websiteRoute = routes.WebsiteController.onPageLoad(NormalMode, index).url

  "Website Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, websiteRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WebsiteView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, index)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(WebsitePage(Index(0)), "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, websiteRoute)

        val view = application.injector.instanceOf[WebsiteView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode, index)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(basicUserAnswers))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, websiteRoute)
            .withFormUrlEncodedBody(("value", "www.example.com"))

        val result = route(application, request).value
        val expectedAnswers = basicUserAnswers.set(WebsitePage(index), "www.example.com").success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual WebsitePage(index).navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, websiteRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[WebsiteView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, index)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, websiteRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, websiteRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return NOT_FOUND for a GET with an index of position 10 or greater" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswers)).build()
      val highIndex = Gen.choose(10, Int.MaxValue).map(Index(_)).sample.value

      running(application) {

        val request = FakeRequest(GET, routes.WebsiteController.onPageLoad(NormalMode, highIndex).url)

        val result = route(application, request).value

        status(result) mustEqual NOT_FOUND
      }
    }

    "must return NOT_FOUND for a POST with an index of position 10 or greater" in {

      val answers =
        basicUserAnswers
          .set(WebsitePage(Index(0)), "foo").success.value
          .set(WebsitePage(Index(1)), "foo").success.value
          .set(WebsitePage(Index(2)), "foo").success.value
          .set(WebsitePage(Index(3)), "foo").success.value
          .set(WebsitePage(Index(4)), "foo").success.value
          .set(WebsitePage(Index(5)), "foo").success.value
          .set(WebsitePage(Index(6)), "foo").success.value
          .set(WebsitePage(Index(7)), "foo").success.value
          .set(WebsitePage(Index(8)), "foo").success.value
          .set(WebsitePage(Index(9)), "foo").success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()
      val highIndex = Gen.choose(10, Int.MaxValue).map(Index(_)).sample.value

      running(application) {

        val request =
          FakeRequest(POST, routes.WebsiteController.onPageLoad(NormalMode, highIndex).url)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual NOT_FOUND
      }
    }
  }
}
