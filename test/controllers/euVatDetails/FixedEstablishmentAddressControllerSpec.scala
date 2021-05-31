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

package controllers.euVatDetails

import base.SpecBase
import forms.euVatDetails.FixedEstablishmentAddressFormProvider
import models.euVatDetails.FixedEstablishmentAddress
import models.{Country, Index, NormalMode}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.euVatDetails.{EuCountryPage, FixedEstablishmentAddressPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.euVatDetails.FixedEstablishmentAddressView

import scala.concurrent.Future

class FixedEstablishmentAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  private val country = Country.euCountries.head
  val formProvider = new FixedEstablishmentAddressFormProvider()
  val form = formProvider()

  private val index = Index(0)
  lazy val fixedEstablishmentAddressRoute = routes.FixedEstablishmentAddressController.onPageLoad(NormalMode, index).url

  private val address         = FixedEstablishmentAddress("value 1", None, "value 2", None, None)
  private val baseUserAnswers = emptyUserAnswers.set(EuCountryPage(index), country).success.value
  private val userAnswers     = baseUserAnswers.set(FixedEstablishmentAddressPage(index), address).success.value

  "FixedEstablishmentAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, fixedEstablishmentAddressRoute)

        val view = application.injector.instanceOf[FixedEstablishmentAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, index, country)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, fixedEstablishmentAddressRoute)

        val view = application.injector.instanceOf[FixedEstablishmentAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(address), NormalMode, index, country)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, fixedEstablishmentAddressRoute)
            .withFormUrlEncodedBody(("line1", "value 1"), ("townOrCity", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, fixedEstablishmentAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[FixedEstablishmentAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, index, country)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, fixedEstablishmentAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, fixedEstablishmentAddressRoute)
            .withFormUrlEncodedBody(("line1", "value 1"), ("townOrCity", "value 2"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
