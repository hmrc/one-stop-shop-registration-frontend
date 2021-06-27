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

import config.FrontendAppConfig
import connectors.RegistrationConnector
import controllers.actions.AuthenticatedControllerComponents
import models.{NormalMode, UserAnswers, VatApiCallResult, responses}
import pages.FirstAuthedPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.VatApiCallResultQuery
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.auth._

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import utils.FutureSyntax._

class AuthController @Inject()(
                                cc: AuthenticatedControllerComponents,
                                config: FrontendAppConfig,
                                connector: RegistrationConnector,
                                clock: Clock,
                                insufficientEnrolmentsView: InsufficientEnrolmentsView,
                                unsupportedAffinityGroupView: UnsupportedAffinityGroupView,
                                unsupportedAuthProviderView: UnsupportedAuthProviderView,
                                unsupportedCredentialRoleView: UnsupportedCredentialRoleView
                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onSignIn: Action[AnyContent] = cc.authAndGetOptionalData.async {
    implicit request =>

      val answers = request.userAnswers.getOrElse(UserAnswers(request.userId, lastUpdated = Instant.now(clock)))

      answers.get(VatApiCallResultQuery) match {
        case Some(_) =>
          Redirect(FirstAuthedPage.navigate(NormalMode, answers)).toFuture

        case None =>
          connector.getVatCustomerInfo().flatMap {
            case Right(vatInfo) =>
              for {
                updatedAnswers <- Future.fromTry(answers.copy(vatInfo = Some(vatInfo)).set(VatApiCallResultQuery, VatApiCallResult.Success))
                _              <- cc.sessionRepository.set(updatedAnswers)
              } yield Redirect(FirstAuthedPage.navigate(NormalMode, updatedAnswers))

            case Left(responses.NotFound) =>
              for {
                updatedAnswers <- Future.fromTry(answers.set(VatApiCallResultQuery, VatApiCallResult.NotFound))
                _              <- cc.sessionRepository.set(updatedAnswers)
              } yield Redirect(FirstAuthedPage.navigate(NormalMode, updatedAnswers))

            case _ =>
              if (cc.features.proceedWhenVatApiCallFails) {
                for {
                  updatedAnswers <- Future.fromTry(answers.set(VatApiCallResultQuery, VatApiCallResult.Error))
                  _              <- cc.sessionRepository.set(updatedAnswers)
                } yield Redirect(FirstAuthedPage.navigate(NormalMode, updatedAnswers))
              } else {
                InternalServerError.toFuture
              }
          }
      }
  }

  def redirectToRegister(continueUrl: String): Action[AnyContent] = Action {
    implicit request =>
      Redirect(
        config.registerUrl,
        Map(
          "origin"      -> Seq(config.origin),
          "continueUrl" -> Seq(continueUrl),
          "accountType" -> Seq("Organisation"))
      )
  }

  def redirectToLogin(continueUrl: String): Action[AnyContent] = Action {
    implicit request =>
        Redirect(config.loginUrl,
          Map(
            "origin"   -> Seq(config.origin),
            "continue" -> Seq(continueUrl)
          )
        )
  }

  def signOut(): Action[AnyContent] = Action {
    _ =>
      Redirect(config.signOutUrl, Map("continue" -> Seq(config.exitSurveyUrl)))
  }

  def signOutNoSurvey(): Action[AnyContent] = Action {
    _ =>
      Redirect(config.signOutUrl, Map("continue" -> Seq(routes.SignedOutController.onPageLoad().url)))
  }

  def unsupportedAffinityGroup(): Action[AnyContent] = Action {
    implicit request =>
      Ok(unsupportedAffinityGroupView())
  }

  def unsupportedAuthProvider(continueUrl: String): Action[AnyContent] = Action {
    implicit request =>
      Ok(unsupportedAuthProviderView(continueUrl))
  }

  def insufficientEnrolments(): Action[AnyContent] = Action {
    implicit request =>
      Ok(insufficientEnrolmentsView())
  }

  def unsupportedCredentialRole(): Action[AnyContent] = Action {
    implicit request =>
      Ok(unsupportedCredentialRoleView())
  }
}
