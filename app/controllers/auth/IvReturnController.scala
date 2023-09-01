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

package controllers.auth

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.iv._

import javax.inject.Inject

class IvReturnController @Inject()(
                                    override val messagesApi: MessagesApi,
                                    val controllerComponents: MessagesControllerComponents,
                                    errorView: IvErrorView,
                                    failedMatchingView: IvFailedMatchingView,
                                    failedView: IvFailedView,
                                    incompleteView: IvIncompleteView,
                                    insufficientEvidenceView: IvInsufficientEvidenceView,
                                    lockedOutView: IvLockedOutView,
                                    notEnoughEvidenceSourcesView: IvNotEnoughEvidenceSourcesView,
                                    preconditionFailedView: IvPreconditionFailedView,
                                    technicalIssueView: IvTechnicalIssueView,
                                    timeoutView: IvTimeoutView,
                                    userAbortedView: IvUserAbortedView
                                  ) extends FrontendBaseController with I18nSupport {

  def error(continueUrl: String): Action[AnyContent] = Action {
    implicit request =>
      Ok(errorView(continueUrl))
  }

  def failedMatching(continueUrl: String): Action[AnyContent] = Action {
    implicit request =>
      Ok(failedMatchingView(continueUrl))
  }

  def failed(continueUrl: String): Action[AnyContent] = Action {
    implicit request =>
      Ok(failedView(continueUrl))
  }

  def incomplete(continueUrl: String): Action[AnyContent] = Action {
    implicit request =>
      Ok(incompleteView(continueUrl))
  }

  def insufficientEvidence(continueUrl: String): Action[AnyContent] = Action {
    implicit request =>
      Ok(insufficientEvidenceView(continueUrl))
  }

  def lockedOut(continueUrl: String): Action[AnyContent] = Action {
    implicit request =>
      Ok(lockedOutView(continueUrl))
  }

  def notEnoughEvidenceSources(continueUrl: String): Action[AnyContent] = Action {
    implicit request =>
      Ok(notEnoughEvidenceSourcesView(continueUrl))
  }

  def preconditionFailed(continueUrl: String): Action[AnyContent] = Action {
    implicit request =>
      Ok(preconditionFailedView(continueUrl))
  }

  def technicalIssue(continueUrl: String): Action[AnyContent] = Action {
    implicit request =>
      Ok(technicalIssueView(continueUrl))
  }

  def timeout(continueUrl: String): Action[AnyContent] = Action {
    implicit request =>
      Ok(timeoutView(continueUrl))
  }

  def userAborted(continueUrl: String): Action[AnyContent] = Action {
    implicit request =>
      Ok(userAbortedView(continueUrl))
  }
}
