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
import forms.previousRegistrations.PreviousIossRegistrationNumberFormProvider
import models.{Country, Index, NormalMode, PreviousScheme}
import models.core.{Match, MatchType}
import models.domain.PreviousSchemeNumbers
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.previousRegistrations.{PreviousEuCountryPage, PreviousIossNumberPage, PreviousIossSchemePage, PreviousSchemePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import services.CoreRegistrationValidationService
import views.html.previousRegistrations.PreviousIossNumberView

import scala.concurrent.Future

class PreviousIossNumberControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new PreviousIossRegistrationNumberFormProvider()

  private val index = Index(0)

  private val country = Country.euCountries.head
  private val baseAnswers = emptyUserAnswers
    .set(PreviousEuCountryPage(index), country).success.value
    .set(PreviousSchemePage(index, index), PreviousScheme.OSSU).success.value
    .set(PreviousIossSchemePage(index, index), false).success.value

  private lazy val previousIossNumberRoute = controllers.previousRegistrations.routes.PreviousIossNumberController.onPageLoad(NormalMode, index, index).url

  private val hasIntermediary: Boolean = false

  private val form = formProvider(country, hasIntermediary)

  private val ossHintText = "This will start with IM040 followed by 7 numbers"

  "PreviousIossNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, previousIossNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PreviousIossNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, index, index, country,
          hasIntermediary = false, ossHintText, "")(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers
        .set(PreviousIossNumberPage(index, index), PreviousSchemeNumbers("answer", None)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, previousIossNumberRoute)

        val view = application.injector.instanceOf[PreviousIossNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(PreviousSchemeNumbers("answer", None)),
          NormalMode, index, index, country, hasIntermediary = false, ossHintText, "")(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]
      val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn Future.successful(None)

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, previousIossNumberRoute)
            .withFormUrlEncodedBody(("previousSchemeNumber", "IM0401234567"))

        val result = route(application, request).value
        val expectedAnswers = baseAnswers.set(PreviousIossNumberPage(index, index), PreviousSchemeNumbers("IM0401234567", None)).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual PreviousIossNumberPage(index, index).navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "when other country validation is enabled" - {

      val genericMatch = Match(
        MatchType.FixedEstablishmentActiveNETP,
        "IM0987654321",
        None,
        "DE",
        None,
        None,
        None,
        None,
        None
      )

      "continue normally when active IOSS found" in {

        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]
        val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn Future.successful(Some(genericMatch))
        when(mockCoreRegistrationValidationService.isActiveTrader(any())) thenReturn true

        val application =
          applicationBuilder(userAnswers = Some(baseAnswers))
            .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
            .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
            .configure(
              "features.other-country-reg-validation-enabled" -> true
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, previousIossNumberRoute)
              .withFormUrlEncodedBody(("previousSchemeNumber", "IM0401234567"))

          val result = route(application, request).value
          val expectedAnswers = baseAnswers.set(PreviousIossNumberPage(index, index), PreviousSchemeNumbers("IM0401234567", None)).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual PreviousIossNumberPage(index, index).navigate(NormalMode, expectedAnswers).url
          verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
        }

      }

      "Redirect to scheme quarantined when quarantined IOSS found" in {
        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]
        val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn
          Future.successful(Some(genericMatch.copy(matchType = MatchType.TraderIdQuarantinedNETP)))
        when(mockCoreRegistrationValidationService.isActiveTrader(any())) thenReturn false
        when(mockCoreRegistrationValidationService.isQuarantinedTrader(any())) thenReturn true

        val application =
          applicationBuilder(userAnswers = Some(baseAnswers))
            .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
            .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
            .configure(
              "features.other-country-reg-validation-enabled" -> true
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, previousIossNumberRoute)
              .withFormUrlEncodedBody(("previousSchemeNumber", "IM0401234567"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.previousRegistrations.routes.SchemeQuarantinedController.onPageLoad(NormalMode, index, index).url
        }
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]

      when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn Future.successful(None)

      val application = applicationBuilder(userAnswers = Some(baseAnswers))
        .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, previousIossNumberRoute)
            .withFormUrlEncodedBody(("previousSchemeNumber", ""))

        val boundForm = form.bind(Map("previousSchemeNumber" -> ""))

        val view = application.injector.instanceOf[PreviousIossNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, index, index, country,
          hasIntermediary = false, ossHintText, "")(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, previousIossNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, previousIossNumberRoute)
            .withFormUrlEncodedBody(("previousSchemeNumber", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
