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

package controllers.previousRegistrations

import base.SpecBase
import connectors.RegistrationConnector
import forms.previousRegistrations.AddPreviousRegistrationFormProvider
import models.domain.PreviousSchemeNumbers
import models.previousRegistrations.{PreviousRegistrationDetailsWithOptionalVatNumber, SchemeDetailsWithOptionalVatNumber}
import models.{AmendMode, Country, Index, NormalMode, PreviousScheme, PreviousSchemeType}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.previousRegistrations.{AddPreviousRegistrationPage, PreviousEuCountryPage, PreviousOssNumberPage, PreviousSchemePage, PreviousSchemeTypePage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import testutils.RegistrationData
import viewmodels.checkAnswers.previousRegistrations.PreviousRegistrationSummary
import views.html.previousRegistrations.AddPreviousRegistrationView

import scala.concurrent.Future

class AddPreviousRegistrationControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new AddPreviousRegistrationFormProvider()
  private val form = formProvider()

  private lazy val addPreviousRegistrationRoute = routes.AddPreviousRegistrationController.onPageLoad(NormalMode).url
  private def addPreviousRegistrationRoutePost(prompt: Boolean) = routes.AddPreviousRegistrationController.onSubmit(NormalMode, prompt).url

  private val baseAnswers =
    basicUserAnswersWithVatInfo
      .set(PreviousEuCountryPage(Index(0)), Country.euCountries.head).success.value
      .set(PreviousSchemePage(Index(0), Index(0)), PreviousScheme.OSSU).success.value
      .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers("foo", None)).success.value

  private val incompleteAnswers =
    basicUserAnswersWithVatInfo
      .set(PreviousEuCountryPage(Index(0)), Country.euCountries.head).success.value
      .set(PreviousSchemePage(Index(0), Index(0)), PreviousScheme.OSSU).success.value

  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]

  "AddPreviousRegistration Controller" - {

    "must return OK and the correct view for a GET when answers are complete" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addPreviousRegistrationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddPreviousRegistrationView]
        implicit val msgs: Messages = messages(application)
        val list                    = PreviousRegistrationSummary.addToListRows(baseAnswers, Seq.empty, NormalMode)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, list, canAddCountries = true)(request, implicitly).toString
      }
    }

    "must return OK and the correct view for a GET when answers aren't complete" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addPreviousRegistrationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddPreviousRegistrationView]
        implicit val msgs: Messages = messages(application)
        val list                    = PreviousRegistrationSummary.addToListRows(incompleteAnswers, Seq.empty, NormalMode)

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form,
            NormalMode,
            list,
            canAddCountries = true,
            Seq(
              PreviousRegistrationDetailsWithOptionalVatNumber(
                Country.euCountries.head,
                Some(List(SchemeDetailsWithOptionalVatNumber(Some(PreviousScheme.OSSU), None)))
              )
            )
          )(request, implicitly).toString
      }
    }

    "must return OK and the correct view for a GET when there are existing previous registrations in amend mode" in {

      val addPreviousRegistrationRouteAmend = routes.AddPreviousRegistrationController.onPageLoad(AmendMode).url

      when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(RegistrationData.registration))

      val existingPreviousRegistrations = RegistrationData.registration.previousRegistrations

      val application = applicationBuilder(userAnswers = Some(baseAnswers))
        .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, addPreviousRegistrationRouteAmend)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddPreviousRegistrationView]
        implicit val msgs: Messages = messages(application)
        val list = PreviousRegistrationSummary.addToListRows(baseAnswers, existingPreviousRegistrations, AmendMode)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, AmendMode, list, canAddCountries = true)(request, implicitly).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, addPreviousRegistrationRoutePost(false))
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val expectedAnswers = baseAnswers.set(AddPreviousRegistrationPage, true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual AddPreviousRegistrationPage.navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addPreviousRegistrationRoutePost(false))
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AddPreviousRegistrationView]
        implicit val msgs: Messages = messages(application)
        val list                    = PreviousRegistrationSummary.addToListRows(baseAnswers, Seq.empty, NormalMode)

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, list, canAddCountries = true)(request, implicitly).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, addPreviousRegistrationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if user answers are empty" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request = FakeRequest(GET, addPreviousRegistrationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, addPreviousRegistrationRoutePost(false))
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must refresh the page for a POST if answers are incomplete and prompt has not been shown" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addPreviousRegistrationRoutePost(false))
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.AddPreviousRegistrationController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to the PreviousEuVatNumber page for a POST if answers are incomplete and prompt has been shown" in {

      val incompleteAnswersWithPreviousSchemetype = incompleteAnswers
        .set(PreviousSchemeTypePage(Index(0), Index(0)), PreviousSchemeType.OSS).success.value
      val application = applicationBuilder(userAnswers = Some(incompleteAnswersWithPreviousSchemetype)).build()

      running(application) {
        val request =
          FakeRequest(POST, addPreviousRegistrationRoutePost(true))
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.PreviousOssNumberController.onPageLoad(NormalMode, Index(0), Index(0)).url
      }
    }

    "must redirect to the PreviousIOSSNumber page for a POST if answers are incomplete and prompt has been shown and previousScheme is set" in {

      val incompleteAnswersWithPreviousSchemetype = basicUserAnswersWithVatInfo
        .set(PreviousEuCountryPage(Index(0)), Country.euCountries.head).success.value
        .set(PreviousSchemeTypePage(Index(0), Index(0)), PreviousSchemeType.IOSS).success.value
        .set(PreviousSchemePage(Index(0), Index(0)), PreviousScheme.IOSSWI).success.value
      val application = applicationBuilder(userAnswers = Some(incompleteAnswersWithPreviousSchemetype)).build()

      running(application) {
        val request =
          FakeRequest(POST, addPreviousRegistrationRoutePost(true))
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.PreviousIossNumberController.onPageLoad(NormalMode, Index(0), Index(0)).url
      }
    }

    "must redirect to the PreviousIOSSScheme page for a POST if answers are incomplete and prompt has been shown and previousScheme is not set" in {

      val incompleteAnswersWithPreviousSchemetype = basicUserAnswersWithVatInfo
        .set(PreviousEuCountryPage(Index(0)), Country.euCountries.head).success.value
        .set(PreviousSchemeTypePage(Index(0), Index(0)), PreviousSchemeType.IOSS).success.value
      val application = applicationBuilder(userAnswers = Some(incompleteAnswersWithPreviousSchemetype)).build()

      running(application) {
        val request =
          FakeRequest(POST, addPreviousRegistrationRoutePost(true))
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.PreviousIossSchemeController.onPageLoad(NormalMode, Index(0), Index(0)).url
      }
    }

    "must redirect to the PreviousScheme page for a POST if answers are incomplete and prompt has been shown and previousSchemeDetails is not set" in {

      val incompleteAnswersWithPreviousSchemetype = basicUserAnswersWithVatInfo
        .set(PreviousEuCountryPage(Index(0)), Country.euCountries.head).success.value
      val application = applicationBuilder(userAnswers = Some(incompleteAnswersWithPreviousSchemetype)).build()

      running(application) {
        val request =
          FakeRequest(POST, addPreviousRegistrationRoutePost(true))
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.PreviousSchemeController.onPageLoad(NormalMode, Index(0), Index(0)).url
      }
    }

  }
}
