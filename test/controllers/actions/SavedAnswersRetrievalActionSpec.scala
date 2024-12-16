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

package controllers.actions

import base.SpecBase
import connectors.{RegistrationConnector, SavedUserAnswers, SaveForLaterConnector}
import models.UserAnswers
import models.requests.AuthenticatedOptionalDataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.EitherValues
import org.scalatestplus.mockito.MockitoSugar
import pages.SavedProgressPage
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import repositories.AuthenticatedUserAnswersRepository

import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SavedAnswersRetrievalActionSpec extends SpecBase with MockitoSugar with EitherValues {

  class Harness(
                 repository: AuthenticatedUserAnswersRepository,
                 saveForLaterConnector: SaveForLaterConnector,
                 registrationConnector: RegistrationConnector
               ) extends SavedAnswersRetrievalAction(repository, saveForLaterConnector, registrationConnector) {
    def callRefine[A](request: AuthenticatedOptionalDataRequest[A]): Future[Either[Result, AuthenticatedOptionalDataRequest[A]]] = refine(request)
  }


  "Authenticated Data Retrieval Action" - {

    "when there is answers in session with continueUrl set" - {

      "must use the answers saved in session" in {

        val sessionRepository = mock[AuthenticatedUserAnswersRepository]
        val saveForLaterConnector = mock[SaveForLaterConnector]
        val registrationConnector = mock[RegistrationConnector]

        val answers = UserAnswers(userAnswersId).set(SavedProgressPage, "/url").success.value
        val action = new Harness(sessionRepository, saveForLaterConnector, registrationConnector)
        val request = FakeRequest(GET, "/test/url?k=session-id")

        val result = action.callRefine(AuthenticatedOptionalDataRequest(request,
          testCredentials,
          vrn,
          Some(answers))).futureValue

        verifyNoInteractions(saveForLaterConnector)
        verifyNoInteractions(sessionRepository)
        result.value.userAnswers mustBe (Some(answers))
      }
    }

    "when there is no answers in session with continueUrl set" - {
      "must retrieve saved answers when present" in {
        val sessionRepository = mock[AuthenticatedUserAnswersRepository]
        val saveForLaterConnector = mock[SaveForLaterConnector]
        val registrationConnector = mock[RegistrationConnector]
        val instant = Instant.now
        val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)
        val answers = UserAnswers(
          userAnswersId,
          vatInfo = Some(vatCustomerInfo),
          lastUpdated = Instant.now(stubClock)
        ).set(SavedProgressPage, "/url").success.value

        when(saveForLaterConnector.get()(any())) thenReturn Future.successful(Right(Some(SavedUserAnswers(vrn, answers.data, instant))))
        when(registrationConnector.getVatCustomerInfo()(any())) thenReturn Future.successful(Right(vatCustomerInfo))
        when(sessionRepository.set(any())) thenReturn Future.successful(true)
        val action = new Harness(sessionRepository, saveForLaterConnector, registrationConnector)
        val request = FakeRequest(GET, "/test/url?k=session-id")

        val result = action.callRefine(AuthenticatedOptionalDataRequest(
          request,
          testCredentials,
          vrn,
          Some(UserAnswers(userAnswersId))
        )).futureValue

        verify(saveForLaterConnector, times(1)).get()(any())
        result.value.userAnswers mustBe Some(answers)
      }

      "must use answers in request when no saved answers present" in {
        val sessionRepository = mock[AuthenticatedUserAnswersRepository]
        val saveForLaterConnector = mock[SaveForLaterConnector]
        val registrationConnector = mock[RegistrationConnector]
        val emptyAnswers = UserAnswers(userAnswersId)

        when(saveForLaterConnector.get()(any())) thenReturn Future.successful(Right(None))
        when(sessionRepository.set(any())) thenReturn Future.successful(true)
        when(registrationConnector.getVatCustomerInfo()(any())) thenReturn Future.successful(Right(vatCustomerInfo))
        val action = new Harness(sessionRepository, saveForLaterConnector, registrationConnector)
        val request = FakeRequest(GET, "/test/url?k=session-id")

        val result = action.callRefine(AuthenticatedOptionalDataRequest(request,
          testCredentials,
          vrn,
          Some(emptyAnswers))).futureValue

        verify(saveForLaterConnector, times(1)).get()(any())
        result.value.userAnswers mustBe Some(emptyAnswers)
      }
    }

  }

}
