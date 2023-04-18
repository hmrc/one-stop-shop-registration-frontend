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
import formats.Format.dateFormatter
import models.{CheckMode, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.reset
import org.mockito.MockitoSugar.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.{CommencementDatePage, DateOfFirstSalePage, HasMadeSalesPage, IsPlanningFirstEligibleSalePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{CoreRegistrationValidationService, DateService}
import views.html.CommencementDateView

import java.time.LocalDate
import scala.concurrent.Future

class CommencementDateControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

 private val dateService: DateService = mock[DateService]
 private val coreRegistrationValidationService: CoreRegistrationValidationService = mock[CoreRegistrationValidationService]

  override def beforeEach(): Unit = {
    reset(dateService)
  }

  "CommencementDate Controller" - {

    "when the scheme has started" - {

      "must return OK and the correct view for a GET when user enters date and commencement date is this quarter" in {
        val now = LocalDate.now()
        val nowFormatted = LocalDate.now().format(dateFormatter)
        val dateOfFirstSale = LocalDate.now().withDayOfMonth(5)

        val answer1 = basicUserAnswersWithVatInfo.set(HasMadeSalesPage, true).success.value
        val answers = answer1.set(DateOfFirstSalePage, dateOfFirstSale).success.value

        when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(dateOfFirstSale)
        when(dateService.startOfCurrentQuarter) thenReturn now
        when(dateService.lastDayOfCalendarQuarter) thenReturn now
        when(dateService.startOfNextQuarter) thenReturn now
        when(coreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn Future.successful(None)

        val application =
          applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .overrides(bind[CoreRegistrationValidationService].toInstance(coreRegistrationValidationService))
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.CommencementDateController.onPageLoad(NormalMode).url)
          val result = route(application, request).value
          val view = application.injector.instanceOf[CommencementDateView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            NormalMode,
            dateOfFirstSale.format(dateFormatter),
            true,
            Some(nowFormatted),
            Some(nowFormatted),
            Some(nowFormatted)
          )(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery for a GET when DateOfFirstSale is missing" in {
        val now = LocalDate.now()
        val dateOfFirstSale = LocalDate.now().withDayOfMonth(5)

        val answer1 = basicUserAnswersWithVatInfo.set(HasMadeSalesPage, true).success.value

        when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(dateOfFirstSale)
        when(dateService.startOfNextQuarter) thenReturn arbitraryStartDate
        when(dateService.startOfCurrentQuarter) thenReturn now
        when(dateService.lastDayOfCalendarQuarter) thenReturn now
        when(dateService.startOfNextQuarter)thenReturn now

        val application =
          applicationBuilder(userAnswers = Some(answer1))
            .overrides(bind[DateService].toInstance(dateService))
            .overrides(bind[CoreRegistrationValidationService].toInstance(coreRegistrationValidationService))
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.CommencementDateController.onPageLoad(NormalMode).url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must return OK and the correct view for a GET when user enters date and commencement date is next quarter" in {
        val now = LocalDate.now()
        val nowFormatted = LocalDate.now().format(dateFormatter)
        val dateOfFirstSale = LocalDate.now().minusMonths(2)
        val commencementDate = now.plusMonths(2)

        val answer1 = basicUserAnswersWithVatInfo.set(HasMadeSalesPage, true).success.value
        val answers = answer1.set(DateOfFirstSalePage, dateOfFirstSale).success.value

        when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(commencementDate)
        when(dateService.startOfNextQuarter) thenReturn arbitraryStartDate
        when(dateService.startOfCurrentQuarter) thenReturn now
        when(dateService.lastDayOfCalendarQuarter) thenReturn now
        when(dateService.startOfNextQuarter)thenReturn now

        val application =
          applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .overrides(bind[CoreRegistrationValidationService].toInstance(coreRegistrationValidationService))
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.CommencementDateController.onPageLoad(NormalMode).url)
          val result = route(application, request).value
          val view = application.injector.instanceOf[CommencementDateView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            NormalMode,
            commencementDate.format(dateFormatter),
            false,
            Some(nowFormatted),
            Some(nowFormatted),
            Some(nowFormatted)
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view when user answers no to hasMadeSales and yes to Is Planning First Eligible Sale" in {
        val nowFormatted = LocalDate.now(stubClockAtArbitraryDate).format(dateFormatter)
        val answer1 = basicUserAnswersWithVatInfo.set(HasMadeSalesPage, false).success.value
        val answers = answer1.set(IsPlanningFirstEligibleSalePage, true).success.value
        val startDateNow = LocalDate.now(stubClockAtArbitraryDate)

        when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(startDateNow)
        when(dateService.startOfNextQuarter) thenReturn startDateNow

        val application =
          applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .overrides(bind[CoreRegistrationValidationService].toInstance(coreRegistrationValidationService))
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.CommencementDateController.onPageLoad(NormalMode).url)
          val result = route(application, request).value
          val view = application.injector.instanceOf[CommencementDateView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            NormalMode,
            nowFormatted,
            true,
            None,
            None,
            None
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view when user answers no to hasMadeSales and no to Is Planning First Eligible Sale" in {
        val answer1 = basicUserAnswersWithVatInfo.set(HasMadeSalesPage, false).success.value
        val answers = answer1.set(IsPlanningFirstEligibleSalePage, false).success.value

        when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(arbitraryStartDate)
        when(dateService.startOfNextQuarter) thenReturn arbitraryStartDate

        val application =
          applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .overrides(bind[CoreRegistrationValidationService].toInstance(coreRegistrationValidationService))
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.CommencementDateController.onPageLoad(NormalMode).url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
        }
      }

      "must redirect to Journey Recovery when user answers no to hasMadeSales and Is Planning First Eligible Sale is empty" in {
        val answer1 = basicUserAnswersWithVatInfo.set(HasMadeSalesPage, false).success.value

        when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(arbitraryStartDate)
        when(dateService.startOfNextQuarter) thenReturn arbitraryStartDate

        val application =
          applicationBuilder(userAnswers = Some(answer1))
            .overrides(bind[DateService].toInstance(dateService))
            .overrides(bind[CoreRegistrationValidationService].toInstance(coreRegistrationValidationService))
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.CommencementDateController.onPageLoad(NormalMode).url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

      "must redirect to Journey Recovery when user answers are empty" in {

        when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(arbitraryStartDate)
        when(dateService.startOfNextQuarter) thenReturn arbitraryStartDate

        val application =
          applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
            .overrides(bind[DateService].toInstance(dateService))
            .overrides(bind[CoreRegistrationValidationService].toInstance(coreRegistrationValidationService))
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.CommencementDateController.onPageLoad(NormalMode).url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url
        }
      }

    }

    "must redirect to correct page for POST in Normal Mode" in {
      val now = LocalDate.now()
      val dateOfFirstSale = LocalDate.now().withDayOfMonth(5)

      val answer1 = basicUserAnswersWithVatInfo.set(HasMadeSalesPage, true).success.value
      val answers = answer1.set(DateOfFirstSalePage, dateOfFirstSale).success.value

      when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(dateOfFirstSale)
      when(dateService.startOfNextQuarter) thenReturn arbitraryStartDate
      when(dateService.startOfCurrentQuarter) thenReturn now
      when(dateService.lastDayOfCalendarQuarter) thenReturn now
      when(dateService.startOfNextQuarter)thenReturn now

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[DateService].toInstance(dateService))
          .build()

      running(application) {
        val request = FakeRequest(POST, routes.CommencementDateController.onPageLoad(NormalMode).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual CommencementDatePage.navigate(NormalMode, answers).url
      }
    }

    "must redirect to correct page for POST in Check Mode" in {
      val now = LocalDate.now()
      val dateOfFirstSale = LocalDate.now().withDayOfMonth(5)

      val answer1 = basicUserAnswersWithVatInfo.set(HasMadeSalesPage, true).success.value
      val answers = answer1.set(DateOfFirstSalePage, dateOfFirstSale).success.value

      when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(dateOfFirstSale)
      when(dateService.startOfNextQuarter) thenReturn arbitraryStartDate
      when(dateService.startOfCurrentQuarter) thenReturn now
      when(dateService.lastDayOfCalendarQuarter) thenReturn now
      when(dateService.startOfNextQuarter)thenReturn now

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[DateService].toInstance(dateService))
          .build()

      running(application) {
        val request = FakeRequest(POST, routes.CommencementDateController.onPageLoad(CheckMode).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual CommencementDatePage.navigate(CheckMode, answers).url
      }
    }

  }
}