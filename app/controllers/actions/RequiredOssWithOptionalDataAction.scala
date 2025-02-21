/*
 * Copyright 2025 HM Revenue & Customs
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

import controllers.routes
import models.requests.{AuthenticatedOptionalDataRequest, AuthenticatedOssOptionalDataRequest}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import utils.FutureSyntax.FutureOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RequiredOssWithOptionalDataActionImpl(
                                             implicit val executionContext: ExecutionContext
                                           )
  extends ActionRefiner[AuthenticatedOptionalDataRequest, AuthenticatedOssOptionalDataRequest] {

  private type OptionalOssRequiredActionResult[A] = Future[Either[Result, AuthenticatedOssOptionalDataRequest[A]]]

  override protected def refine[A](request: AuthenticatedOptionalDataRequest[A]): OptionalOssRequiredActionResult[A] = {

    request.registration match {
      case Some(registration) =>
        Right(
          AuthenticatedOssOptionalDataRequest(
            request = request,
            credentials = request.credentials,
            vrn = request.vrn,
            registration = registration,
            userAnswers = request.userAnswers
          )).toFuture

      case _ =>
        Left(Redirect(routes.NotRegisteredController.onPageLoad().url)).toFuture
    }
  }
}

class RequiredOssWithOptionalDataAction @Inject()(
                                                   implicit executionContext: ExecutionContext
                                                 ) {

  def apply(): RequiredOssWithOptionalDataActionImpl = {
    new RequiredOssWithOptionalDataActionImpl()
  }
}
