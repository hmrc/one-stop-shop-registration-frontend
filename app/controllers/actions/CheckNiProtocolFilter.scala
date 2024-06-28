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

import config.FrontendAppConfig
import controllers.routes
import logging.Logging
import models.{AmendMode, Mode}
import models.requests.AuthenticatedDataRequest
import play.api.mvc.{ActionFilter, Result}
import play.api.mvc.Results.Redirect

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckNiProtocolFilterImpl(mode: Option[Mode],
                                appConfig: FrontendAppConfig)
                               (implicit val executionContext: ExecutionContext)
  extends ActionFilter[AuthenticatedDataRequest] with Logging {

  override protected def filter[A](request: AuthenticatedDataRequest[A]): Future[Option[Result]] = {

    if (appConfig.registrationValidationEnabled) {
      request.userAnswers.vatInfo match {
        case Some(vatCustomerInfo) => vatCustomerInfo.singleMarketIndicator match {
          case Some(true) => Future.successful(None)

          case Some(false) =>
            if (!mode.contains(AmendMode)){
              Future.successful(Some(Redirect(routes.NiProtocolRejectionController.onPageLoad())))
            } else {
              Future.successful(None)
            }

          case _ =>
            logger.error("Illegal state cause by SingleMarketIndicator missing")
            throw new IllegalStateException("Illegal State Exception while processing the request for SingleMarketIndicator")
        }
        case _ => Future.successful(None)
      }
    } else {
      Future.successful(None)
    }
  }
}

class CheckNiProtocolFilter @Inject()(appConfig: FrontendAppConfig)
                                     (implicit val executionContext: ExecutionContext) {

  def apply(mode: Option[Mode]): CheckNiProtocolFilterImpl = {
    new CheckNiProtocolFilterImpl(mode, appConfig)
  }
}
