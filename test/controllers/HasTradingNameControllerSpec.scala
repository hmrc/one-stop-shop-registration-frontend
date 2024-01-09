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
import controllers.routes
import controllers.amend.{routes => amendRoutes}
import connectors.RegistrationConnector
import forms.HasTradingNameFormProvider
import models.{AmendMode, NormalMode}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.HasTradingNamePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import views.html.HasTradingNameView

import scala.concurrent.Future

class HasTradingNameControllerSpec extends SpecBase with MockitoSugar {

  private val registeredCompanyName = "Company name"
  private val formProvider = new HasTradingNameFormProvider()
  private val form = formProvider()

  private lazy val hasTradingNameRoute = routes.HasTradingNameController.onPageLoad(NormalMode).url
  private lazy val hasTradingNameAmendRoute = routes.HasTradingNameController.onPageLoad(AmendMode).url

  private val baseUserAnswers = basicUserAnswersWithVatInfo.copy(vatInfo = Some(vatCustomerInfo))

  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]

  "HasTradingName Controller" - {

    "must return OK and the correct view for a GET when the user has answered the registered company name question" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, hasTradingNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HasTradingNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, registeredCompanyName)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when we have the user's company name in their VAT details" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request = FakeRequest(GET, hasTradingNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HasTradingNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, vatCustomerInfo.organisationName.get)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when we don't have the user's company name but has individual name in their VAT details" in {

      val individualName = "a b c"
      val vatCustomerInfoWithIndividualName = vatCustomerInfo.copy(organisationName = None, individualName = Some(individualName))

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo.copy(vatInfo = Some(vatCustomerInfoWithIndividualName)))).build()

      running(application) {
        val request = FakeRequest(GET, hasTradingNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HasTradingNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, individualName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseUserAnswers.set(HasTradingNamePage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, hasTradingNameRoute)

        val view = application.injector.instanceOf[HasTradingNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, registeredCompanyName)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, hasTradingNameRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val expectedAnswers = baseUserAnswers.set(HasTradingNamePage, true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual HasTradingNamePage.navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, hasTradingNameRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[HasTradingNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, registeredCompanyName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, hasTradingNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, hasTradingNameRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "in AmendMode" - {

      "must redirect to Amend Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, hasTradingNameAmendRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual amendRoutes.AmendJourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Amend Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, hasTradingNameAmendRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual amendRoutes.AmendJourneyRecoveryController.onPageLoad().url
        }
      }

    }
  }
}
