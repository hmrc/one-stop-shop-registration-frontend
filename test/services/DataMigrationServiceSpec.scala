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

package services

import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import repositories.{AuthenticatedUserAnswersRepository, SessionRepository, UnauthenticatedUserAnswersRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataMigrationServiceSpec extends AnyFreeSpec with MockitoSugar with ScalaFutures with Matchers with BeforeAndAfterEach  {
  val authenticatedRepository   = mock[AuthenticatedUserAnswersRepository]
  val unauthenticatedRepository = mock[UnauthenticatedUserAnswersRepository]
  val sessionRepository = mock[SessionRepository]
  override def beforeEach() = {
    reset(authenticatedRepository)
    reset(unauthenticatedRepository)
  }
  ".migrate" - {

    "when there is some data in the unauthenticated repository for this session id" - {

      "must migrate it to the authenticated repository" in {
        val sessionId                 = "session-id"
        val userId                    = "userId"
        val captor                    = ArgumentCaptor.forClass(classOf[UserAnswers])
        val json                      = Json.obj("foo" -> "bar")
        val unauthenticatedData       = UserAnswers(sessionId, json, None)

        when(unauthenticatedRepository.get(any())) thenReturn Future.successful(Some(unauthenticatedData))
        when(authenticatedRepository.set(any())) thenReturn Future.successful(true)
        when(sessionRepository.get(any())) thenReturn Future.successful(Seq.empty)
        when(sessionRepository.set(any())) thenReturn Future.successful(true)

        val service = new DataMigrationService(authenticatedRepository, unauthenticatedRepository, sessionRepository)

        service.migrate(sessionId, userId).futureValue
        verify(authenticatedRepository, times(1)).set(captor.capture())
        captor.getValue must matchPattern { case UserAnswers(`userId`, `json`, None, _) => }
      }
    }

    "when there is no data in the unauthenticated repository for this session id" - {

      "must create an empty user answers in the authenticated repository" in {
        val sessionId                 = "session-id"
        val userId                    = "userId"
        val captor                    = ArgumentCaptor.forClass(classOf[UserAnswers])
        val emptyJson                 = Json.obj()

        when(unauthenticatedRepository.get(any())) thenReturn Future.successful(None)
        when(authenticatedRepository.set(any())) thenReturn Future.successful(true)
        when(sessionRepository.get(any())) thenReturn Future.successful(Seq.empty)
        when(sessionRepository.set(any())) thenReturn Future.successful(true)
        val service = new DataMigrationService(authenticatedRepository, unauthenticatedRepository, sessionRepository)

        service.migrate(sessionId, userId).futureValue
        verify(authenticatedRepository, times(1)).set(captor.capture())
        captor.getValue must matchPattern { case UserAnswers(`userId`, `emptyJson`, None, _) => }
      }
    }

    "when repository does not return success" - {

      "throw DataOperationFailedError" in {
        val sessionId                 = "session-id"
        val userId                    = "userId"
        val json                      = Json.obj("foo" -> "bar")
        val unauthenticatedData       = UserAnswers(sessionId, json, None)

        when(unauthenticatedRepository.get(any())) thenReturn Future.successful(Some(unauthenticatedData))
        when(authenticatedRepository.set(any())) thenReturn Future.successful(false)
        when(sessionRepository.get(any())) thenReturn Future.successful(Seq.empty)
        when(sessionRepository.set(any())) thenReturn Future.successful(true)
        val service = new DataMigrationService(authenticatedRepository, unauthenticatedRepository, sessionRepository)

        val d = intercept[Exception](service.migrate(sessionId, userId).futureValue)
        d.getMessage must include("Failed to set authenticated user answers during migration")

      }
    }
  }
}
