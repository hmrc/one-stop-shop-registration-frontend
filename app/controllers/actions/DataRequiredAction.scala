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
import controllers.routes
import models.requests.{AuthenticatedDataRequest, AuthenticatedOptionalDataRequest, UnauthenticatedDataRequest, UnauthenticatedOptionalDataRequest}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedDataRequiredAction @Inject()(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[AuthenticatedOptionalDataRequest, AuthenticatedDataRequest] {

  override protected def refine[A](request: AuthenticatedOptionalDataRequest[A]): Future[Either[Result, AuthenticatedDataRequest[A]]] = {

    request.userAnswers match {
      case None =>
        Future.successful(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())))
      case Some(data) =>
        Future.successful(Right(AuthenticatedDataRequest(request.request, request.credentials, request.vrn, data)))
    }
  }
}

class UnauthenticatedDataRequiredAction @Inject()(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[UnauthenticatedOptionalDataRequest, UnauthenticatedDataRequest] {

  override protected def refine[A](request: UnauthenticatedOptionalDataRequest[A]): Future[Either[Result, UnauthenticatedDataRequest[A]]] = {

    request.userAnswers match {
      case None =>
        Future.successful(Left(Redirect(routes.RegisteredForOssInEuController.onPageLoad())))
      case Some(data) =>
        Future.successful(Right(UnauthenticatedDataRequest(request.request, request.userId, data)))
    }
  }
}
