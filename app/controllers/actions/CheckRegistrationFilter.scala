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
import connectors.RegistrationConnector
import controllers.ioss.routes as iossExclusionsRoutes
import controllers.routes
import logging.Logging
import models.Mode
import models.requests.AuthenticatedIdentifierRequest
import play.api.http.Status.NO_CONTENT
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import services.DataMigrationService
import services.ioss.IossExclusionService
import uk.gov.hmrc.auth.core.{EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.FutureSyntax.FutureOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckRegistrationFilterImpl(
                                   mode: Option[Mode],
                                   connector: RegistrationConnector,
                                   frontendAppConfig: FrontendAppConfig,
                                   migrationService: DataMigrationService,
                                   iossExclusionService: IossExclusionService
                                 )(implicit val executionContext: ExecutionContext)
  extends ActionFilter[AuthenticatedIdentifierRequest] with Logging {

  override protected def filter[A](request: AuthenticatedIdentifierRequest[A]): Future[Option[Result]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    if (mode.exists(_.isInAmendOrRejoin)) {
      request.registration match {
        case Some(_) if hasRegistrationEnrolment(request.enrolments) =>
          if (mode.exists(_.isInAmend)) {
            None.toFuture
          } else {
            checkIossExclusionAndRedirect(request)
          }

        case Some(_) =>
          enrolRegisteredUser(request)

        case _ => Some(Redirect(routes.NotRegisteredController.onPageLoad())).toFuture
      }
    } else if (request.registration.isDefined || hasRegistrationEnrolment(request.enrolments)) {
      request.queryString.get("k").flatMap(_.headOption).map(sessionId =>
        migrationService
          .migrate(sessionId, request.userId)
      )
      Some(Redirect(routes.AlreadyRegisteredController.onPageLoad())).toFuture
    } else {
      checkIossExclusionAndRedirect(request)
    }
  }

  private def checkIossExclusionAndRedirect(request: AuthenticatedIdentifierRequest[_])(implicit hc: HeaderCarrier): Future[Option[Result]] = {
    hasExcludedIossEnrolment(request).map {
      case true =>
        Some(Redirect(iossExclusionsRoutes.CannotRegisterQuarantinedIossTraderController.onPageLoad()))
      case _ =>
        None
    }
  }

  private def hasRegistrationEnrolment(enrolments: Enrolments): Boolean = {
    frontendAppConfig.enrolmentsEnabled && enrolments.enrolments.exists(_.key == frontendAppConfig.ossEnrolment)
  }

  private def enrolRegisteredUser(request: AuthenticatedIdentifierRequest[_])
                                 (implicit hc: HeaderCarrier): Future[Option[Result]] = {

    connector.enrolUser().flatMap { response =>
      response.status match {
        case NO_CONTENT =>
          logger.info(s"Successfully retrospectively enrolled user ${request.vrn.vrn}")
          checkIossExclusionAndRedirect(request)
        case status =>
          logger.error(s"Failure enrolling an existing user, got status $status from registration service")
          throw new IllegalStateException("Existing user didn't have enrolment and was unable to enrol user")
      }
    }
  }

  private def hasExcludedIossEnrolment(request: AuthenticatedIdentifierRequest[_])(implicit hc: HeaderCarrier): Future[Boolean] = {
    getIossEnrolments(request) match {
      case Some(_) =>
        iossExclusionService.isQuarantinedCode4().map { result =>
          result
        }
      case _ =>
        false.toFuture
    }
  }

  private def getIossEnrolments(request: AuthenticatedIdentifierRequest[_]): Option[EnrolmentIdentifier] = {
    request.enrolments.enrolments.filter(_.key == frontendAppConfig.iossEnrolment).toSeq
      .flatMap(_.identifiers.filter(_.key == "IOSSNumber")).headOption
  }
}

class CheckRegistrationFilterProvider @Inject()(
                                                 connector: RegistrationConnector,
                                                 frontendAppConfig: FrontendAppConfig,
                                                 migrationService: DataMigrationService,
                                                 iossExclusionService: IossExclusionService
                                               )(implicit ec: ExecutionContext) {
  def apply(mode: Option[Mode]): CheckRegistrationFilterImpl = {
    new CheckRegistrationFilterImpl(mode, connector, frontendAppConfig, migrationService, iossExclusionService)
  }
}