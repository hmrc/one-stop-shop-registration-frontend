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

package controllers.euDetails

import base.SpecBase
import forms.euDetails.EuVatNumberFormProvider
import models.{Country, CountryWithValidationDetails, Index, NormalMode}
import models.core.{Match, MatchType}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.euDetails.{EuCountryPage, EuVatNumberPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import services.CoreRegistrationValidationService
import views.html.euDetails.EuVatNumberView

import scala.concurrent.Future

class EuVatNumberControllerSpec extends SpecBase with MockitoSugar {

  private val index = Index(0)

  private val country = Country("SI", "Slovenia")
  private val countryWithValidation = CountryWithValidationDetails.euCountriesWithVRNValidationRules.find(_.country.code == "SI").value
  private val formProvider = new EuVatNumberFormProvider()
  private val form = formProvider(country)

  private lazy val euVatNumberRoute = routes.EuVatNumberController.onPageLoad(NormalMode, index).url

  private lazy val euVatNumberSubmitRoute = routes.EuVatNumberController.onSubmit(NormalMode, index).url

  private val baseUserAnswers = basicUserAnswersWithVatInfo.set(EuCountryPage(index), country).success.value

  private val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]

  private val genericMatch = Match(
    MatchType.FixedEstablishmentActiveNETP,
    "33333333",
    None,
    "DE",
    None,
    None,
    None,
    None,
    None
  )

  "EuVatNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, euVatNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EuVatNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, index, countryWithValidation)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseUserAnswers.set(EuVatNumberPage(index), "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, euVatNumberRoute)

        val view = application.injector.instanceOf[EuVatNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode, index, countryWithValidation)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .configure(
            "features.other-country-reg-validation-enabled" -> false
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, euVatNumberRoute)
            .withFormUrlEncodedBody(("value", "SI12345678"))

        val result = route(application, request).value
        val expectedAnswers = baseUserAnswers.set(EuVatNumberPage(index), "SI12345678").success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual EuVatNumberPage(index).navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, euVatNumberRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[EuVatNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, index, countryWithValidation)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, euVatNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if the country for this index is not found" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request = FakeRequest(GET, euVatNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, euVatNumberRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "when other country registration validation toggle is true" - {

      "must redirect to FixedEstablishmentVRNAlreadyRegisteredController page when matchType=FixedEstablishmentActiveNETP" in {

        val euVrn: String = "SI33333333"

        val application =
          applicationBuilder(userAnswers = Some(baseUserAnswers))
            .configure(
              "features.other-country-reg-validation-enabled" -> true
            )
            .overrides(
              bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
            ).build()

        running(application) {

          when(mockCoreRegistrationValidationService.searchEuVrn(eqTo(euVrn), eqTo(country.code))) thenReturn
            Future.successful(Option(genericMatch))

          when(mockCoreRegistrationValidationService.isActiveTrader(genericMatch)) thenReturn true

          val request =
            FakeRequest(POST, euVatNumberSubmitRoute)
              .withFormUrlEncodedBody(("value", euVrn))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.FixedEstablishmentVRNAlreadyRegisteredController.onPageLoad().url
        }
      }

      "must redirect to FixedEstablishmentVRNAlreadyRegisteredController page when matchType=TraderIdActiveNETP" in {

        val euVrn: String = "SI33333333"

        val application =
          applicationBuilder(userAnswers = Some(baseUserAnswers))
            .configure(
              "features.other-country-reg-validation-enabled" -> true
            )
            .overrides(
              bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
            ).build()

        running(application) {

          val expectedResponse = genericMatch.copy(matchType = MatchType.TraderIdActiveNETP)

          when(mockCoreRegistrationValidationService.searchEuVrn(eqTo(euVrn), eqTo(country.code))) thenReturn
            Future.successful(Option(expectedResponse))

          when(mockCoreRegistrationValidationService.isActiveTrader(expectedResponse)) thenReturn true

          val request =
            FakeRequest(POST, euVatNumberSubmitRoute)
              .withFormUrlEncodedBody(("value", euVrn))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.FixedEstablishmentVRNAlreadyRegisteredController.onPageLoad().url
        }
      }

      "must redirect to FixedEstablishmentVRNAlreadyRegisteredController page when matchType=OtherMSNETPActiveNETP" in {

        val euVrn: String = "SI33333333"

        val application =
          applicationBuilder(userAnswers = Some(baseUserAnswers))
            .configure(
              "features.other-country-reg-validation-enabled" -> true
            )
            .overrides(
              bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
            ).build()

        running(application) {

          val expectedResponse = genericMatch.copy(matchType = MatchType.OtherMSNETPActiveNETP)

          when(mockCoreRegistrationValidationService.searchEuVrn(eqTo(euVrn), eqTo(country.code))) thenReturn
            Future.successful(Option(expectedResponse))

          when(mockCoreRegistrationValidationService.isActiveTrader(expectedResponse)) thenReturn true

          val request =
            FakeRequest(POST, euVatNumberSubmitRoute)
              .withFormUrlEncodedBody(("value", euVrn))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.FixedEstablishmentVRNAlreadyRegisteredController.onPageLoad().url
        }
      }

      "must redirect to ExcludedVRNController page when the vat number is excluded for match FixedEstablishmentQuarantinedNETP " in {

        val euVrn: String = "SI33333333"

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
          ).build()

        running(application) {

          val expectedResponse = genericMatch.copy(matchType = MatchType.FixedEstablishmentQuarantinedNETP)

          when(mockCoreRegistrationValidationService.searchEuVrn(eqTo(euVrn), eqTo(country.code))) thenReturn
            Future.successful(Option(expectedResponse))

          when(mockCoreRegistrationValidationService.isQuarantinedTrader(expectedResponse)) thenReturn true

          val request = FakeRequest(POST, euVatNumberSubmitRoute)
            .withFormUrlEncodedBody(("value", euVrn))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ExcludedVRNController.onPageLoad().url
        }
      }

      "must redirect to ExcludedVRNController page when the vat number is excluded for match TraderIdQuarantinedNETP " in {

        val euVrn: String = "SI33333333"

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
          ).build()

        running(application) {

          val expectedResponse = genericMatch.copy(matchType = MatchType.TraderIdQuarantinedNETP)

          when(mockCoreRegistrationValidationService.searchEuVrn(eqTo(euVrn), eqTo(country.code))) thenReturn
            Future.successful(Option(expectedResponse))

          when(mockCoreRegistrationValidationService.isQuarantinedTrader(expectedResponse)) thenReturn true

          val request = FakeRequest(POST, euVatNumberSubmitRoute)
            .withFormUrlEncodedBody(("value", euVrn))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ExcludedVRNController.onPageLoad().url
        }
      }

      "must redirect to ExcludedVRNController page when the vat number is excluded for match OtherMSNETPQuarantinedNETP " in {

        val euVrn: String = "SI33333333"

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
          ).build()

        running(application) {

          val expectedResponse = genericMatch.copy(matchType = MatchType.OtherMSNETPQuarantinedNETP)

          when(mockCoreRegistrationValidationService.searchEuVrn(eqTo(euVrn), eqTo(country.code))) thenReturn
            Future.successful(Option(expectedResponse))

          when(mockCoreRegistrationValidationService.isQuarantinedTrader(expectedResponse)) thenReturn true

          val request = FakeRequest(POST, euVatNumberSubmitRoute)
            .withFormUrlEncodedBody(("value", euVrn))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ExcludedVRNController.onPageLoad().url
        }
      }

      "must redirect to the next page when there is no active trader" in {

        val euVrn: String = "SI33333333"

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
          ).build()

        running(application) {

          val expectedResponse = genericMatch.copy(matchType = MatchType.TransferringMSID)

          when(mockCoreRegistrationValidationService.searchEuVrn(eqTo(euVrn), eqTo(country.code))) thenReturn
            Future.successful(Option(expectedResponse))

          when(mockCoreRegistrationValidationService.isActiveTrader(expectedResponse)) thenReturn false

          val request = FakeRequest(POST, euVatNumberSubmitRoute)
            .withFormUrlEncodedBody(("value", euVrn))

          val result = route(application, request).value

          val expectedAnswers = baseUserAnswers.set(EuVatNumberPage(index), euVrn).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual EuVatNumberPage(index).navigate(NormalMode, expectedAnswers).url
        }
      }

      "must redirect to the next page when there is no excluded trader" in {

        val euVrn: String = "SI33333333"

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
          ).build()

        running(application) {

          val expectedResponse = genericMatch.copy(matchType = MatchType.TransferringMSID)

          when(mockCoreRegistrationValidationService.searchEuVrn(eqTo(euVrn), eqTo(country.code))) thenReturn
            Future.successful(Option(expectedResponse))

          when(mockCoreRegistrationValidationService.isQuarantinedTrader(expectedResponse)) thenReturn false

          val request = FakeRequest(POST, euVatNumberSubmitRoute)
            .withFormUrlEncodedBody(("value", euVrn))

          val result = route(application, request).value

          val expectedAnswers = baseUserAnswers.set(EuVatNumberPage(index), euVrn).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual EuVatNumberPage(index).navigate(NormalMode, expectedAnswers).url
        }
      }

      "must redirect to the next page when no active match found" in {

        val euVrn: String = "SI33333333"

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
          ).build()

        running(application) {

          when(mockCoreRegistrationValidationService.searchEuVrn(eqTo(euVrn), eqTo(country.code))) thenReturn
            Future.successful(None)

          val request = FakeRequest(POST, euVatNumberSubmitRoute)
            .withFormUrlEncodedBody(("value", euVrn))

          val result = route(application, request).value

          val expectedAnswers = baseUserAnswers.set(EuVatNumberPage(index), euVrn).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual EuVatNumberPage(index).navigate(NormalMode, expectedAnswers).url
        }
      }
    }

    "when other country registration validation toggle is false" - {

      "redirect to the next page when valid data is submitted" in {

        val euVrn: String = "SI33333333"

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> false
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
          ).build()

        running(application) {

          val request = FakeRequest(POST, euVatNumberSubmitRoute)
            .withFormUrlEncodedBody(("value", euVrn))

          val result = route(application, request).value

          val expectedAnswers = baseUserAnswers.set(EuVatNumberPage(index), euVrn).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual EuVatNumberPage(index).navigate(NormalMode, expectedAnswers).url
        }
      }
    }
  }
}
