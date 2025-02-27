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

package controllers.actions

import models.UserAnswers
import models.requests.{AuthenticatedDataRequest, AuthenticatedMandatoryDataRequest}
import play.api.libs.json.JsObject
import play.api.mvc.Result
import testutils.RegistrationData
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.domain.Vrn
import utils.FutureSyntax.FutureOps

import scala.concurrent.{ExecutionContext, Future}

class FakeOssRequiredActionImpl extends OssRequiredActionImpl()(ExecutionContext.Implicits.global) {

  override protected def refine[A](request: AuthenticatedDataRequest[A]): Future[Either[Result, AuthenticatedMandatoryDataRequest[A]]] = {
    Right(
      AuthenticatedMandatoryDataRequest(
        request = request,
        credentials = Credentials("12345-credId", "GGW"),
        vrn = Vrn("123456789"),
        registration = RegistrationData.registration,
        userAnswers = UserAnswers(id = "12345-credId", data = JsObject.empty)
      )
    ).toFuture
  }
}

class FakeOssRequiredAction extends OssRequiredAction()(ExecutionContext.Implicits.global) {
  
  override def apply(): OssRequiredActionImpl = new FakeOssRequiredActionImpl()
}
