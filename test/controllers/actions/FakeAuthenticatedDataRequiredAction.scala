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

import connectors.RegistrationConnector
import models.{AmendMode, Mode, UserAnswers}
import models.requests.{AuthenticatedDataRequest, AuthenticatedOptionalDataRequest}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.Result
import testutils.RegistrationData
import utils.FutureSyntax._

import java.time.{LocalDate, ZoneId}
import scala.concurrent.{ExecutionContext, Future}

class FakeAuthenticatedDataRequiredAction(dataToReturn: Option[UserAnswers], mode: Option[Mode])
  extends AuthenticatedDataRequiredActionImpl(mode, mock[RegistrationConnector])(ExecutionContext.Implicits.global) {

  private val emptyUserAnswers: UserAnswers = UserAnswers("12345-credId", lastUpdated = LocalDate.now.atStartOfDay(ZoneId.systemDefault).toInstant)

  private val registration = RegistrationData.registration

  private val data = dataToReturn match {
    case Some(data) => data
    case _ => emptyUserAnswers
  }
  override protected def refine[A](request: AuthenticatedOptionalDataRequest[A]): Future[Either[Result, AuthenticatedDataRequest[A]]] = {
    mode match {
      case Some(AmendMode) => Right(AuthenticatedDataRequest(request.request, request.credentials, request.vrn, Some(registration), data)).toFuture
      case _ =>  Right(AuthenticatedDataRequest(request.request, request.credentials, request.vrn, None, data)).toFuture

    }
  }
}
