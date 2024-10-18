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

package controllers.auth

import base.SpecBase
import config.FrontendAppConfig
import connectors.{RegistrationConnector, SaveForLaterConnector, SavedUserAnswers}
import models.{UserAnswers, VatApiCallResult, responses}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, reset, times, verify, verifyNoInteractions, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.SavedProgressPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.VatApiCallResultQuery
import repositories.AuthenticatedUserAnswersRepository
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import views.html.auth.{InsufficientEnrolmentsView, UnsupportedAffinityGroupView, UnsupportedAuthProviderView, UnsupportedCredentialRoleView}

import java.net.URLEncoder
import java.time.{Instant, LocalDate}
import scala.concurrent.Future

class AuthControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val continueUrl = "http://localhost/foo"

  private val mockConnector = mock[RegistrationConnector]
  private val mockRepository = mock[AuthenticatedUserAnswersRepository]
  private val mockSavedAnswersConnector = mock[SaveForLaterConnector]

  private def appBuilder(answers: Option[UserAnswers]) =
    applicationBuilder(answers)
      .overrides(
        bind[RegistrationConnector].toInstance(mockConnector),
        bind[AuthenticatedUserAnswersRepository].toInstance(mockRepository),
        bind[SaveForLaterConnector].toInstance(mockSavedAnswersConnector)
      )

  override def beforeEach(): Unit = {
    reset(mockConnector)
    reset(mockRepository)
    reset(mockSavedAnswersConnector)
  }


  "onSignIn" - {

    "when we already have some user answers" - {

      "must redirect to the ContinueRegistration page if saved url was retrieved from saved answers" in {

        val answers = emptyUserAnswers.set(VatApiCallResultQuery, VatApiCallResult.Success).success.value
          .set(SavedProgressPage, "/url").success.value
        val application = appBuilder(Some(answers)).build()
        when(mockSavedAnswersConnector.get()(any())) thenReturn
          Future.successful(Right(Some(SavedUserAnswers(vrn, answers.data, Instant.now))))

        running(application) {
          val request = FakeRequest(GET, routes.AuthController.onSignIn().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ContinueRegistrationController.onPageLoad().url
          verify(mockConnector, never()).getVatCustomerInfo()(any())
          verify(mockRepository, never()).set(any())
        }
      }

      "and we have made a call to get VAT info" - {

        "must redirect to the next page without makings calls to get data or updating the user answers" in {

          val answers = emptyUserAnswers.set(VatApiCallResultQuery, VatApiCallResult.Success).success.value
          val application = appBuilder(Some(answers)).build()
          when(mockSavedAnswersConnector.get()(any())) thenReturn Future.successful(Right(None))
          when(mockConnector.getVatCustomerInfo()(any())) thenReturn Future.successful(Right(vatCustomerInfo))
          running(application) {
            val request = FakeRequest(GET, routes.AuthController.onSignIn().url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.CheckVatDetailsController.onPageLoad().url
            verify(mockRepository, never()).set(any())
          }
        }
      }

      "and we have not yet made a call to get VAT info" - {

        "and we can find their VAT details" - {

          "and the de-registration date is today or before" - {

            "must redirect to Invalid VRN date kick-out page" in {

              val application = appBuilder(None).build()
              val validVrnVatInfo = vatCustomerInfo.copy(
                deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate))
              )

              when(mockConnector.getVatCustomerInfo()(any())) thenReturn Future.successful(Right(validVrnVatInfo))
              when(mockRepository.set(any())) thenReturn Future.successful(true)
              when(mockSavedAnswersConnector.get()(any())) thenReturn Future.successful(Right(None))

              running(application) {
                val request = FakeRequest(GET, routes.AuthController.onSignIn().url)
                val result = route(application, request).value

                status(result) mustBe SEE_OTHER
                redirectLocation(result).value mustEqual controllers.routes.InvalidVrnDateController.onPageLoad().url
                verifyNoInteractions(mockRepository)

              }


            }
          }

          "and the de-registration date is later than today" - {

            "must create user answers with their VAT details, then redirect to the next page" in {

              val application = appBuilder(Some(emptyUserAnswers)).build()
              val validVrnVatInfo = vatCustomerInfo.copy(
                deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate).plusDays(1))
              )

              when(mockConnector.getVatCustomerInfo()(any())) thenReturn Future.successful(Right(validVrnVatInfo))
              when(mockRepository.set(any())) thenReturn Future.successful(true)
              when(mockSavedAnswersConnector.get()(any())) thenReturn Future.successful(Right(None))

              running(application) {

                val request = FakeRequest(GET, routes.AuthController.onSignIn().url)
                val result = route(application, request).value

                val expectedAnswers = emptyUserAnswersWithVatInfo
                  .copy(vatInfo = Some(validVrnVatInfo))
                  .set(VatApiCallResultQuery, VatApiCallResult.Success).success.value

                status(result) mustEqual SEE_OTHER
                redirectLocation(result).value mustEqual controllers.routes.CheckVatDetailsController.onPageLoad().url
                verify(mockRepository, times(1)).set(eqTo(expectedAnswers))
              }
            }
          }
        }

        "and we cannot find their VAT details" - {

          "must redirect to Vat Api Down page" in {

            val application = appBuilder(Some(emptyUserAnswers)).build()

            when(mockConnector.getVatCustomerInfo()(any())) thenReturn Future.successful(Left(responses.NotFound))
            when(mockRepository.set(any())) thenReturn Future.successful(true)
            when(mockSavedAnswersConnector.get()(any())) thenReturn Future.successful(Right(None))

            running(application) {

              val request = FakeRequest(GET, routes.AuthController.onSignIn().url)
              val result = route(application, request).value

              val expectedAnswers = emptyUserAnswers.set(VatApiCallResultQuery, VatApiCallResult.Error).success.value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.VatApiDownController.onPageLoad().url
              verify(mockRepository, times(1)).set(eqTo(expectedAnswers))
            }
          }
        }

        "and the call to get their VAT details fails" - {

          val failureResponse = responses.UnexpectedResponseStatus(INTERNAL_SERVER_ERROR, "foo")


          "must return an internal server error" in {

            val application =
              appBuilder(None)
                .build()

            when(mockConnector.getVatCustomerInfo()(any())) thenReturn Future.successful(Left(failureResponse))
            when(mockRepository.set(any())) thenReturn Future.successful(true)
            when(mockSavedAnswersConnector.get()(any())) thenReturn Future.successful(Right(None))

            running(application) {

              val request = FakeRequest(GET, routes.AuthController.onSignIn().url)
              val result = route(application, request).value

              val expectedAnswers = emptyUserAnswers.set(VatApiCallResultQuery, VatApiCallResult.Error).success.value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.VatApiDownController.onPageLoad().url
              verify(mockRepository, times(1)).set(eqTo(expectedAnswers))
            }
          }

        }
      }
    }

    "when we don't already have user answers" - {

      "and we can find their VAT details" - {

        "and the de-registration date is today or before" - {

          "must redirect to the Invalid VRN date page" in {
            val application = appBuilder(None).build()
            val invalidVrnVatInfo = vatCustomerInfo.copy(
              deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate))
            )

            when(mockConnector.getVatCustomerInfo()(any())) thenReturn Future.successful(Right(invalidVrnVatInfo))
            when(mockRepository.set(any())) thenReturn Future.successful(true)
            when(mockSavedAnswersConnector.get()(any())) thenReturn Future.successful(Right(None))

            running(application) {

              val request = FakeRequest(GET, routes.AuthController.onSignIn().url)
              val result = route(application, request).value

              status(result) mustBe SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.InvalidVrnDateController.onPageLoad().url
              verifyNoInteractions(mockRepository)
            }
          }
        }

        " and the de-registration date is later than today" - {

          "must create user answers with their VAT details, then redirect to the next page" in {

            val application = appBuilder(None).build()
            val validVrnVatInfo = vatCustomerInfo.copy(
              deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate).plusDays(1))
            )

            when(mockConnector.getVatCustomerInfo()(any())) thenReturn Future.successful(Right(validVrnVatInfo))
            when(mockRepository.set(any())) thenReturn Future.successful(true)
            when(mockSavedAnswersConnector.get()(any())) thenReturn Future.successful(Right(None))

            running(application) {

              val request = FakeRequest(GET, routes.AuthController.onSignIn().url)
              val result = route(application, request).value

              val expectedAnswers = emptyUserAnswersWithVatInfo
                .copy(vatInfo = Some(validVrnVatInfo))
                .set(VatApiCallResultQuery, VatApiCallResult.Success).success.value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.CheckVatDetailsController.onPageLoad().url
              verify(mockRepository, times(1)).set(eqTo(expectedAnswers))
            }
          }
        }

      }

      "and we cannot find their VAT details" - {

        "must redirect to Vat Api Down page" in {

          val application = appBuilder(None).build()

          when(mockConnector.getVatCustomerInfo()(any())) thenReturn Future.successful(Left(responses.NotFound))
          when(mockRepository.set(any())) thenReturn Future.successful(true)
          when(mockSavedAnswersConnector.get()(any())) thenReturn Future.successful(Right(None))

          running(application) {

            val request = FakeRequest(GET, routes.AuthController.onSignIn().url)
            val result = route(application, request).value

            val expectedAnswers = emptyUserAnswers.set(VatApiCallResultQuery, VatApiCallResult.Error).success.value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.VatApiDownController.onPageLoad().url
            verify(mockRepository, times(1)).set(eqTo(expectedAnswers))
          }
        }
      }

      "and the call to get their VAT details fails" - {

        val failureResponse = responses.UnexpectedResponseStatus(INTERNAL_SERVER_ERROR, "foo")

        "must return an internal server error" in {

          val application =
            appBuilder(None)
              .build()

          when(mockConnector.getVatCustomerInfo()(any())) thenReturn Future.successful(Left(failureResponse))
          when(mockRepository.set(any())) thenReturn Future.successful(true)
          when(mockSavedAnswersConnector.get()(any())) thenReturn Future.successful(Right(None))

          running(application) {

            val request = FakeRequest(GET, routes.AuthController.onSignIn().url)
            val result = route(application, request).value

            val expectedAnswers = emptyUserAnswers.set(VatApiCallResultQuery, VatApiCallResult.Error).success.value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.VatApiDownController.onPageLoad().url
            verify(mockRepository, times(1)).set(eqTo(expectedAnswers))
          }
        }

      }
    }
  }

  "continueOnSignIn" - {

    "must redirect to the ContinueRegistration page if saved url was retrieved from saved answers" in {

      val answers = emptyUserAnswers.set(VatApiCallResultQuery, VatApiCallResult.Success).success.value
        .set(SavedProgressPage, "/url").success.value
      val application = appBuilder(Some(answers)).build()
      when(mockSavedAnswersConnector.get()(any())) thenReturn
        Future.successful(Right(Some(SavedUserAnswers(vrn, answers.data, Instant.now))))

      running(application) {
        val request = FakeRequest(GET, routes.AuthController.continueOnSignIn().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ContinueRegistrationController.onPageLoad().url
      }
    }

    "must redirect to NoRegistrationInProgress when there is no saved answers" in {
      val application = appBuilder(None).build()
      when(mockSavedAnswersConnector.get()(any())) thenReturn
        Future.successful(Right(None))
      when(mockConnector.getVatCustomerInfo()(any())) thenReturn Future.successful(Right(vatCustomerInfo))

      running(application) {
        val request = FakeRequest(GET, routes.AuthController.continueOnSignIn().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.NoRegistrationInProgressController.onPageLoad().url
      }
    }
  }

  "signOut" - {

    "must redirect to sign out, specifying the exit survey as the continue URL" in {

      val application = applicationBuilder(None).build()

      running(application) {

        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request = FakeRequest(GET, routes.AuthController.signOut().url)

        val result = route(application, request).value

        val encodedContinueUrl = URLEncoder.encode(appConfig.exitSurveyUrl, "UTF-8")
        val expectedRedirectUrl = s"${appConfig.signOutUrl}?continue=$encodedContinueUrl"

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedRedirectUrl
      }
    }
  }

  "signOutNoSurvey" - {

    "must redirect to sign out, specifying SignedOut as the continue URL" in {

      val application = applicationBuilder(None).build()

      running(application) {

        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request = FakeRequest(GET, routes.AuthController.signOutNoSurvey().url)

        val result = route(application, request).value

        val encodedContinueUrl = URLEncoder.encode(routes.SignedOutController.onPageLoad().url, "UTF-8")
        val expectedRedirectUrl = s"${appConfig.signOutUrl}?continue=$encodedContinueUrl"

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedRedirectUrl
      }
    }
  }

  "redirectToRegister" - {

    "must redirect the user to bas-gateway to register" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AuthController.redirectToRegister(RedirectUrl("http://localhost/foo")).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9553/bas-gateway/register?origin=OSS&continueUrl=http%3A%2F%2Flocalhost%2Ffoo&accountType=Organisation"
      }
    }
  }

  "redirectToLogin" - {

    "must redirect the user to bas-gateway to log in" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AuthController.redirectToLogin(RedirectUrl("http://localhost/foo")).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9553/bas-gateway/sign-in?origin=OSS&continue=http%3A%2F%2Flocalhost%2Ffoo"
      }
    }
  }

  "unsupportedAuthProvider" - {

    "must return OK and the correct view" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AuthController.unsupportedAuthProvider(RedirectUrl("http://localhost/foo")).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnsupportedAuthProviderView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(RedirectUrl(continueUrl))(request, messages(application)).toString
      }
    }
  }

  "unsupportedAffinityGroup" - {

    "must return OK and the correct view" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AuthController.unsupportedAffinityGroup().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnsupportedAffinityGroupView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view()(request, messages(application)).toString
      }
    }
  }

  "unsupportedCredentialRole" - {

    "must return OK and the correct view" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AuthController.unsupportedCredentialRole().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnsupportedCredentialRoleView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view()(request, messages(application)).toString
      }
    }
  }

  "insufficientEnrolments" - {

    "must return OK and the correct view" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AuthController.insufficientEnrolments().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[InsufficientEnrolmentsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view()(request, messages(application)).toString
      }
    }
  }
}
