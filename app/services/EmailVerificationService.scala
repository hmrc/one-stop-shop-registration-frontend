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

package services

import config.FrontendAppConfig
import connectors.EmailVerificationConnector
import connectors.EmailVerificationHttpParser.ReturnVerificationStatus
import controllers.routes
import logging.Logging
import models.emailVerification.{EmailStatus, EmailVerificationRequest, VerifyEmail}
import models.{Mode, NormalMode}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailVerificationService @Inject()(
                                        config: FrontendAppConfig,
                                        validateEmailConnector: EmailVerificationConnector
                                        )(implicit hc: HeaderCarrier, ec: ExecutionContext) extends Logging {


  def createEmailVerificationRequest(mode: Mode,
                                     credId: String,
                                     emailAddress: String,
                                     pageTitle: Option[String],
                                     continueUrl: String
                                  ): Future[String] = {
    validateEmailConnector.verifyEmail(
      EmailVerificationRequest(
        credId = credId,
        continueUrl = continueUrl,
        origin = config.origin,
        deskproServiceName = Some("one-stop-shop-registration-frontend"),
        accessibilityStatementUrl = config.accessibilityStatementUrl,
        pageTitle = pageTitle,
        backUrl = Some(routes.BusinessContactDetailsController.onPageLoad(mode).url),
        email = Some(
          VerifyEmail(
            address = emailAddress,
            enterUrl = routes.BusinessContactDetailsController.onPageLoad(NormalMode).url
          )
        )
      )
    ).map {
      case Right(response) => response.redirectUri
      case Left(error) => error.body
    }
  }

  private def getStatus(credId: String)(implicit hc: HeaderCarrier): Future[ReturnVerificationStatus] = {
    validateEmailConnector.getStatus(credId)
  }

  def isEmailVerified(emailAddress: String, credId: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    getStatus(credId).map {
        case Right(Some(verificationStatus)) =>
          verificationStatus.emails.exists {
            case emailStatus @ EmailStatus(_, true, _) if emailStatus.emailAddress == emailAddress => true
            case _ => false
          }
        case Right(None) =>
          false
        case Left(error) =>
          logger.error(s"There was an error retrieving verification status", error.body)
          false
      }
  }

}

