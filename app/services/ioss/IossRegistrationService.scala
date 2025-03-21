/*
 * Copyright 2025 HM Revenue & Customs
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

package services.ioss

import connectors.RegistrationConnector
import models.iossRegistration.IossEtmpDisplayRegistration
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureSyntax.FutureOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IossRegistrationService @Inject()(
                                         registrationConnector: RegistrationConnector
                                       )(implicit ec: ExecutionContext) {

  def getIossRegistration(iossNumber: Option[String])(implicit hc: HeaderCarrier): Future[Option[IossEtmpDisplayRegistration]] = {
    iossNumber match {
      case Some(iossNumber) =>
        registrationConnector.getIossRegistration(iossNumber).map {
          case Right(iossEtmpDisplayRegistration) => Some(iossEtmpDisplayRegistration)
          case Left(_) => None
        }

      case _ => None.toFuture
    }
  }
}
