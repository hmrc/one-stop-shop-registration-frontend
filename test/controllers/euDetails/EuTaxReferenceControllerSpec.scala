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
import forms.euDetails.EuTaxReferenceFormProvider
import models.{Country, Index, NormalMode}
import models.core.{Match, MatchType}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.euDetails.{EuCountryPage, EuTaxReferencePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import services.CoreRegistrationValidationService
import views.html.euDetails.EuTaxReferenceView

import scala.concurrent.Future

class EuTaxReferenceControllerSpec extends SpecBase with MockitoSugar {

  private val index = Index(0)

  private val country = Country.euCountries.head
  private val formProvider = new EuTaxReferenceFormProvider()
  private val form = formProvider(country)

  private lazy val euTaxReferenceRoute = routes.EuTaxReferenceController.onPageLoad(NormalMode, index).url

  private lazy val euTaxReferenceSubmitRoute = routes.EuTaxReferenceController.onSubmit(NormalMode, index).url

  private val baseUserAnswers = basicUserAnswersWithVatInfo.set(EuCountryPage(index), country).success.value

  private val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]

  private val genericMatch = Match(
    MatchType.FixedEstablishmentActiveNETP,
    "333333333",
    None,
    "DE",
    None,
    None,
    None,
    None,
    None
  )

  "EuTaxReference Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
        .configure(
          "features.other-country-reg-validation-enabled" -> false
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, euTaxReferenceRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EuTaxReferenceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, index, country)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseUserAnswers.set(EuTaxReferencePage(index), "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .configure(
          "features.other-country-reg-validation-enabled" -> false
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, euTaxReferenceRoute)

        val view = application.injector.instanceOf[EuTaxReferenceView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode, index, country)(request, messages(application)).toString
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
          FakeRequest(POST, euTaxReferenceRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value
        val expectedAnswers = baseUserAnswers.set(EuTaxReferencePage(index), "answer").success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual EuTaxReferencePage(index).navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, euTaxReferenceRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[EuTaxReferenceView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, index, country)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, euTaxReferenceRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if user answers are empty" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request = FakeRequest(GET, euTaxReferenceRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, euTaxReferenceRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "when other country registration validation toggle is true" - {

      "must redirect to FixedEstablishmentVRNAlreadyRegisteredController page when matchType=FixedEstablishmentActiveNETP" in {

        val taxReferenceNumber: String = "333333333"

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
          ).build()

        running(application) {

          when(mockCoreRegistrationValidationService.searchEuTaxId(eqTo(taxReferenceNumber), eqTo(country.code))(any(), any())) thenReturn
            Future.successful(Option(genericMatch))

          when(mockCoreRegistrationValidationService.isActiveTrader(genericMatch)) thenReturn true

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute)
            .withFormUrlEncodedBody(("value", taxReferenceNumber))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.FixedEstablishmentVRNAlreadyRegisteredController.onPageLoad(NormalMode, index).url
        }
      }

      "must redirect to FixedEstablishmentVRNAlreadyRegisteredController page when matchType=TraderIdActiveNETP" in {

        val taxReferenceNumber: String = "333333333"

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
          ).build()

        running(application) {

          val expectedResponse = genericMatch.copy(matchType = MatchType.TraderIdActiveNETP)

          when(mockCoreRegistrationValidationService.searchEuTaxId(eqTo(taxReferenceNumber), eqTo(country.code))(any(), any())) thenReturn
            Future.successful(Option(expectedResponse))

          when(mockCoreRegistrationValidationService.isActiveTrader(expectedResponse)) thenReturn true

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute)
            .withFormUrlEncodedBody(("value", taxReferenceNumber))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.FixedEstablishmentVRNAlreadyRegisteredController.onPageLoad(NormalMode, index).url
        }
      }

      "must redirect to FixedEstablishmentVRNAlreadyRegisteredController page when matchType=OtherMSNETPActiveNETP" in {

        val taxReferenceNumber: String = "333333333"

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
          ).build()

        running(application) {

          val expectedResponse = genericMatch.copy(matchType = MatchType.OtherMSNETPActiveNETP)

          when(mockCoreRegistrationValidationService.searchEuTaxId(eqTo(taxReferenceNumber), eqTo(country.code))(any(), any())) thenReturn
            Future.successful(Option(expectedResponse))

          when(mockCoreRegistrationValidationService.isActiveTrader(expectedResponse)) thenReturn true

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute)
            .withFormUrlEncodedBody(("value", taxReferenceNumber))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.FixedEstablishmentVRNAlreadyRegisteredController.onPageLoad(NormalMode, index).url
        }
      }


      "must redirect to ExcludedVRNController page when the vat number is excluded for match FixedEstablishmentQuarantinedNETP " in {

        val taxReferenceNumber: String = "333333333"

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
          ).build()

        running(application) {

          val expectedResponse = genericMatch.copy(matchType = MatchType.FixedEstablishmentQuarantinedNETP)

          when(mockCoreRegistrationValidationService.searchEuTaxId(eqTo(taxReferenceNumber), eqTo(country.code))(any(), any())) thenReturn
            Future.successful(Option(expectedResponse))

          when(mockCoreRegistrationValidationService.isQuarantinedTrader(expectedResponse)) thenReturn true

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute)
            .withFormUrlEncodedBody(("value", taxReferenceNumber))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ExcludedVRNController.onPageLoad().url
        }
      }

      "must redirect to ExcludedVRNController page when the vat number is excluded for match TraderIdQuarantinedNETP " in {

        val taxReferenceNumber: String = "333333333"

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
          ).build()

        running(application) {

          val expectedResponse = genericMatch.copy(matchType = MatchType.TraderIdQuarantinedNETP)

          when(mockCoreRegistrationValidationService.searchEuTaxId(eqTo(taxReferenceNumber), eqTo(country.code))(any(), any())) thenReturn
            Future.successful(Option(expectedResponse))

          when(mockCoreRegistrationValidationService.isQuarantinedTrader(expectedResponse)) thenReturn true

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute)
            .withFormUrlEncodedBody(("value", taxReferenceNumber))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ExcludedVRNController.onPageLoad().url
        }
      }

      "must redirect to ExcludedVRNController page when the vat number is excluded for match OtherMSNETPQuarantinedNETP " in {

        val taxReferenceNumber: String = "333333333"

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
          ).build()

        running(application) {

          val expectedResponse = genericMatch.copy(matchType = MatchType.OtherMSNETPQuarantinedNETP)

          when(mockCoreRegistrationValidationService.searchEuTaxId(eqTo(taxReferenceNumber), eqTo(country.code))(any(), any())) thenReturn
            Future.successful(Option(expectedResponse))

          when(mockCoreRegistrationValidationService.isQuarantinedTrader(expectedResponse)) thenReturn true

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute)
            .withFormUrlEncodedBody(("value", taxReferenceNumber))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ExcludedVRNController.onPageLoad().url
        }
      }

      "must redirect to the next page when there is no active trader" in {

        val taxReferenceNumber: String = "333333333"

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
          ).build()

        running(application) {

          val expectedResponse = genericMatch.copy(matchType = MatchType.TransferringMSID)

          when(mockCoreRegistrationValidationService.searchEuTaxId(eqTo(taxReferenceNumber), eqTo(country.code))(any(), any())) thenReturn
            Future.successful(Option(expectedResponse))

          when(mockCoreRegistrationValidationService.isActiveTrader(expectedResponse)) thenReturn false

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute)
            .withFormUrlEncodedBody(("value", taxReferenceNumber))

          val result = route(application, request).value

          val expectedAnswers = baseUserAnswers.set(EuTaxReferencePage(index), taxReferenceNumber).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual EuTaxReferencePage(index).navigate(NormalMode, expectedAnswers).url
        }
      }

      "must redirect to the next page when there is no excluded trader" in {

        val taxReferenceNumber: String = "333333333"

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
          ).build()

        running(application) {

          val expectedResponse = genericMatch.copy(matchType = MatchType.TransferringMSID)

          when(mockCoreRegistrationValidationService.searchEuTaxId(eqTo(taxReferenceNumber), eqTo(country.code))(any(), any())) thenReturn
            Future.successful(Option(expectedResponse))

          when(mockCoreRegistrationValidationService.isQuarantinedTrader(expectedResponse)) thenReturn false

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute)
            .withFormUrlEncodedBody(("value", taxReferenceNumber))

          val result = route(application, request).value

          val expectedAnswers = baseUserAnswers.set(EuTaxReferencePage(index), taxReferenceNumber).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual EuTaxReferencePage(index).navigate(NormalMode, expectedAnswers).url
        }
      }

      "must redirect to the next page when no active match found" in {

        val taxReferenceNumber: String = "333333333"

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
          ).build()

        running(application) {

          when(mockCoreRegistrationValidationService.searchEuTaxId(eqTo(taxReferenceNumber), eqTo(country.code))(any(), any())) thenReturn
            Future.successful(None)

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute)
            .withFormUrlEncodedBody(("value", taxReferenceNumber))

          val result = route(application, request).value

          val expectedAnswers = baseUserAnswers.set(EuTaxReferencePage(index), taxReferenceNumber).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual EuTaxReferencePage(index).navigate(NormalMode, expectedAnswers).url
        }
      }
    }

    "when other country registration validation toggle is false" - {

      "must save the answer and redirect to the next page when valid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> false
          )
          .build()

        running(application) {

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute)
            .withFormUrlEncodedBody(("value", "333333333"))

          val result = route(application, request).value

          val expectedAnswers = baseUserAnswers.set(EuTaxReferencePage(index), "333333333").success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual EuTaxReferencePage(index).navigate(NormalMode, expectedAnswers).url
        }
      }
    }
  }
}
