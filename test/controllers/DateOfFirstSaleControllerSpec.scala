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

package controllers

import base.SpecBase
import formats.Format.{dateFormatter, dateHintFormatter}
import forms.DateOfFirstSaleFormProvider
import models.requests.AuthenticatedDataRequest
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.DateOfFirstSalePage
import play.api.inject.bind
import play.api.mvc.{AnyContent, AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.AuthenticatedUserAnswersRepository
import services.{CoreRegistrationValidationService, DateService}
import utils.FutureSyntax.FutureOps
import views.html.DateOfFirstSaleView

import java.time.LocalDate

class DateOfFirstSaleControllerSpec extends SpecBase with MockitoSugar {

  private val coreRegistrationValidationService: CoreRegistrationValidationService = mock[CoreRegistrationValidationService]

  private val date: LocalDate = LocalDate.now(stubClockAtArbitraryDate)
  private val dateService = new DateService(stubClockAtArbitraryDate, coreRegistrationValidationService)
  private val dateFormatted = dateService.earliestSaleAllowed().format(dateFormatter)
  private val dateHintFormatted = dateService.earliestSaleAllowed().format(dateHintFormatter)

  private implicit lazy val dataRequest: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest(getRequest, testCredentials, vrn, None, emptyUserAnswers, None, 0, None)

  private val formProvider = new DateOfFirstSaleFormProvider(dateService, stubClockAtArbitraryDate)

  private lazy val dateOfFirstSaleRoute = routes.DateOfFirstSaleController.onPageLoad(NormalMode).url

  private def getRequest: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, dateOfFirstSaleRoute)

  private def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, dateOfFirstSaleRoute)
      .withFormUrlEncodedBody(
        "value.day" -> date.getDayOfMonth.toString,
        "value.month" -> date.getMonthValue.toString,
        "value.year" -> date.getYear.toString
      )

  "DateOfFirstSale Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
        .build()

      running(application) {
        val result = route(application, getRequest).value

        val view = application.injector.instanceOf[DateOfFirstSaleView]

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(formProvider(), NormalMode, dateFormatted, dateHintFormatted)(getRequest, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(DateOfFirstSalePage, date).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val view = application.injector.instanceOf[DateOfFirstSaleView]

        val result = route(application, getRequest).value

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(formProvider().fill(date), NormalMode, dateFormatted, dateHintFormatted)(getRequest, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn true.toFuture

      val application =
        applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
          .overrides(
            bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository),
            bind[DateService].toInstance(dateService)
          )
          .build()

      running(application) {
        val result = route(application, postRequest()).value
        val expectedAnswers = basicUserAnswersWithVatInfo.set(DateOfFirstSalePage, date).success.value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` DateOfFirstSalePage.navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      val request =
        FakeRequest(POST, dateOfFirstSaleRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = formProvider().bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[DateOfFirstSaleView]

        val result = route(application, request).value

        status(result) `mustBe` BAD_REQUEST
        contentAsString(result) `mustBe` view(boundForm, NormalMode, dateFormatted, dateHintFormatted)(request, messages(application)).toString
      }
    }
  }
}
