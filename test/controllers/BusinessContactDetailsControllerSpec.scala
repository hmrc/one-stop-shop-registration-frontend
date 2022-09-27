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

package controllers

import base.SpecBase
import connectors.ValidateEmailConnector
import forms.BusinessContactDetailsFormProvider
import models.responses.UnexpectedResponseStatus
import models.{BusinessContactDetails, NormalMode, ValidateEmailRequest, ValidateEmailResponse, VerifyEmail}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, verifyNoInteractions, when}
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import pages.BusinessContactDetailsPage
import play.api.http.Status.{BAD_GATEWAY, BAD_REQUEST, INTERNAL_SERVER_ERROR, UNAUTHORIZED}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import views.html.BusinessContactDetailsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessContactDetailsControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new BusinessContactDetailsFormProvider()
  private val form = formProvider()

  private lazy val businessContactDetailsRoute = routes.BusinessContactDetailsController.onPageLoad(NormalMode).url

  private val contactDetails = BusinessContactDetails("name", "0111 2223334", "email@example.com")
  private val userAnswers = basicUserAnswers.set(BusinessContactDetailsPage, contactDetails).success.value

  private val mockValidateEmailConnector = mock[ValidateEmailConnector]

  private implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  private val verifyEmail: VerifyEmail = VerifyEmail(
    address = contactDetails.emailAddress,
    enterUrl = "/pay-vat-on-goods-sold-to-eu/northern-ireland-register/business-contact-details"
  )

  private val validateEmailRequest: ValidateEmailRequest = ValidateEmailRequest(
    credId = userAnswersId,
    continueUrl = "/pay-vat-on-goods-sold-to-eu/northern-ireland-register/bank-details",
    origin = "OSS",
    deskproServiceName = Some("one-stop-shop-registration-frontend"),
    accessibilityStatementUrl = "/register-and-pay-vat-on-goods-sold-to-eu-from-northern-ireland",
    pageTitle = Some("Register to pay VAT on distance sales of goods from Northern Ireland to the EU"),
    backUrl = Some("/pay-vat-on-goods-sold-to-eu/northern-ireland-register/business-contact-details"),
    email = Some(verifyEmail)
  )

  private val validateEmailResponse: ValidateEmailResponse = ValidateEmailResponse(
    redirectUrl = routes.BankDetailsController.onPageLoad(NormalMode).url
  )

  "BusinessContactDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, businessContactDetailsRoute)

        val view = application.injector.instanceOf[BusinessContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, enrolmentsEnabled = false)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, businessContactDetailsRoute)

        val view = application.injector.instanceOf[BusinessContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(contactDetails), NormalMode, enrolmentsEnabled = false)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockValidateEmailConnector.validateEmail(eqTo(validateEmailRequest))(any(), any())) thenReturn Future.successful(Right(validateEmailResponse))

      val application =
        applicationBuilder(userAnswers = Some(basicUserAnswers))
          .overrides(
            bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository),
            bind[ValidateEmailConnector].toInstance(mockValidateEmailConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, businessContactDetailsRoute)
            .withFormUrlEncodedBody(("fullName", "name"), ("telephoneNumber", "0111 2223334"), ("emailAddress", "email@example.com"))

        val result = route(application, request).value
        val expectedAnswers = basicUserAnswers.set(BusinessContactDetailsPage, contactDetails).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual validateEmailResponse.redirectUrl
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
//        verify(mockValidateEmailConnector.validateEmail(eqTo(validateEmailRequest))(any(), any()), times(1))
      }
    }

    "must not save the answer and redirect to the current page when invalid email request is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      val httpStatus = Gen.oneOf(BAD_REQUEST, UNAUTHORIZED, INTERNAL_SERVER_ERROR, BAD_GATEWAY).sample.value

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockValidateEmailConnector.validateEmail(any())(any(), any())) thenReturn
        Future.successful(Left(UnexpectedResponseStatus(httpStatus, "error")))

      val application =
        applicationBuilder(userAnswers = Some(basicUserAnswers))
          .overrides(
            bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository),
            bind[ValidateEmailConnector].toInstance(mockValidateEmailConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, businessContactDetailsRoute)
            .withFormUrlEncodedBody(("fullName", "name"), ("telephoneNumber", "0111 2223334"), ("emailAddress", "email@example.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.BusinessContactDetailsController.onPageLoad(NormalMode).url
        verifyNoInteractions(mockSessionRepository)
//        verify(mockValidateEmailConnector, times(1)).validateEmail(eqTo(validateEmailRequest)).futureValue
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, businessContactDetailsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[BusinessContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, enrolmentsEnabled = false)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, businessContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, businessContactDetailsRoute)
            .withFormUrlEncodedBody(("fullName", "value 1"), ("telephoneNumber", "0111 2223334"), ("emailAddress", "email@email.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
