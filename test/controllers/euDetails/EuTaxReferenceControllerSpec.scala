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

package controllers.euDetails

import base.SpecBase
import connectors.RegistrationConnector
import forms.euDetails.EuTaxReferenceFormProvider
import models.{Country, Index, Mode, NormalMode, RejoinMode}
import models.core.{Match, MatchType}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.euDetails.{EuCountryPage, EuTaxReferencePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import services.{CoreRegistrationValidationService, RejoinRegistrationService}
import testutils.RegistrationData.registration
import views.html.euDetails.EuTaxReferenceView

import java.time.LocalDate
import scala.concurrent.Future

class EuTaxReferenceControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val index = Index(0)

  private val country = Country.euCountries.head
  private val formProvider = new EuTaxReferenceFormProvider()
  private val form = formProvider(country)

  private val baseUserAnswers = basicUserAnswersWithVatInfo.set(EuCountryPage(index), country).success.value

  private val genericMatch = Match(
    MatchType.FixedEstablishmentActiveNETP,
    "333333333",
    None,
    "DE",
    None,
    None,
    exclusionEffectiveDate = Some(LocalDate.now),
    None,
    None
  )

  private lazy val euTaxReferenceRoute: String = routes.EuTaxReferenceController.onPageLoad(NormalMode, index).url

  private def euTaxReferenceSubmitRoute(mode: Mode = NormalMode): String =
    routes.EuTaxReferenceController.onSubmit(mode, index).url

  private val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]
  private val mockRegistrationConnector = mock[RegistrationConnector]
  private val mockRejoinRegistrationService = mock[RejoinRegistrationService]

  override def beforeEach(): Unit = {
    Mockito.reset(mockCoreRegistrationValidationService)
    Mockito.reset(mockRegistrationConnector)
    Mockito.reset(mockRejoinRegistrationService)
  }

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

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute())
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

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute())
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
          
          val request = FakeRequest(POST, euTaxReferenceSubmitRoute())
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

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute())
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

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute())
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

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute())
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

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute())
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

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute())
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

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute())
            .withFormUrlEncodedBody(("value", taxReferenceNumber))

          val result = route(application, request).value

          val expectedAnswers = baseUserAnswers.set(EuTaxReferencePage(index), taxReferenceNumber).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual EuTaxReferencePage(index).navigate(NormalMode, expectedAnswers).url
        }
      }

      "must redirect to RejoinAlreadyRegisteredOtherCountryController page when matchType=FixedEstablishmentActiveNETP and mode=RejoinMode" in {

        val taxReferenceNumber: String = "333333333"

        when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
        when(mockRejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers), mode = Some(RejoinMode))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService),
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[RejoinRegistrationService].toInstance(mockRejoinRegistrationService)
          ).build()

        running(application) {

          when(mockCoreRegistrationValidationService.searchEuTaxId(eqTo(taxReferenceNumber), eqTo(country.code))(any(), any())) thenReturn
            Future.successful(Option(genericMatch))

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute(RejoinMode))
            .withFormUrlEncodedBody(("value", taxReferenceNumber))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.rejoin.routes.RejoinAlreadyRegisteredOtherCountryController.onPageLoad(genericMatch.memberState).url
        }
      }

      "must redirect to RejoinAlreadyRegisteredOtherCountryController page when matchType=TraderIdActiveNETP and mode=RejoinMode" in {

        val taxReferenceNumber: String = "333333333"

        when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
        when(mockRejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService),
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[RejoinRegistrationService].toInstance(mockRejoinRegistrationService)
          ).build()

        running(application) {

          val expectedResponse = genericMatch.copy(matchType = MatchType.TraderIdActiveNETP)

          when(mockCoreRegistrationValidationService.searchEuTaxId(eqTo(taxReferenceNumber), eqTo(country.code))(any(), any())) thenReturn
            Future.successful(Option(expectedResponse))

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute(RejoinMode))
            .withFormUrlEncodedBody(("value", taxReferenceNumber))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.rejoin.routes.RejoinAlreadyRegisteredOtherCountryController.onPageLoad(genericMatch.memberState).url
          }
      }

      "must redirect to RejoinAlreadyRegisteredOtherCountryController page when matchType=OtherMSNETPActiveNETP and mode=RejoinMode" in {

        val taxReferenceNumber: String = "333333333"

        when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
        when(mockRejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService),
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[RejoinRegistrationService].toInstance(mockRejoinRegistrationService)
          ).build()

        running(application) {

          val expectedResponse = genericMatch.copy(matchType = MatchType.OtherMSNETPActiveNETP)

          when(mockCoreRegistrationValidationService.searchEuTaxId(eqTo(taxReferenceNumber), eqTo(country.code))(any(), any())) thenReturn
            Future.successful(Option(expectedResponse))

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute(RejoinMode))
            .withFormUrlEncodedBody(("value", taxReferenceNumber))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.rejoin.routes.RejoinAlreadyRegisteredOtherCountryController.onPageLoad(genericMatch.memberState).url
        }
      }


      "must redirect to CannotRejoinQuarantinedCountryController page when the vat number is excluded for match FixedEstablishmentQuarantinedNETP and mode=RejoinMode" in {

        val taxReferenceNumber: String = "333333333"

        when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
        when(mockRejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService),
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[RejoinRegistrationService].toInstance(mockRejoinRegistrationService)
          ).build()

        running(application) {

          val expectedResponse = genericMatch.copy(matchType = MatchType.FixedEstablishmentQuarantinedNETP)

          when(mockCoreRegistrationValidationService.searchEuTaxId(eqTo(taxReferenceNumber), eqTo(country.code))(any(), any())) thenReturn
            Future.successful(Option(expectedResponse))

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute(RejoinMode))
            .withFormUrlEncodedBody(("value", taxReferenceNumber))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rejoin.routes.CannotRejoinQuarantinedCountryController.onPageLoad(
            expectedResponse.memberState, expectedResponse.exclusionEffectiveDate.mkString).url
        }
      }

      "must redirect to CannotRejoinQuarantinedCountryController page when the vat number is excluded for match TraderIdQuarantinedNETP and mode=RejoinMode" in {

        val taxReferenceNumber: String = "333333333"

        when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
        when(mockRejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService),
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[RejoinRegistrationService].toInstance(mockRejoinRegistrationService)
          ).build()

        running(application) {

          val expectedResponse = genericMatch.copy(matchType = MatchType.TraderIdQuarantinedNETP)

          when(mockCoreRegistrationValidationService.searchEuTaxId(eqTo(taxReferenceNumber), eqTo(country.code))(any(), any())) thenReturn
            Future.successful(Option(expectedResponse))

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute(RejoinMode))
            .withFormUrlEncodedBody(("value", taxReferenceNumber))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rejoin.routes.CannotRejoinQuarantinedCountryController.onPageLoad(
            expectedResponse.memberState, expectedResponse.exclusionEffectiveDate.mkString).url
        }
      }

      "must redirect to CannotRejoinQuarantinedCountryController page when the vat number is excluded for match OtherMSNETPQuarantinedNETP and mode=RejoinMode" in {

        val taxReferenceNumber: String = "333333333"

        when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
        when(mockRejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
          .configure(
            "features.other-country-reg-validation-enabled" -> true
          )
          .overrides(
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService),
            bind[RegistrationConnector].toInstance(mockRegistrationConnector),
            bind[RejoinRegistrationService].toInstance(mockRejoinRegistrationService)
          ).build()

        running(application) {

          val expectedResponse = genericMatch.copy(matchType = MatchType.OtherMSNETPQuarantinedNETP)

          when(mockCoreRegistrationValidationService.searchEuTaxId(eqTo(taxReferenceNumber), eqTo(country.code))(any(), any())) thenReturn
            Future.successful(Option(expectedResponse))

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute(RejoinMode))
            .withFormUrlEncodedBody(("value", taxReferenceNumber))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual controllers.rejoin.routes.CannotRejoinQuarantinedCountryController.onPageLoad(
            expectedResponse.memberState, expectedResponse.exclusionEffectiveDate.mkString).url
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

          val request = FakeRequest(POST, euTaxReferenceSubmitRoute())
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
