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

package controllers.actions

import base.SpecBase
import models.UserAnswers
import models.requests.{AuthenticatedIdentifierRequest, AuthenticatedOptionalDataRequest, SessionRequest, UnauthenticatedOptionalDataRequest}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import repositories.{AuthenticatedSessionRepository, UnauthenticatedSessionRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with MockitoSugar {

  class AuthenticatedHarness(sessionRepository: AuthenticatedSessionRepository) extends AuthenticatedDataRetrievalAction(sessionRepository) {
    def callTransform[A](request: AuthenticatedIdentifierRequest[A]): Future[AuthenticatedOptionalDataRequest[A]] = transform(request)
  }

  class UnauthenticatedHarness(sessionRepository: UnauthenticatedSessionRepository) extends UnauthenticatedDataRetrievalAction(sessionRepository) {
    def callTransform[A](request: SessionRequest[A]): Future[UnauthenticatedOptionalDataRequest[A]] = transform(request)
  }

  "Authenticated Data Retrieval Action" - {

    "when there is no data in the cache" - {

      "must set userAnswers to 'None' in the request" in {

        val sessionRepository = mock[AuthenticatedSessionRepository]
        when(sessionRepository.get("12345-credId")) thenReturn Future(None)
        val action = new AuthenticatedHarness(sessionRepository)

        val result = action.callTransform(AuthenticatedIdentifierRequest(FakeRequest(), testCredentials, vrn)).futureValue

        result.userAnswers must not be defined
      }
    }

    "when there is data in the cache" - {

      "must build a userAnswers object and add it to the request" in {

        val sessionRepository = mock[AuthenticatedSessionRepository]
        when(sessionRepository.get("12345-credId")) thenReturn Future(Some(UserAnswers("12345-credId")))
        val action = new AuthenticatedHarness(sessionRepository)

        val result = action.callTransform(new AuthenticatedIdentifierRequest(FakeRequest(), testCredentials, vrn)).futureValue

        result.userAnswers mustBe defined
      }
    }
  }

  "Unauthenticated Data Retrieval Action" - {

    "when there is no data in the cache" - {

      "must set userAnswers to 'None' in the request" in {

        val sessionRepository = mock[UnauthenticatedSessionRepository]
        when(sessionRepository.get("12345")) thenReturn Future(None)
        val action = new UnauthenticatedHarness(sessionRepository)

        val result = action.callTransform(SessionRequest(FakeRequest(), "12345")).futureValue

        result.userAnswers must not be defined
      }
    }

    "when there is data in the cache" - {

      "must build a userAnswers object and add it to the request" in {

        val sessionRepository = mock[UnauthenticatedSessionRepository]
        when(sessionRepository.get("12345")) thenReturn Future(Some(UserAnswers("12345")))
        val action = new UnauthenticatedHarness(sessionRepository)

        val result = action.callTransform(SessionRequest(FakeRequest(), "12345")).futureValue

        result.userAnswers mustBe defined
      }
    }
  }
}
