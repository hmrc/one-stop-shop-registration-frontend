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

import connectors.RegistrationConnector
import controllers.actions.FakeAuthenticatedDataRetrievalAction.{mockMigrationService, mockSessionRepository, registrationConnector, stubClock}
import models.UserAnswers
import models.requests.{AuthenticatedIdentifierRequest, AuthenticatedOptionalDataRequest}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.Result
import repositories.AuthenticatedUserAnswersRepository
import services.DataMigrationService
import uk.gov.hmrc.domain.Vrn
import utils.FutureSyntax._

import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.{ExecutionContext, Future}

class FakeAuthenticatedDataRetrievalAction(dataToReturn: Option[UserAnswers], vrn: Vrn)
  extends AuthenticatedDataRetrievalAction(mockSessionRepository, mockMigrationService, registrationConnector, stubClock)(ExecutionContext.Implicits.global) {

  override protected def refine[A](request: AuthenticatedIdentifierRequest[A]): Future[Either[Result, AuthenticatedOptionalDataRequest[A]]] =
    Right(
      AuthenticatedOptionalDataRequest(
        request.request,
        request.credentials,
        vrn,
        dataToReturn
      )
    ).toFuture
}

object FakeAuthenticatedDataRetrievalAction {
  val mockSessionRepository: AuthenticatedUserAnswersRepository = mock[AuthenticatedUserAnswersRepository]
  val mockMigrationService: DataMigrationService = mock[DataMigrationService]
  val registrationConnector: RegistrationConnector = mock[RegistrationConnector]

  val instant: Instant = Instant.now
  val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

}
