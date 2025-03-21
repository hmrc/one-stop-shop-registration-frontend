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

import controllers.amend.routes as amendRoutes
import controllers.routes
import models.requests.{AuthenticatedDataRequest, AuthenticatedOptionalDataRequest, UnauthenticatedDataRequest, UnauthenticatedOptionalDataRequest}
import models.{AmendMode, Mode}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import utils.CheckJourneyRecovery.determineJourneyRecovery
import utils.FutureSyntax.*

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedDataRequiredActionImpl @Inject()(
                                                     mode: Option[Mode]
                                                   )(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[AuthenticatedOptionalDataRequest, AuthenticatedDataRequest] {

  override protected def refine[A](request: AuthenticatedOptionalDataRequest[A]): Future[Either[Result, AuthenticatedDataRequest[A]]] = {

    request.userAnswers match {
      case None =>
        Left(Redirect(determineJourneyRecovery(mode))).toFuture
      case Some(data) =>
        if (mode.exists(_.isInAmendOrRejoin)) {
          request.registration match {
            case Some(registration) =>
              Right(AuthenticatedDataRequest(
                request.request,
                request.credentials,
                request.vrn,
                Some(registration),
                data,
                request.iossNumber,
                request.numberOfIossRegistrations,
                request.latestIossRegistration
              )).toFuture
            case None =>
              if (mode.contains(AmendMode)) {
                Left(Redirect(amendRoutes.AmendJourneyRecoveryController.onPageLoad())).toFuture
              } else {
                Left(Redirect(controllers.rejoin.routes.RejoinJourneyRecoveryController.onPageLoad())).toFuture
              }
          }
        } else {
          Right(AuthenticatedDataRequest(
            request.request,
            request.credentials,
            request.vrn,
            None,
            data,
            request.iossNumber,
            request.numberOfIossRegistrations,
            request.latestIossRegistration
          )).toFuture
        }
    }
  }

}

class UnauthenticatedDataRequiredAction @Inject()(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[UnauthenticatedOptionalDataRequest, UnauthenticatedDataRequest] {

  override protected def refine[A](request: UnauthenticatedOptionalDataRequest[A]): Future[Either[Result, UnauthenticatedDataRequest[A]]] = {

    request.userAnswers match {
      case None =>
        Left(Redirect(routes.RegisteredForOssInEuController.onPageLoad())).toFuture
      case Some(data) =>
        Right(UnauthenticatedDataRequest(request.request, request.userId, data)).toFuture
    }
  }
}

class AuthenticatedDataRequiredAction @Inject()(implicit val executionContext: ExecutionContext) {

  def apply(mode: Option[Mode]): AuthenticatedDataRequiredActionImpl = {
    new AuthenticatedDataRequiredActionImpl(mode)
  }
}