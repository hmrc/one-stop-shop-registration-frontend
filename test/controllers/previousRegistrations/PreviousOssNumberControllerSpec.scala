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

package controllers.previousRegistrations

import base.SpecBase
import connectors.RegistrationConnector
import forms.previousRegistrations.PreviousOssNumberFormProvider
import models.core.{Match, TraderId}
import models.domain.PreviousSchemeNumbers
import models.previousRegistrations.PreviousSchemeHintText
import models.{Country, CountryWithValidationDetails, Index, NormalMode, PreviousScheme, RejoinMode}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.previousRegistrations.{PreviousEuCountryPage, PreviousOssNumberPage, PreviousSchemePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.AuthenticatedUserAnswersRepository
import services.{CoreRegistrationValidationService, RejoinRegistrationService}
import testutils.RegistrationData.registration
import utils.FutureSyntax.FutureOps
import views.html.previousRegistrations.PreviousOssNumberView

import java.time.LocalDate

class PreviousOssNumberControllerSpec extends SpecBase with MockitoSugar {

  private val index = Index(0)
  private val country = Country("SI", "Slovenia")
  private val countryWithValidation = CountryWithValidationDetails.euCountriesWithVRNValidationRules.find(_.country.code == "SI").value
  private val formProvider = new PreviousOssNumberFormProvider()
  private val form = formProvider(country, Seq.empty)

  private lazy val previousOssNumberRoute = routes.PreviousOssNumberController.onPageLoad(NormalMode, index, index).url

  private val baseAnswers = basicUserAnswersWithVatInfo.set(PreviousEuCountryPage(index), country).success.value


  "PreviousEuVatNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, previousOssNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PreviousOssNumberView]

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form, NormalMode, index, index, countryWithValidation,
          PreviousSchemeHintText.Both)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(PreviousOssNumberPage(index, index), PreviousSchemeNumbers("answer", None)).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, previousOssNumberRoute)

        val view = application.injector.instanceOf[PreviousOssNumberView]

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form.fill("answer"), NormalMode, index, index,
          countryWithValidation, PreviousSchemeHintText.Both)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" - {

      "when the ID starts with EU it sets to non-union" in {
        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

        val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]

        when(mockSessionRepository.set(any())) thenReturn true.toFuture

        when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture

        val application =
          applicationBuilder(userAnswers = Some(baseAnswers))
            .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
            .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, previousOssNumberRoute)
              .withFormUrlEncodedBody(("value", "EU123456789"))

          val result = route(application, request).value
          val expectedAnswers = baseAnswers
            .set(PreviousOssNumberPage(index, index), PreviousSchemeNumbers("EU123456789", None)).success.value
            .set(PreviousSchemePage(index, index), PreviousScheme.OSSNU).success.value

          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` PreviousOssNumberPage(index, index).navigate(NormalMode, expectedAnswers).url
          verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
        }
      }

      "when the ID doesn't start with EU it sets to union" in {
        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

        val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]

        when(mockSessionRepository.set(any())) thenReturn true.toFuture

        when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture

        val application =
          applicationBuilder(userAnswers = Some(baseAnswers))
            .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
            .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, previousOssNumberRoute)
              .withFormUrlEncodedBody(("value", "SI12345678"))

          val result = route(application, request).value
          val expectedAnswers = baseAnswers
            .set(PreviousOssNumberPage(index, index), PreviousSchemeNumbers("SI12345678", None)).success.value
            .set(PreviousSchemePage(index, index), PreviousScheme.OSSU).success.value

          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` PreviousOssNumberPage(index, index).navigate(NormalMode, expectedAnswers).url
          verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
        }
      }
    }

    "when other country validation is enabled" - {

      val genericMatch = Match(
        TraderId("IM0987654321"),
        None,
        "DE",
        None,
        None,
        exclusionEffectiveDate = Some(LocalDate.now()),
        None,
        None
      )

      "Redirect to scheme still active when active OSS found" in {

        val countryCode = genericMatch.memberState

        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]
        val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]

        when(mockSessionRepository.set(any())) thenReturn true.toFuture
        when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn Some(genericMatch).toFuture

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
            FakeRequest(POST, previousOssNumberRoute)
              .withFormUrlEncodedBody(("value", "SI12345678"))

          val result = route(application, request).value

          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` controllers.previousRegistrations.routes.SchemeStillActiveController.onPageLoad(NormalMode, countryCode, index, index).url
          verify(mockCoreRegistrationValidationService, times(1)).searchScheme(any(), any(), any(), any())(any(), any())
        }
      }

      "Redirect to Rejoin Already Registered Other Country when active OSS found and mode=RejoinMode" in {
        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]
        val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]
        val mockRegistrationConnector = mock[RegistrationConnector]
        val mockRejoinRegistrationService = mock[RejoinRegistrationService]

        when(mockSessionRepository.set(any())) thenReturn true.toFuture
        when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn Some(genericMatch).toFuture
        when(mockRegistrationConnector.getRegistration()(any())) thenReturn Some(registration).toFuture
        when(mockRejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true

        val application =
          applicationBuilder(userAnswers = Some(baseAnswers))
            .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
            .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
            .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
            .overrides(bind[RejoinRegistrationService].toInstance(mockRejoinRegistrationService))
            .configure(
              "features.other-country-reg-validation-enabled" -> true
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, controllers.previousRegistrations.routes.PreviousOssNumberController.onPageLoad(RejoinMode, index, index).url)
              .withFormUrlEncodedBody(("value", "SI12345678"))

          val result = route(application, request).value

          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` controllers.rejoin.routes.RejoinAlreadyRegisteredOtherCountryController.onPageLoad(
            genericMatch.memberState).url
        }
      }

      "Redirect to scheme quarantined when quarantined OSS found" in {

        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]
        val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]

        when(mockSessionRepository.set(any())) thenReturn true.toFuture
        when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn
          Some(genericMatch.copy(exclusionStatusCode = Some(4))).toFuture

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
            FakeRequest(POST, previousOssNumberRoute)
              .withFormUrlEncodedBody(("value", "SI12345678"))

          val result = route(application, request).value

          status(result) `mustBe` SEE_OTHER
          verify(mockCoreRegistrationValidationService, times(1)).searchScheme(any(), any(), any(), any())(any(), any())
        }
      }

      "Redirect to Cannot Rejoin Quarantined Country when quarantined OSS found and mode=RejoinMode" in {
        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]
        val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]
        val mockRegistrationConnector = mock[RegistrationConnector]
        val mockRejoinRegistrationService = mock[RejoinRegistrationService]

        when(mockSessionRepository.set(any())) thenReturn true.toFuture
        when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn
          Some(genericMatch.copy(exclusionStatusCode = Some(4))).toFuture
        when(mockRegistrationConnector.getRegistration()(any())) thenReturn Some(registration).toFuture
        when(mockRejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true

        val application =
          applicationBuilder(userAnswers = Some(baseAnswers))
            .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
            .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
            .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
            .overrides(bind[RejoinRegistrationService].toInstance(mockRejoinRegistrationService))
            .configure(
              "features.other-country-reg-validation-enabled" -> true
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, controllers.previousRegistrations.routes.PreviousOssNumberController.onPageLoad(RejoinMode, index, index).url)
              .withFormUrlEncodedBody(("value", "SI12345678"))

          val result = route(application, request).value

          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` controllers.rejoin.routes.CannotRejoinQuarantinedCountryController.onPageLoad(
            genericMatch.memberState, genericMatch.exclusionEffectiveDate.mkString).url
        }
      }

      "save and redirect to the next page when non-compliant details populated" in {

        val genericMatchWithNonCompliantDetails = genericMatch.copy(
          exclusionStatusCode = Some(6),
          nonCompliantReturns = Some(1),
          nonCompliantPayments = Some(1),
        )

        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]
        val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]

        when(mockSessionRepository.set(any())) thenReturn true.toFuture
        when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn Some(genericMatchWithNonCompliantDetails).toFuture

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
            FakeRequest(POST, previousOssNumberRoute)
              .withFormUrlEncodedBody(("value", "SI12345678"))

          val result = route(application, request).value

          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` controllers.previousRegistrations.routes.CheckPreviousSchemeAnswersController.onPageLoad(NormalMode, index).url
          verify(mockCoreRegistrationValidationService, times(1)).searchScheme(any(), any(), any(), any())(any(), any())
        }
      }

      "not call core validation when OSS Non Union" in {

        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]
        val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]

        when(mockSessionRepository.set(any())) thenReturn true.toFuture
        when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn
          Some(genericMatch.copy(exclusionStatusCode = Some(4))).toFuture

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
            FakeRequest(POST, previousOssNumberRoute)
              .withFormUrlEncodedBody(("value", "EU123456789"))

          val result = route(application, request).value
          val expectedAnswers = baseAnswers
            .set(PreviousOssNumberPage(index, index), PreviousSchemeNumbers("EU123456789", None)).success.value
            .set(PreviousSchemePage(index, index), PreviousScheme.OSSNU).success.value

          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` PreviousOssNumberPage(index, index).navigate(NormalMode, expectedAnswers).url
          verify(mockCoreRegistrationValidationService, times(0)).searchScheme(any(), any(), any(), any())(any(), any())
        }

      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, previousOssNumberRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PreviousOssNumberView]

        val result = route(application, request).value

        status(result) `mustBe` BAD_REQUEST
        contentAsString(result) `mustBe` view(boundForm, NormalMode, index, index, countryWithValidation,
          PreviousSchemeHintText.Both)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, previousOssNumberRoute)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if user answers are empty" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request = FakeRequest(GET, previousOssNumberRoute)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, previousOssNumberRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
