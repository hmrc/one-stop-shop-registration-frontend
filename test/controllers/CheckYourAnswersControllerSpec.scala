/*
 * Copyright 2021 HM Revenue & Customs
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
import connectors.RegistrationConnector
import models.{BusinessContactDetails, NormalMode}
import models.emails.EmailSendingResult.EMAIL_ACCEPTED
import models.responses.ConflictFound
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.{Mockito}
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.{BusinessContactDetailsPage, CheckYourAnswersPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{running, _}
import services.{EmailService, RegistrationService}
import testutils.RegistrationData
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency with BeforeAndAfterEach {

  private val registration = RegistrationData.registration

  private val registrationService = mock[RegistrationService]
  private val registrationConnector = mock[RegistrationConnector]
  private val emailService = mock[EmailService]

  override def beforeEach(): Unit = {
    Mockito.reset(registrationConnector)
    Mockito.reset(registrationService)
    Mockito.reset(emailService)
  }

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
      }
    }

    "on submit" - {

      "when the user has answered all necessary data and submission of the registration succeeds" - {

        "user should be redirected to the next page and successfully send email confirmation" in {

          when(registrationService.fromUserAnswers(any(), any())) thenReturn Some(registration)
          when(registrationConnector.submitRegistration(any())(any())) thenReturn Future.successful(Right())
          when(emailService.sendConfirmationEmail(
            eqTo(vrn.toString()),
            eqTo(registration.contactDetails.emailAddress)
          )(any())) thenReturn Future.successful(EMAIL_ACCEPTED)

          val contactDetails = BusinessContactDetails("name", "0111 2223334", "email@example.com")
          val userAnswers = emptyUserAnswersWithVatInfo.set(BusinessContactDetailsPage, contactDetails).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[RegistrationService].toInstance(registrationService),
              bind[RegistrationConnector].toInstance(registrationConnector),
              bind[EmailService].toInstance(emailService)
            ).build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual CheckYourAnswersPage.navigate(NormalMode, emptyUserAnswers).url

            verify(emailService, times(1))
              .sendConfirmationEmail(any(), any())(any())
          }
        }
      }

      "when the user has not answered all necessary data" - {

        "the user is redirected to Journey Recovery Page" in {

          when(registrationService.fromUserAnswers(any(), any())) thenReturn None

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[RegistrationService].toInstance(registrationService)).build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onPageLoad().url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }

      "when the submission fails because the user has already registered" - {

        "the user is redirected to Already Registered Page" in {

          when(registrationService.fromUserAnswers(any(), any())) thenReturn Some(registration)
          when(registrationConnector.submitRegistration(any())(any())) thenReturn Future.successful(Left(ConflictFound))

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[RegistrationService]
              .toInstance(registrationService),bind[RegistrationConnector]
              .toInstance(registrationConnector)
            ).build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onPageLoad().url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.AlreadyRegisteredController.onPageLoad().url
          }
        }
      }
    }
  }
}
