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

package controllers.amend

import base.SpecBase
import cats.data.Validated.Valid
import connectors.RegistrationConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.AuthenticatedUserAnswersRepository
import services.{RegistrationService, RegistrationValidationService}
import testutils.RegistrationData
import views.html.amend.NoLongerAmendableView

import scala.concurrent.Future

class NoLongerAmendableControllerSpec extends SpecBase with MockitoSugar {

  private val registration = RegistrationData.registration

  private val mockRegistrationConnector = mock[RegistrationConnector]
  private val mockRegistrationService = mock[RegistrationService]
  private val mockRegistrationValidationService = mock[RegistrationValidationService]
  private val mockAuthenticatedUserAnswersRepository = mock[AuthenticatedUserAnswersRepository]

  "NoLongerAmendable Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
      when(mockRegistrationConnector.getVatCustomerInfo()(any())) thenReturn Future.successful(Right(vatCustomerInfo))
      when(mockRegistrationService.toUserAnswers(any(), any(), any())) thenReturn Future.successful(basicUserAnswersWithVatInfo)
      when(mockRegistrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn Future.successful(Valid(registration))
      when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .overrides(bind[RegistrationService].toInstance(mockRegistrationService))
          .overrides(bind[RegistrationValidationService].toInstance(mockRegistrationValidationService))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockAuthenticatedUserAnswersRepository))
        .build()

      running(application) {

        val request = FakeRequest(GET, routes.NoLongerAmendableController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NoLongerAmendableView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
