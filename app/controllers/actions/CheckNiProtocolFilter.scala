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

import config.FrontendAppConfig
import connectors.RegistrationConnector
import controllers.routes
import models.requests.AuthenticatedIdentifierRequest
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckNiProtocolFilterImpl @Inject()(connector: RegistrationConnector,
                                          appConfig: FrontendAppConfig
                                         )
                                         (implicit val executionContext: ExecutionContext)
  extends CheckNiProtocolFilter {

  override protected def filter[A](request: AuthenticatedIdentifierRequest[A]): Future[Option[Result]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    if (appConfig.registrationValidationEnabled) {
      connector.validateRegistration(request.vrn) map {
        case Right(validationResult) =>
          if (validationResult.validRegistration) {
            None
          } else {
            Some(Redirect(routes.NiProtocolRejectionController.onPageLoad()))
          }
        case Left(error) =>
          throw new Exception(error.body)
      }
    } else {
      Future.successful(None)
    }

  }
}

trait CheckNiProtocolFilter extends ActionFilter[AuthenticatedIdentifierRequest]