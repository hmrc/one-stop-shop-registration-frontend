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

import connectors.RegistrationConnector
import controllers.routes
import models.requests.AuthenticatedIdentifierRequest
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import services.DataMigrationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckRegistrationFilterImpl @Inject()(connector: RegistrationConnector,
                                            migrationService: DataMigrationService)
                                           (implicit val executionContext: ExecutionContext)
  extends CheckRegistrationFilter {

  override protected def filter[A](request: AuthenticatedIdentifierRequest[A]): Future[Option[Result]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    connector.getRegistration() map {
      case Some(_) =>
        request.queryString.get("k").flatMap(_.headOption).map(sessionId =>
        migrationService
          .migrate(sessionId, request.userId)
        )
        Some(Redirect(routes.AlreadyRegisteredController.onPageLoad()))
      case None    => None
    }
  }
}

trait CheckRegistrationFilter extends ActionFilter[AuthenticatedIdentifierRequest]