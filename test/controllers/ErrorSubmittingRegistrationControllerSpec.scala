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

package controllers

import base.SpecBase
import models.SessionData
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.external.ExternalReturnUrlQuery
import repositories.SessionRepository
import views.html.ErrorSubmittingRegistration

import scala.concurrent.Future

class ErrorSubmittingRegistrationControllerSpec extends SpecBase  with MockitoSugar {

  private val externalUrl = "/test"
  private val sessionRepository = mock[SessionRepository]

  "ErrorSubmittingRegistration Controller" - {

      "must return OK and the correct view with back to your account link if external url is present" in {

        val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
          .overrides(
            inject.bind[SessionRepository].toInstance(sessionRepository)
          )
          .build()

        when(sessionRepository.get(any())) thenReturn Future.successful(Seq(SessionData("id").set(ExternalReturnUrlQuery.path, externalUrl).success.value))

        running(application) {
          val request = FakeRequest(GET, routes.ErrorSubmittingRegistrationController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ErrorSubmittingRegistration]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(Some(externalUrl))(request, messages(application)).toString
        }
      }

      "must return OK and the correct view with no link if external url is not present" in {

        val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

        running(application) {
          val request = FakeRequest(GET, routes.ErrorSubmittingRegistrationController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ErrorSubmittingRegistration]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(None)(request, messages(application)).toString
        }
      }

  }

}
