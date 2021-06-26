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

import javax.inject.Inject
import models.requests.{AuthenticatedIdentifierRequest, AuthenticatedOptionalDataRequest, SessionRequest, UnauthenticatedOptionalDataRequest}
import play.api.mvc.ActionTransformer
import repositories.{AuthenticatedSessionRepository, UnauthenticatedSessionRepository}

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedDataRetrievalAction @Inject()(val sessionRepository: AuthenticatedSessionRepository)
                                                (implicit val executionContext: ExecutionContext)
  extends ActionTransformer[AuthenticatedIdentifierRequest, AuthenticatedOptionalDataRequest] {

  override protected def transform[A](request: AuthenticatedIdentifierRequest[A]): Future[AuthenticatedOptionalDataRequest[A]] = {

    sessionRepository.get(request.userId).map {
      AuthenticatedOptionalDataRequest(request.request, request.credentials, request.vrn, _)
    }
  }
}

class UnauthenticatedDataRetrievalAction @Inject()(val sessionRepository: UnauthenticatedSessionRepository)
                                                (implicit val executionContext: ExecutionContext)
  extends ActionTransformer[SessionRequest, UnauthenticatedOptionalDataRequest] {

  override protected def transform[A](request: SessionRequest[A]): Future[UnauthenticatedOptionalDataRequest[A]] = {

    sessionRepository.get(request.userId).map {
      UnauthenticatedOptionalDataRequest(request.request, request.userId, _)
    }
  }
}
