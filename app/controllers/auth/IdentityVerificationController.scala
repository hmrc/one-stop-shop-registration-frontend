/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.auth

import connectors.IdentityVerificationConnector
import models.iv.IdentityVerificationResult._
import models.iv._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.iv.IdentityProblemView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IdentityVerificationController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                val controllerComponents: MessagesControllerComponents,
                                                ivConnector: IdentityVerificationConnector,
                                                view: IdentityProblemView
                                              )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport {

  def identityError(continueUrl: String): Action[AnyContent] = Action {
    implicit request =>
      Ok(view(continueUrl))
  }

  private val allPossibleEvidences: List[IdentityVerificationEvidenceSource] =
    List(PayslipService, P60Service, NtcService, Passport, CallValidate)

  private def allSourcesDisabled(disabledSources: List[IdentityVerificationEvidenceSource]): Boolean =
    allPossibleEvidences.forall(disabledSources.contains)

  def handleIvFailure(continueUrl: String, journeyId: Option[String]): Action[AnyContent] = Action.async {
    implicit request =>

      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      journeyId.map {
        id =>
          ivConnector.getJourneyStatus(id).flatMap {
            case Some(result: IdentityVerificationResult) =>
              result match {
                case InsufficientEvidence       => handleInsufficientEvidence
                case Success                    => Future.successful(Redirect(continueUrl))
                case Incomplete                 => Future.successful(Redirect(routes.IvReturnController.incomplete().url))
                case FailedMatching             => Future.successful(Redirect(routes.IvReturnController.failedMatching(continueUrl).url))
                case FailedIdentityVerification => Future.successful(Redirect(routes.IvReturnController.failed(continueUrl).url))
                case UserAborted                => Future.successful(Redirect(routes.IvReturnController.userAborted(continueUrl).url))
                case LockedOut                  => Future.successful(Redirect(routes.IvReturnController.lockedOut().url))
                case PrecondFailed              => Future.successful(Redirect(routes.IvReturnController.preconditionFailed().url))
                case TechnicalIssue             => Future.successful(Redirect(routes.IvReturnController.technicalIssue().url))
                case Timeout                    => Future.successful(Redirect(routes.IvReturnController.timeout().url))
              }
            case _ =>
              Future.successful(Redirect(routes.IvReturnController.error().url))
          }
      }.getOrElse {
        Future.successful(Redirect(routes.IdentityVerificationController.identityError(continueUrl).url))
      }
  }

  private def handleInsufficientEvidence()(implicit hc: HeaderCarrier): Future[Result] =
    ivConnector.getDisabledEvidenceSources().map {
      case list if allSourcesDisabled(list) => Redirect(routes.IvReturnController.notEnoughEvidenceSources().url)
      case _                                => Redirect(routes.IvReturnController.insufficientEvidence().url)
    }
}
