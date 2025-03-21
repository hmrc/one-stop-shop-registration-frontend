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


import connectors.{RegistrationConnector, SaveForLaterConnector}
import controllers.actions.FakeSavedAnswersRetrievalAction.*
import models.UserAnswers
import models.requests.AuthenticatedOptionalDataRequest
import org.scalatestplus.mockito.MockitoSugar.mock
import repositories.AuthenticatedUserAnswersRepository
import uk.gov.hmrc.domain.Vrn

import scala.concurrent.{ExecutionContext, Future}

class FakeSavedAnswersRetrievalAction(dataToReturn: Option[UserAnswers], vrn: Vrn)
  extends SavedAnswersRetrievalAction(
    repository,
    saveForLaterConnector,
    registrationConnector
  )(ExecutionContext.Implicits.global) {

  override protected def transform[A](request: AuthenticatedOptionalDataRequest[A]): Future[AuthenticatedOptionalDataRequest[A]] =
    Future.successful(
      AuthenticatedOptionalDataRequest(
        request.request,
        request.credentials,
        vrn,
        None,
        dataToReturn,
        request.iossNumber,
        request.numberOfIossRegistrations,
        request.latestIossRegistration
      ))
}

object FakeSavedAnswersRetrievalAction {
  val repository: AuthenticatedUserAnswersRepository = mock[AuthenticatedUserAnswersRepository]
  val saveForLaterConnector: SaveForLaterConnector = mock[SaveForLaterConnector]
  val registrationConnector: RegistrationConnector = mock[RegistrationConnector]
}
