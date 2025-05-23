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

package controllers

import base.SpecBase
import controllers.amend.routes as amendRoutes
import forms.AddWebsiteFormProvider
import models.{AmendMode, Index, NormalMode}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{AddWebsitePage, WebsitePage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.AuthenticatedUserAnswersRepository
import viewmodels.checkAnswers.WebsiteSummary
import views.html.AddWebsiteView

import scala.concurrent.Future

class AddWebsiteControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new AddWebsiteFormProvider()
  private val form = formProvider()

  private lazy val addWebsiteRoute = routes.AddWebsiteController.onPageLoad(NormalMode).url
  private lazy val addWebsiteAmendRoute = routes.AddWebsiteController.onPageLoad(AmendMode).url

  private val baseAnswers = basicUserAnswersWithVatInfo.set(WebsitePage(Index(0)), "foo").success.value

  "AddWebsite Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addWebsiteRoute)

        val view = application.injector.instanceOf[AddWebsiteView]
        implicit val msgs: Messages = messages(application)
        val list = WebsiteSummary.addToListRows(baseAnswers, NormalMode)

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form, NormalMode, list, canAddWebsites = true)(request, implicitly).toString
      }
    }

    "must return OK and the correct view for a GET when the maximum number of websites have already been added" in {

      val answers =
        basicUserAnswersWithVatInfo
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

      running(application) {
        val request = FakeRequest(GET, addWebsiteRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddWebsiteView]
        implicit val msgs: Messages = messages(application)
        val list = WebsiteSummary.addToListRows(answers, NormalMode)

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form, NormalMode, list, canAddWebsites = false)(request, implicitly).toString
      }
    }

    "must redirect to CheckYourAnswers and the correct view for a GET when cannot derive number of websites" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request = FakeRequest(GET, addWebsiteRoute)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` routes.CheckYourAnswersController.onPageLoad().url
      }
    }

    "must not populate the answer on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(AddWebsitePage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addWebsiteRoute)

        val view = application.injector.instanceOf[AddWebsiteView]
        implicit val msgs: Messages = messages(application)
        val list = WebsiteSummary.addToListRows(baseAnswers, NormalMode)

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsString(result) must not be view(form.fill(true), NormalMode, list, canAddWebsites = true)(request, implicitly).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, addWebsiteRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val expectedAnswers = baseAnswers.set(AddWebsitePage, true).success.value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` AddWebsitePage.navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addWebsiteRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AddWebsiteView]
        implicit val msgs: Messages = messages(application)
        val list = WebsiteSummary.addToListRows(baseAnswers, NormalMode)

        val result = route(application, request).value

        status(result) `mustBe` BAD_REQUEST
        contentAsString(result) `mustBe` view(boundForm, NormalMode, list, canAddWebsites = true)(request, implicitly).toString
      }
    }

    "in AmendMode" - {

      "must redirect to resolve missing answers and the correct view for a GET when cannot derive number of websites" in {

        val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
          .build()

        running(application) {
          val request = FakeRequest(GET, addWebsiteAmendRoute)

          val result = route(application, request).value

          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` amendRoutes.ChangeYourRegistrationController.onPageLoad().url
        }
      }
    }
  }
}
