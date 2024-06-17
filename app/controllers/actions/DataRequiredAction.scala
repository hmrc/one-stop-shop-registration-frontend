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
import controllers.routes
import controllers.amend.{routes => amendRoutes}
import models.{AmendLoopMode, AmendMode, Mode, RejoinMode}
import models.requests.{AuthenticatedDataRequest, AuthenticatedOptionalDataRequest, UnauthenticatedDataRequest, UnauthenticatedOptionalDataRequest}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.CheckJourneyRecovery.determineJourneyRecovery
import utils.FutureSyntax._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedDataRequiredActionImpl @Inject()(
                                                     mode: Option[Mode],
                                                     val registrationConnector: RegistrationConnector
                                                   )(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[AuthenticatedOptionalDataRequest, AuthenticatedDataRequest] {

  override protected def refine[A](request: AuthenticatedOptionalDataRequest[A]): Future[Either[Result, AuthenticatedDataRequest[A]]] = {

    request.userAnswers match {
      case None =>
        Left(Redirect(determineJourneyRecovery(mode))).toFuture
      case Some(data) if data.data.value.isEmpty =>
        if (mode.contains(AmendMode) || mode.contains(AmendLoopMode)) {
          Left(Redirect(amendRoutes.AmendJourneyRecoveryController.onPageLoad())).toFuture
        } else if (mode.contains(RejoinMode)) {
          Left(Redirect(controllers.rejoin.routes.RejoinJourneyRecoveryController.onPageLoad())).toFuture
        } else {
          Left(Redirect(routes.JourneyRecoveryController.onMissingAnswers())).toFuture
        }
      case Some(data) =>
        if (mode.contains(AmendMode) || mode.contains(AmendLoopMode)) {
          val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request.request, request.session)
          registrationConnector.getRegistration()(hc) flatMap {
            case Some(registration) =>
              Right(AuthenticatedDataRequest(request.request, request.credentials, request.vrn, Some(registration), data)).toFuture
            case None =>
              Left(Redirect(amendRoutes.AmendJourneyRecoveryController.onPageLoad())).toFuture
          }

        } else {
          Right(AuthenticatedDataRequest(request.request, request.credentials, request.vrn, None, data)).toFuture

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

class AuthenticatedDataRequiredAction @Inject()(
                                                 registrationConnector: RegistrationConnector
                                               )(implicit val executionContext: ExecutionContext) {

  def apply(mode: Option[Mode]): AuthenticatedDataRequiredActionImpl = {
    new AuthenticatedDataRequiredActionImpl(mode, registrationConnector)
  }
}