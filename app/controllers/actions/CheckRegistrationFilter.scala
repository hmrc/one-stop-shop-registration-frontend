/*
 * Copyright 2023 HM Revenue & Customs
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
import logging.Logging
import models.requests.AuthenticatedIdentifierRequest
import models.{AmendLoopMode, AmendMode, Mode}
import play.api.http.Status.NO_CONTENT
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import services.DataMigrationService
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.FutureSyntax.FutureOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckRegistrationFilterImpl(mode: Option[Mode],
                                  connector: RegistrationConnector,
                                  config: FrontendAppConfig,
                                  migrationService: DataMigrationService)
                                 (implicit val executionContext: ExecutionContext)
  extends ActionFilter[AuthenticatedIdentifierRequest] with Logging {

  override protected def filter[A](request: AuthenticatedIdentifierRequest[A]): Future[Option[Result]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    (for {
      mayBeRegistration <- connector.getRegistration()
    } yield {

      if (mode.contains(AmendMode) || mode.contains(AmendLoopMode)) {
        mayBeRegistration match {

          case Some(_) if hasRegistrationEnrolment(request.enrolments) => None.toFuture

          case Some(_) => enrolRegisteredUser(request.vrn)

          case _ => Some(Redirect(routes.NotRegisteredController.onPageLoad())).toFuture
        }
      } else if (mayBeRegistration.isDefined || hasRegistrationEnrolment(request.enrolments)) {
        request.queryString.get("k").flatMap(_.headOption).map(sessionId =>
          migrationService
            .migrate(sessionId, request.userId)
        )
        Some(Redirect(routes.AlreadyRegisteredController.onPageLoad())).toFuture
      } else {
        None.toFuture
      }
    }).flatten
  }

  private def hasRegistrationEnrolment(enrolments: Enrolments): Boolean = {
    config.enrolmentsEnabled && enrolments.enrolments.exists(_.key == config.ossEnrolment)
  }

  private def enrolRegisteredUser(vrn: Vrn)
                                           (implicit hc: HeaderCarrier): Future[Option[Result]] = {

    connector.enrolUser().map { response =>
      response.status match {
        case NO_CONTENT =>
          logger.info(s"Successfully retrospectively enrolled user ${vrn.vrn}")
          None
        case status =>
          logger.error(s"Failure enrolling an existing user, got status $status from registration service")
          throw new IllegalStateException("Existing user didn't have enrolment and was unable to enrol user")
      }
    }
  }
}

class CheckRegistrationFilterProvider @Inject()(
                                                 connector: RegistrationConnector,
                                                 config: FrontendAppConfig,
                                                 migrationService: DataMigrationService
                                               )(implicit ec: ExecutionContext) {
  def apply(mode: Option[Mode]): CheckRegistrationFilterImpl = {
    new CheckRegistrationFilterImpl(mode, connector, config, migrationService)
  }
}