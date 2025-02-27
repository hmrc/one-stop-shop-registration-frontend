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
import models.Mode
import models.requests.AuthenticatedIdentifierRequest
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.Result
import services.DataMigrationService
import services.ioss.IossExclusionService

import scala.concurrent.{ExecutionContext, Future}


class FakeCheckRegistrationFilter() extends CheckRegistrationFilterImpl(
  mock[Option[Mode]],
  mock[RegistrationConnector],
  mock[FrontendAppConfig],
  mock[DataMigrationService],
  mock[IossExclusionService]
)(ExecutionContext.Implicits.global) {

  override protected def filter[A](request: AuthenticatedIdentifierRequest[A]): Future[Option[Result]] = {
    Future.successful(None)
  }

}

class FakeCheckRegistrationFilterProvider()
  extends CheckRegistrationFilterProvider(
    mock[RegistrationConnector],
    mock[FrontendAppConfig],
    mock[DataMigrationService],
    mock[IossExclusionService]
  )(ExecutionContext.Implicits.global) {

  override def apply(mode: Option[Mode]): CheckRegistrationFilterImpl = new FakeCheckRegistrationFilter()

}
