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
import controllers.amend.routes as amendRoutes
import controllers.previousRegistrations.routes as prevRoutes
import controllers.routes
import forms.previousRegistrations.DeletePreviousRegistrationFormProvider
import models.domain.{PreviousSchemeDetails, PreviousSchemeNumbers}
import models.previousRegistrations.PreviousRegistrationDetails
import models.{AmendMode, Country, Index, NormalMode, PreviousScheme}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.previousRegistrations.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.previousRegistration.PreviousRegistrationQuery
import repositories.AuthenticatedUserAnswersRepository
import testutils.RegistrationData
import utils.FutureSyntax.FutureOps
import views.html.previousRegistrations.DeletePreviousRegistrationView

class DeletePreviousRegistrationControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new DeletePreviousRegistrationFormProvider()
  private val form = formProvider()

  private val index = Index(0)
  private val index1 = Index(1)
  private val country = Country.euCountries.head
  private val previousSchemeNumbers = PreviousSchemeNumbers("VAT Number", None)
  private val previousScheme = PreviousSchemeDetails(PreviousScheme.OSSU, previousSchemeNumbers)
  private val previousRegistration = PreviousRegistrationDetails(country, List(previousScheme))

  private lazy val deletePreviousRegistrationRoute = prevRoutes.DeletePreviousRegistrationController.onPageLoad(NormalMode, index).url
  private lazy val deletePreviousRegistrationAmendRoute = prevRoutes.DeletePreviousRegistrationController.onPageLoad(AmendMode, index).url

  private val baseUserAnswers =
    basicUserAnswersWithVatInfo
      .set(PreviousEuCountryPage(index), previousRegistration.previousEuCountry).success.value
      .set(PreviousSchemePage(index, index), PreviousScheme.OSSU).success.value
      .set(PreviousOssNumberPage(index, index), previousSchemeNumbers).success.value

  "DeletePreviousRegistration Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, deletePreviousRegistrationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeletePreviousRegistrationView]

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form, NormalMode, index, previousRegistration.previousEuCountry.name)(request, messages(application)).toString
      }
    }

    "must delete a record and redirect to the next page when the user answers Yes" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn true.toFuture

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, deletePreviousRegistrationRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val expectedAnswers =
          baseUserAnswers
            .remove(PreviousRegistrationQuery(index)).success.value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` DeletePreviousRegistrationPage(index).navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must not delete a record and redirect to the next page when the user answers No" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn true.toFuture

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, deletePreviousRegistrationRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` DeletePreviousRegistrationPage(index).navigate(NormalMode, baseUserAnswers).url
        verify(mockSessionRepository, never()).set(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, deletePreviousRegistrationRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[DeletePreviousRegistrationView]

        val result = route(application, request).value

        status(result) `mustBe` BAD_REQUEST
        contentAsString(result) `mustBe` view(boundForm, NormalMode, index, previousRegistration.previousEuCountry.name)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, deletePreviousRegistrationRoute)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no EU VAT details exist" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request = FakeRequest(GET, deletePreviousRegistrationRoute)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, deletePreviousRegistrationRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "in AmendMode" - {

      "must not delete an existing previous registration and redirect to Cannot Remove Existing Previous Registrations when the user answers Yes" in {

        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

        when(mockSessionRepository.set(any())) thenReturn true.toFuture

        val application =
          applicationBuilder(userAnswers =
            Some(basicUserAnswersWithVatInfo
              .set(PreviousEuCountryPage(index), Country("DE", "Germany")).success.value
              .set(PreviousSchemePage(index, index), PreviousScheme.OSSU).success.value
              .set(PreviousOssNumberPage(index, index), previousSchemeNumbers).success.value
              .set(PreviousEuCountryPage(index1), Country("FR", "France")).success.value
              .set(PreviousSchemePage(index1, index), PreviousScheme.OSSNU).success.value
              .set(PreviousOssNumberPage(index1, index), previousSchemeNumbers).success.value
            ),
            mode = Some(AmendMode),
            registration = Some(RegistrationData.registration)
          )
            .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, deletePreviousRegistrationAmendRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` prevRoutes.CannotRemoveExistingPreviousRegistrationsController.onPageLoad().url
          verify(mockSessionRepository, never()).set(any())
        }
      }

      "must delete a new previous registration and redirect to the next page when the user answers Yes" in {

        lazy val deleteAmendPreviousRegistrationAmendRoute = prevRoutes.DeletePreviousRegistrationController.onPageLoad(AmendMode, index1).url

        val answers = basicUserAnswersWithVatInfo
          .set(PreviousEuCountryPage(index), Country("DE", "Germany")).success.value
          .set(PreviousSchemePage(index, index), PreviousScheme.OSSU).success.value
          .set(PreviousOssNumberPage(index, index), previousSchemeNumbers).success.value
          .set(PreviousEuCountryPage(index1), Country("FR", "France")).success.value
          .set(PreviousSchemePage(index1, index), PreviousScheme.OSSNU).success.value
          .set(PreviousOssNumberPage(index1, index), previousSchemeNumbers).success.value

        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

        when(mockSessionRepository.set(any())) thenReturn true.toFuture

        val application =
          applicationBuilder(userAnswers = Some(answers), mode = Some(AmendMode), registration = Some(RegistrationData.registration))
            .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, deleteAmendPreviousRegistrationAmendRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val expectedAnswers = answers
            .remove(PreviousRegistrationQuery(index1)).success.value

          val result = route(application, request).value

          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` DeletePreviousRegistrationPage(index1).navigate(AmendMode, answers).url
          verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
        }
      }

      "must redirect to Amend Journey Recovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, deletePreviousRegistrationAmendRoute)

          val result = route(application, request).value

          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` amendRoutes.AmendJourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Amend Journey Recovery for a GET if no EU VAT details exist" in {

        val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
          .build()

        running(application) {
          val request = FakeRequest(GET, deletePreviousRegistrationAmendRoute)

          val result = route(application, request).value

          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` amendRoutes.AmendJourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Amend Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, deletePreviousRegistrationAmendRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` amendRoutes.AmendJourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
