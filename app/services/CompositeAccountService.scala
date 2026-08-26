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

package services

import config.FrontendAppConfig
import connectors.RegistrationConnector
import logging.Logging
import models.etmp.intermediary.{EtmpTradingName, IntermediaryRegistrationWrapper}
import models.iossRegistration.IossEtmpDisplayRegistration
import models.{BankDetails, BusinessContactDetails, CompositeAccount}
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureSyntax.FutureOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CompositeAccountService @Inject()(
                                         registrationConnector: RegistrationConnector,
                                         frontendAppConfig: FrontendAppConfig
                                       )(implicit ec: ExecutionContext) extends Logging {

  def getCompositeAccount(enrolments: Enrolments, iossNumber: Option[String])(implicit hc: HeaderCarrier): Future[Option[CompositeAccount]] = {

    val hasIossEnrolment = enrolments.enrolments.exists(_.key == frontendAppConfig.iossEnrolment)
    val hasIntermediaryEnrolment = enrolments.enrolments.exists(_.key == frontendAppConfig.intermediaryEnrolment)

    if (hasIossEnrolment) {
      getLatestIossRegistration(iossNumber).map {
        case Some(iossRegistration) =>
          createCompositeAccount(
            tradingNames = CompositeAccount.fromIossTradingName(iossRegistration.tradingNames),
            fullName = iossRegistration.schemeDetails.contactName,
            telephoneNumber = iossRegistration.schemeDetails.businessTelephoneNumber,
            emailAddress = iossRegistration.schemeDetails.businessEmailId,
            bankDetails = CompositeAccount.fromEtmpBankDetails(iossRegistration.bankDetails)
          )

        case _ => None
      }
    } else if (hasIntermediaryEnrolment) {
      getIntermediaryRegistration(enrolments).map {
        case Some(intermediaryRegistration) =>
          createCompositeAccount(
            tradingNames = intermediaryRegistration.etmpDisplayRegistration.tradingNames,
            fullName = intermediaryRegistration.etmpDisplayRegistration.schemeDetails.contactName,
            telephoneNumber = intermediaryRegistration.etmpDisplayRegistration.schemeDetails.businessTelephoneNumber,
            emailAddress = intermediaryRegistration.etmpDisplayRegistration.schemeDetails.businessEmailId,
            bankDetails = intermediaryRegistration.etmpDisplayRegistration.bankDetails
          )

        case _ => None
      }
    } else {
      None.toFuture
    }
  }

  private def createCompositeAccount(
                                      tradingNames: Seq[EtmpTradingName],
                                      fullName: String,
                                      telephoneNumber: String,
                                      emailAddress: String,
                                      bankDetails: BankDetails
                                    ): Option[CompositeAccount] = {

    Some(CompositeAccount(
      tradingNames = tradingNames,
      contactDetails = BusinessContactDetails(
        fullName = fullName,
        telephoneNumber = telephoneNumber,
        emailAddress = emailAddress
      ),
      bankDetails = bankDetails
    ))
  }

  private def getLatestIossRegistration(iossNumber: Option[String])(implicit hc: HeaderCarrier): Future[Option[IossEtmpDisplayRegistration]] = {
    iossNumber match {
      case Some(iossNumber) =>
        registrationConnector.getIossRegistration(iossNumber).map {
          case Right(iossEtmpDisplayRegistration) => Some(iossEtmpDisplayRegistration)
          case Left(_) => None
        }

      case _ => None.toFuture
    }
  }

  private def getIntermediaryRegistration(enrolments: Enrolments)(implicit hc: HeaderCarrier): Future[Option[IntermediaryRegistrationWrapper]] = {
    getIntermediaryEnrolment(enrolments, frontendAppConfig.intermediaryEnrolment, "IntNumber") match {
      case Some(intermediaryNumber) =>
        registrationConnector.getIntermediaryRegistration(intermediaryNumber).map {
          case Right(intermediaryRegistrationWrapper) =>
            Some(intermediaryRegistrationWrapper)

          case Left(_) =>
            None
        }

      case _ => None.toFuture
    }
  }

  private def getIntermediaryEnrolment(
                                        enrolments: Enrolments,
                                        key: String,
                                        identifierKey: String
                                      ): Option[String] = {
    enrolments.getEnrolment(key)
      .flatMap(_.identifiers
        .find(_.key == identifierKey)
        .map(_.value)
      )
  }
}