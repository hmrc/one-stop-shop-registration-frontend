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

import akka.http.impl.util.JavaAccessors.HttpResponse
import base.SpecBase
import forms.HasTradingNameFormProvider
import models.RegistrationResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, when}
import pages.RegisteredCompanyNamePage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.RegistrationService
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

import javax.inject.Inject
import scala.concurrent.Future.successful

class CheckYourAnswersControllerSpec @Inject()(
  mockRegistrationService: RegistrationService
) extends SpecBase with SummaryListFluency {

  private val mockRegistrationResponse = mock[RegistrationResponse]

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to 'Application Complete' page on valid submission" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      when(mockRegistrationService.submit(any())(any(),any())).thenReturn(successful(mockRegistrationResponse))

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ApplicationCompleteController.onPageLoad().url
    }



  }
}
