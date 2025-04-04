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

package controllers.rejoin

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.UnauthenticatedUserAnswersRepository
import uk.gov.hmrc.http.SessionKeys
import views.html.rejoin.CannotRejoinView

import scala.concurrent.Future

class CannotReverseControllerSpec extends SpecBase with MockitoSugar {

  "CannotRejoinQuarantinedCountry Controller" - {

    "must return OK and the correct view for a GET" in {

      val sessionRepository = mock[UnauthenticatedUserAnswersRepository]
      when(sessionRepository.clear(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
        .overrides(bind[UnauthenticatedUserAnswersRepository].toInstance(sessionRepository))
        .build()

      running(application) {
        val sessionId = "123"

        val request = FakeRequest(GET, routes.CannotReverseController.onPageLoad().url)
          .withSession(SessionKeys.sessionId -> sessionId)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CannotRejoinView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
