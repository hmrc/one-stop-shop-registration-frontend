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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import connectors.RegistrationConnector
import formats.Format.dateFormatter
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testutils.RegistrationData.registration
import views.html.AlreadyRegisteredView

import scala.concurrent.Future

class AlreadyRegisteredControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockConnector = mock[RegistrationConnector]

  override def beforeEach(): Unit = {
    Mockito.reset(mockConnector)
  }

  "AlreadyRegistered Controller" - {

    "when the connector returns a registration" - {

      "must return OK and the correct view for a GET" in {

        when(mockConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[RegistrationConnector].toInstance(mockConnector))
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.AlreadyRegisteredController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AlreadyRegisteredView]
          val config = application.injector.instanceOf[FrontendAppConfig]

          status(result) mustEqual OK

          val expectedContent =
            view(
              registration.registeredCompanyName,
              vrn,
              config.feedbackUrl(request),
              registration.commencementDate.format(dateFormatter)
            )(request, messages(application)).toString

          contentAsString(result) mustEqual expectedContent
        }
      }
    }

    "when the connector does not find an existing registration" - {

      "must redirect the user to the start of the service" in {}

      when(mockConnector.getRegistration()(any())) thenReturn Future.successful(None)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[RegistrationConnector].toInstance(mockConnector))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.AlreadyRegisteredController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url
      }
    }
  }
}