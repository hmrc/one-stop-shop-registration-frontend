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
import config.FrontendAppConfig
import connectors.RegistrationConnector
import formats.Format.dateFormatter
import models.{Period, UserAnswers}
import models.Quarter.{Q1, Q4}
import models.external.ExternalEntryUrl
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{BusinessContactDetailsPage, DateOfFirstSalePage, HasMadeSalesPage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{CoreRegistrationValidationService, DateService, PeriodService}
import views.html.rejoin.RejoinCompleteView

import java.time.LocalDate
import scala.concurrent.Future

class RejoinCompleteControllerSpec extends SpecBase with MockitoSugar {

  private val periodService = mock[PeriodService]
  private val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]
  private val mockRegistrationConnector = mock[RegistrationConnector]
  private val mockDateService = mock[DateService]

  private  val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      BusinessContactDetailsPage.toString -> Json.obj(
        "fullName" -> "value 1",
        "telephoneNumber" -> "value 2",
        "emailAddress" -> "test@test.com",
        "websiteAddress" -> "value 4",
      ),
      DateOfFirstSalePage.toString -> Json.toJson(arbitraryStartDate)
    ),
    vatInfo = Some(vatCustomerInfo)
  )

  "must return OK and the correct view when Date Of First Sale is different to the Commencement Date" in {

    when(periodService.getFirstReturnPeriod(any())) thenReturn Period(2022, Q4)
    when(periodService.getNextPeriod(any())) thenReturn Period(2023, Q1)
    when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(LocalDate.of(2021, 10, 1))

    when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn Future.successful(None)

    when(mockRegistrationConnector.getSavedExternalEntry()(any())) thenReturn Future.successful(Right(ExternalEntryUrl(None)))

    val answers = userAnswers.copy()
      .set(DateOfFirstSalePage, LocalDate.of(2021, 7, 1)).success.value

    val application =
      applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
        .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
        .overrides(bind[PeriodService].toInstance(periodService))
        .overrides(bind[DateService].toInstance(mockDateService))
        .build()

    running(application) {
      implicit val msgs: Messages = messages(application)
      val request = FakeRequest(GET, controllers.rejoin.routes.RejoinCompleteController.onPageLoad().url)
      val config = application.injector.instanceOf[FrontendAppConfig]
      val result = route(application, request).value
      val view = application.injector.instanceOf[RejoinCompleteView]
      val commencementDate = LocalDate.of(2021, 10, 1)
      val periodOfFirstReturn = periodService.getFirstReturnPeriod(commencementDate)
      val nextPeriod = periodService.getNextPeriod(periodOfFirstReturn)
      val firstDayOfNextPeriod = nextPeriod.firstDay

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(
        vrn.vrn,
        config.feedbackUrl(request),
        None,
        yourAccountUrl,
        "Company name",
        commencementDate.format(dateFormatter),
        periodOfFirstReturn.displayShortText,
        firstDayOfNextPeriod.format(dateFormatter),
      )(request, messages(application)).toString
    }
  }

  "must return OK and the correct view when there is no Date Of First Sale" in {

    val answers = userAnswers.copy()
      .remove(DateOfFirstSalePage).success.value
      .set(HasMadeSalesPage, false).success.value

    val application =
      applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
        .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
        .overrides(bind[PeriodService].toInstance(periodService))
        .overrides(bind[DateService].toInstance(mockDateService))
        .build()

    when(periodService.getFirstReturnPeriod(any())) thenReturn Period(2022, Q4)
    when(periodService.getNextPeriod(any())) thenReturn Period(2023, Q1)
    when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(LocalDate.now(stubClockAtArbitraryDate))

    when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn Future.successful(None)

    when(mockRegistrationConnector.getSavedExternalEntry()(any())) thenReturn Future.successful(Right(ExternalEntryUrl(None)))

    running(application) {
      implicit val msgs: Messages = messages(application)
      val request = FakeRequest(GET, controllers.rejoin.routes.RejoinCompleteController.onPageLoad().url)
      val config = application.injector.instanceOf[FrontendAppConfig]
      val result = route(application, request).value
      val view = application.injector.instanceOf[RejoinCompleteView]
      val commencementDate = LocalDate.now(stubClockAtArbitraryDate)
      val periodOfFirstReturn = periodService.getFirstReturnPeriod(commencementDate)
      val nextPeriod = periodService.getNextPeriod(periodOfFirstReturn)
      val firstDayOfNextPeriod = nextPeriod.firstDay

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(
        vrn.vrn,
        config.feedbackUrl(request),
        None,
        yourAccountUrl,
        "Company name",
        commencementDate.format(dateFormatter),
        periodOfFirstReturn.displayShortText,
        firstDayOfNextPeriod.format(dateFormatter),
      )(request, messages(application)).toString
    }
  }
}
