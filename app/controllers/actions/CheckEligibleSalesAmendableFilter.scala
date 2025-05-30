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

import logging.Logging
import models.requests.AuthenticatedDataRequest
import models.{AmendMode, Mode}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import services.RegistrationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckEligibleSalesAmendableFilterImpl(mode: Option[Mode],
                                            registrationService: RegistrationService
                                           )(implicit val executionContext: ExecutionContext)
  extends ActionFilter[AuthenticatedDataRequest] with Logging {

  override protected def filter[A](request: AuthenticatedDataRequest[A]): Future[Option[Result]] = {

    implicit val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    implicit val req: AuthenticatedDataRequest[A] = request

    mode match {
      case Some(AmendMode) =>
        registrationService.isEligibleSalesAmendable(AmendMode).map {
          case false => Some(Redirect(controllers.amend.routes.NoLongerAmendableController.onPageLoad().url))
          case _ => None
        }
      case _ => Future.successful(None)
    }

  }
}

class CheckEligibleSalesAmendableFilterProvider @Inject()(
                                                           registrationService: RegistrationService
                                                         )(implicit val executionContext: ExecutionContext) {
  def apply(mode: Option[Mode]): CheckEligibleSalesAmendableFilterImpl = {
    new CheckEligibleSalesAmendableFilterImpl(mode, registrationService)
  }
}

