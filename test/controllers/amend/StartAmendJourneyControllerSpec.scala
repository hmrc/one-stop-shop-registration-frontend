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
import connectors.RegistrationConnector
import controllers.amend.routes as amendRoutes
import models.domain.Registration
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{running, *}
import repositories.AuthenticatedUserAnswersRepository
import services.*
import testutils.RegistrationData
import utils.FutureSyntax.FutureOps
import viewmodels.govuk.SummaryListFluency

class StartAmendJourneyControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency with BeforeAndAfterEach {

  private val mockRegistrationConnector = mock[RegistrationConnector]
  private val mockRegistrationService = mock[RegistrationService]
  private val mockAuthenticatedUserAnswersRepository = mock[AuthenticatedUserAnswersRepository]

  private val registration: Registration = RegistrationData.registration

  override def beforeEach(): Unit = {
    Mockito.reset(mockRegistrationConnector)
    Mockito.reset(mockRegistrationService)
    Mockito.reset(mockAuthenticatedUserAnswersRepository)
  }

  "Start Amend Controller" - {

    "GET" - {

      "must set user answers from registration and redirect to change your registration" in {

        when(mockRegistrationConnector.getVatCustomerInfo()(any())) thenReturn Right(vatCustomerInfo).toFuture
        when(mockRegistrationService.toUserAnswers(any(), any(), any())) thenReturn completeUserAnswers.toFuture
        when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture

        val application = applicationBuilder(userAnswers = Some(completeUserAnswers), registration = Some(registration))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .overrides(bind[RegistrationService].toInstance(mockRegistrationService))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockAuthenticatedUserAnswersRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, amendRoutes.StartAmendJourneyController.onPageLoad().url)
          val result = route(application, request).value

          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` amendRoutes.ChangeYourRegistrationController.onPageLoad().url
        }
      }

      "must redirect to Not Registered Page when no registration found" in {

        when(mockRegistrationConnector.getVatCustomerInfo()(any())) thenReturn Right(vatCustomerInfo).toFuture
        when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockAuthenticatedUserAnswersRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, amendRoutes.StartAmendJourneyController.onPageLoad().url)
          val result = route(application, request).value

          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` controllers.routes.NotRegisteredController.onPageLoad().url
        }
      }
    }
  }
}
