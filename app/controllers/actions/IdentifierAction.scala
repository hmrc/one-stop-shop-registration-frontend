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

package controllers.actions

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.routes
import controllers.auth.{routes => authRoutes}
import logging.Logging
import models.requests.IdentifierRequest
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject()(
                                               override val authConnector: AuthConnector,
                                               config: FrontendAppConfig,
                                               val parser: BodyParsers.Default
                                             )
                                             (implicit val executionContext: ExecutionContext) extends IdentifierAction with AuthorisedFunctions with Logging {

  //noinspection ScalaStyle
  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    val continueUrl = config.loginContinueUrl + request.path

    authorised(
      AuthProviders(AuthProvider.GovernmentGateway) and
        (AffinityGroup.Individual or AffinityGroup.Organisation) and
        CredentialStrength(CredentialStrength.strong)
    ).retrieve(
      Retrievals.internalId and
        Retrievals.allEnrolments and
        Retrievals.affinityGroup and
        Retrievals.confidenceLevel and
        Retrievals.credentialRole) {

      case Some(internalId) ~ enrolments ~ Some(Organisation) ~ _ ~ Some(credentialRole) if credentialRole == User =>
        findVrnFromEnrolments(enrolments) match {
          case Some(vrn) => block(IdentifierRequest(request, internalId, vrn))
          case None      => throw InsufficientEnrolments()
        }

      case _ ~ _ ~ Some(Organisation) ~ _ ~ Some(credentialRole) if credentialRole == Assistant =>
        throw UnsupportedCredentialRole()

      case Some(internalId) ~ enrolments ~ Some(Individual) ~ confidence ~ _ =>
        findVrnFromEnrolments(enrolments) match {
          case Some(vrn) =>
            if (confidence >= ConfidenceLevel.L250) {
              block(IdentifierRequest(request, internalId, vrn))
            } else {
              throw InsufficientConfidenceLevel() // TODO: Change to an IV uplift journey
            }

          case None =>
            throw InsufficientEnrolments()
        }

      case _ =>
        throw new UnauthorizedException("Unable to retrieve authorisation data")

    } recover {
      case _: NoActiveSession =>
        logger.info("No active session")
        Redirect(config.loginUrl, Map("continue" -> Seq(continueUrl)))

      case _: UnsupportedAffinityGroup =>
        logger.info("Unsupported affinity grouop")
        Redirect(authRoutes.AuthController.unsupportedAffinityGroup())

      case _: UnsupportedAuthProvider =>
        logger.info("Unsupported auth provider")
        Redirect(authRoutes.AuthController.unsupportedAuthProvider(continueUrl = continueUrl))

      case _: UnsupportedCredentialRole =>
        logger.info("Unsupported credential role")
        Redirect(authRoutes.AuthController.unsupportedCredentialRole().url)

      case _: InsufficientEnrolments =>
        logger.info("Insufficient enrolments")
        Redirect(authRoutes.AuthController.insufficientEnrolments())

      case _: IncorrectCredentialStrength =>
        logger.info("Incorrect credential strength")
        upliftCredentialStrength(request)

      case _: InsufficientConfidenceLevel =>
        logger.info("Insufficient confidence level")
        upliftConfidenceLevel(request)

      case e: AuthorisationException =>
        logger.info("Authorisation Exception", e.getMessage)
        Redirect(routes.UnauthorisedController.onPageLoad())

      case e: UnauthorizedException =>
        logger.info("Unauthorised exception", e.message)
        Redirect(routes.UnauthorisedController.onPageLoad())
    }
  }

  private def findVrnFromEnrolments(enrolments: Enrolments): Option[Vrn] =
    enrolments.enrolments.find(_.key == "HMRC-MTD-VAT")
      .flatMap {
        enrolment =>
          enrolment.identifiers.find(_.key == "VRN").map(e => Vrn(e.value))
      } orElse enrolments.enrolments.find(_.key == "HMRE-VATDEC-ORG")
      .flatMap {
        enrolment =>
          enrolment.identifiers.find(_.key == "VATRegNo").map(e => Vrn(e.value))
      }

  private def upliftCredentialStrength(request: Request[_]): Result =
    Redirect(
      config.mfaUpliftUrl,
      Map(
        "origin"      -> Seq(config.origin),
        "continueUrl" -> Seq(config.loginContinueUrl + request.path)
      )
    )

  private def upliftConfidenceLevel[A](request: Request[A]): Result =
    Redirect(
      config.ivUpliftUrl,
      Map(
        "origin"          -> Seq(config.origin),
        "confidenceLevel" -> Seq(ConfidenceLevel.L250.toString),
        "completionURL"   -> Seq(config.loginContinueUrl + request.path),
        "failureURL"      ->
          Seq(config.loginContinueUrl + authRoutes.IdentityVerificationController.handleIvFailure(config.loginContinueUrl + request.path, None).url)
      )
    )
}
