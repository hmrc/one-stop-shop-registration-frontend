/*
 * Copyright 2026 HM Revenue & Customs
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

import models.{AmendLoopMode, AmendMode, Mode}
import models.exclusions.ExclusionReason.Reversal
import models.requests.AuthenticatedDataRequest
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import utils.FutureSyntax.FutureOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

// TODO - Test and create Fake
class CheckExcludedInAmendFilterImpl(
                                      mode: Option[Mode],
                                      restrictExcludedInAmend: Boolean
                                    )(implicit val executionContext: ExecutionContext) extends ActionFilter[AuthenticatedDataRequest] {
  override protected def filter[A](request: AuthenticatedDataRequest[A]): Future[Option[Result]] = {
    
    val isExcluded: Boolean = request.registration.exists(_.excludedTrader.exists(_.exclusionReason != Reversal))

    if (restrictExcludedInAmend && isExcluded && mode.contains(AmendMode) || mode.contains(AmendLoopMode)) {
      // TODO -> Redirect to ???? Create new Cannot access Page?
      Some(Redirect(controllers.routes.InvalidVrnDateController.onPageLoad())).toFuture
    } else {
      None.toFuture
    }
  }
}

class CheckExcludedInAmendFilter @Inject()(implicit val executionContext: ExecutionContext) {

  def apply(mode: Option[Mode], restrictExcludedInAmend: Boolean) = new CheckExcludedInAmendFilterImpl(mode, restrictExcludedInAmend)
}
