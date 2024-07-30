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
import models.{UserAnswers, VatApiCallResult}
import models.requests._

import javax.inject.Inject
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, ActionTransformer, Result}
import queries.VatApiCallResultQuery
import services.DataMigrationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.FutureSyntax._
import repositories.{AuthenticatedUserAnswersRepository, UnauthenticatedUserAnswersRepository}
import controllers.routes

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedDataRetrievalAction @Inject()(authenticatedUserAnswersRepository: AuthenticatedUserAnswersRepository,
                                                 migrationService: DataMigrationService,
                                                 registrationConnector: RegistrationConnector
                                                )
                                                (implicit val executionContext: ExecutionContext)
  extends ActionRefiner[AuthenticatedIdentifierRequest, AuthenticatedOptionalDataRequest] {

  override protected def refine[A](request: AuthenticatedIdentifierRequest[A]): Future[Either[Result, AuthenticatedOptionalDataRequest[A]]] = {

    implicit lazy val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    request.queryString.get("k").flatMap(_.headOption) match {
      case Some(sessionId) =>
        val k = migrationService
          .migrate(sessionId, request.userId)
          .map(_ => Left(Redirect(request.path)))
k
      case None =>
        authenticatedUserAnswersRepository
          .get(request.userId)
          .flatMap {
            case None =>
              copyCurrentSessionData(request).map(Right(_))
            case Some(answers) =>
              if (answers.vatInfo.isDefined) {
                getUpdatedVatInfo(answers).flatMap {
                  case Right(updatedUserAnswers) =>
                    AuthenticatedOptionalDataRequest(request, request.credentials, request.vrn, Some(updatedUserAnswers))
                      .toFuture
                      .map(Right[Result, AuthenticatedOptionalDataRequest[A]])
                  case Left(value) =>
                    value.toFuture.map(Left[Result, AuthenticatedOptionalDataRequest[A]])
                }
              } else {
                AuthenticatedOptionalDataRequest(request, request.credentials, request.vrn, Some(answers))
                  .toFuture
                  .map(Right[Result, AuthenticatedOptionalDataRequest[A]])
              }
          }
    }
  }

  private def getUpdatedVatInfo(answers: UserAnswers)(implicit hc: HeaderCarrier): Future[Either[Result, UserAnswers]] = {
    registrationConnector.getVatCustomerInfo().flatMap {
      case Right(vatInfo) =>
        Future.successful(
          Right[Result, UserAnswers](answers.copy(vatInfo = Some(vatInfo)))
        )
      case Left(_) =>
        for {
          updatedAnswers <- Future.fromTry(answers.set(VatApiCallResultQuery, VatApiCallResult.Error))
          _ <- authenticatedUserAnswersRepository.set(updatedAnswers)
        } yield {
          Left[Result, UserAnswers](Redirect(routes.JourneyRecoveryController.onPageLoad()))
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
