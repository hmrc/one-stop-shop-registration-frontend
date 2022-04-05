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

package controllers.actions

import javax.inject.Inject
import models.requests._
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, ActionTransformer, Result}
import repositories.{AuthenticatedUserAnswersRepository, UnauthenticatedUserAnswersRepository}
import services.DataMigrationService
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.FutureSyntax._

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedDataRetrievalAction @Inject()(sessionRepository: AuthenticatedUserAnswersRepository,
                                                 migrationService: DataMigrationService)
                                                (implicit val executionContext: ExecutionContext)
  extends ActionRefiner[AuthenticatedIdentifierRequest, AuthenticatedOptionalDataRequest] {

  override protected def refine[A](request: AuthenticatedIdentifierRequest[A]): Future[Either[Result, AuthenticatedOptionalDataRequest[A]]] = {

    request.queryString.get("k").flatMap(_.headOption) match {
      case Some(sessionId) =>
        migrationService
          .migrate(sessionId, request.userId)
          .map(_ => Left(Redirect(request.path)))

      case None =>
        sessionRepository
          .get(request.userId)
          .flatMap {
            case None =>
              copyCurrentSessionData(request).map(Right(_))
            case Some(answers) =>
              AuthenticatedOptionalDataRequest(request, request.credentials, request.vrn, Some(answers)).toFuture.map(Right(_))
          }
    }
  }

  private def copyCurrentSessionData[A](request: AuthenticatedIdentifierRequest[A]): Future[AuthenticatedOptionalDataRequest[A]] = {
    val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    hc.sessionId.map {
      id =>
        migrationService
          .migrate(id.value, request.userId)
          .map(ua => AuthenticatedOptionalDataRequest(request, request.credentials, request.vrn, Some(ua)))
    }.getOrElse(AuthenticatedOptionalDataRequest(request, request.credentials, request.vrn, None).toFuture)
  }
}

class UnauthenticatedDataRetrievalAction @Inject()(val sessionRepository: UnauthenticatedUserAnswersRepository)
                                                (implicit val executionContext: ExecutionContext)
  extends ActionTransformer[SessionRequest, UnauthenticatedOptionalDataRequest] {

  override protected def transform[A](request: SessionRequest[A]): Future[UnauthenticatedOptionalDataRequest[A]] = {

    sessionRepository.get(request.userId).map {
      UnauthenticatedOptionalDataRequest(request.request, request.userId, _)
    }
  }
}
