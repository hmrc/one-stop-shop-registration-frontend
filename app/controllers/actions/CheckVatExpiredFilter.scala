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
import models.{AmendMode, Mode}
import models.requests.AuthenticatedDataRequest
import play.api.mvc.{ActionFilter, Result}
import play.api.mvc.Results.Redirect
import utils.FutureSyntax.FutureOps

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckVatExpiredFilterImpl(
                                 mode: Option[Mode],
                                 clock: Clock
                               )
                               (implicit val executionContext: ExecutionContext)
  extends ActionFilter[AuthenticatedDataRequest] with Logging {

  override protected def filter[A](request: AuthenticatedDataRequest[A]): Future[Option[Result]] = {

    if (!mode.contains(AmendMode)) {

      request.userAnswers.vatInfo.flatMap(_.deregistrationDecisionDate) match {
        case Some(deregistrationDecisionDate) if !deregistrationDecisionDate.isAfter(LocalDate.now(clock)) =>
          Some(Redirect(controllers.routes.InvalidVrnDateController.onPageLoad())).toFuture
        case _ => None.toFuture
      }

    } else {
      None.toFuture
    }
  }
}

class CheckVatExpiredFilter @Inject()(clock: Clock)
                                     (implicit val executionContext: ExecutionContext) {

  def apply(mode: Option[Mode]): CheckVatExpiredFilterImpl = {
    new CheckVatExpiredFilterImpl(mode, clock)
  }
}
