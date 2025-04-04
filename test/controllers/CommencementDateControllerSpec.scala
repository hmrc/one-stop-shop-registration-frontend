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
import controllers.amend.{routes => amendRoutes}
import connectors.RegistrationConnector
import formats.Format.dateFormatter
import models.{AmendMode, CheckMode, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.previousRegistrations.PreviouslyRegisteredPage
import pages.{CommencementDatePage, DateOfFirstSalePage, HasMadeSalesPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{CoreRegistrationValidationService, DateService, RegistrationService}
import testutils.RegistrationData
import views.html.CommencementDateView

import java.time.LocalDate
import scala.concurrent.Future

class CommencementDateControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val dateService: DateService = mock[DateService]
  private val registrationService: RegistrationService = mock[RegistrationService]
  private val coreRegistrationValidationService: CoreRegistrationValidationService = mock[CoreRegistrationValidationService]

  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]

  override def beforeEach(): Unit = {
    reset(dateService)
    reset(registrationService)
    reset(mockRegistrationConnector)
  }

  "CommencementDate Controller" - {

    "when the scheme has started" - {

      "must return OK and the correct view for a GET when user enters date and commencement date is this quarter" in {
        val now = LocalDate.now()
        val nowFormatted = LocalDate.now().format(dateFormatter)
        val dateOfFirstSale = LocalDate.now().withDayOfMonth(1)
        val dateOfLastAmendment = LocalDate.now().plusMonths(1).withDayOfMonth(10)

        val answer1 = basicUserAnswersWithVatInfo.set(HasMadeSalesPage, true).success.value
        val answers = answer1.set(DateOfFirstSalePage, dateOfFirstSale).success.value

        when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(Some(dateOfFirstSale))
        when(dateService.startOfCurrentQuarter) thenReturn now
        when(dateService.lastDayOfCalendarQuarter) thenReturn now
        when(dateService.startOfNextQuarter()) thenReturn now
        when(dateService.calculateFinalAmendmentDate(any())(any())) thenReturn dateOfLastAmendment
        when(coreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn Future.successful(None)
        when(registrationService.isEligibleSalesAmendable(any())(any(), any(), any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .overrides(bind[CoreRegistrationValidationService].toInstance(coreRegistrationValidationService))
            .overrides(bind[RegistrationService].toInstance(registrationService))
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.CommencementDateController.onPageLoad(NormalMode).url)
          val result = route(application, request).value
          val view = application.injector.instanceOf[CommencementDateView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            NormalMode,
            dateOfFirstSale.format(dateFormatter),
            dateOfLastAmendment.format(dateFormatter),
            true,
            Some(nowFormatted),
            Some(nowFormatted),
            Some(nowFormatted),
            true
          )(request, messages(application)).toString
        }
      }

      "must redirect to next page if eligible sales is not amendable" in {
        val now = LocalDate.now()
        val dateOfFirstSale = LocalDate.now().withDayOfMonth(1)
        val dateOfLastAmendment = LocalDate.now().plusMonths(1).withDayOfMonth(10)

        val answer1 = basicUserAnswersWithVatInfo.set(HasMadeSalesPage, true).success.value
        val answers = answer1.set(DateOfFirstSalePage, dateOfFirstSale).success.value

        when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(Some(dateOfFirstSale))
        when(dateService.startOfCurrentQuarter) thenReturn now
        when(dateService.lastDayOfCalendarQuarter) thenReturn now
        when(dateService.startOfNextQuarter()) thenReturn now
        when(dateService.calculateFinalAmendmentDate(any())(any())) thenReturn dateOfLastAmendment
        when(coreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn Future.successful(None)
        when(registrationService.isEligibleSalesAmendable(any())(any(), any(), any())) thenReturn Future.successful(false)

        val application =
          applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .overrides(bind[CoreRegistrationValidationService].toInstance(coreRegistrationValidationService))
            .overrides(bind[RegistrationService].toInstance(registrationService))
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.CommencementDateController.onPageLoad(NormalMode).url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual CommencementDatePage.navigate(NormalMode, answers).url
        }
      }

      "must redirect to Journey Recovery for a GET when DateOfFirstSale is missing" in {
        val now = LocalDate.now()
        val dateOfFirstSale = LocalDate.now().withDayOfMonth(5)

        val answer1 = basicUserAnswersWithVatInfo.set(HasMadeSalesPage, true).success.value

        when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(Some(dateOfFirstSale))
        when(dateService.startOfNextQuarter()) thenReturn arbitraryStartDate
        when(dateService.startOfCurrentQuarter) thenReturn now
        when(dateService.lastDayOfCalendarQuarter) thenReturn now
        when(dateService.startOfNextQuarter()) thenReturn now

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
        val dateOfLastAmendment = LocalDate.now() // TODO

        val answer1 = basicUserAnswersWithVatInfo.set(HasMadeSalesPage, true).success.value
        val answers = answer1.set(DateOfFirstSalePage, dateOfFirstSale).success.value

        when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(Some(commencementDate))
        when(dateService.startOfNextQuarter()) thenReturn arbitraryStartDate
        when(dateService.startOfCurrentQuarter) thenReturn now
        when(dateService.lastDayOfCalendarQuarter) thenReturn now
        when(dateService.startOfNextQuarter()) thenReturn now
        when(dateService.calculateFinalAmendmentDate(any())(any())) thenReturn dateOfLastAmendment

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
            dateOfLastAmendment.format(dateFormatter),
            false,
            Some(nowFormatted),
            Some(nowFormatted),
            Some(nowFormatted),
            true
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view when user answers no to hasMadeSales and no to Previously Registered" in {
        val nowFormatted = LocalDate.now(stubClockAtArbitraryDate).format(dateFormatter)
        val answer1 = basicUserAnswersWithVatInfo.set(HasMadeSalesPage, false).success.value
        val answers = answer1.set(PreviouslyRegisteredPage, false).success.value
        val startDateNow = LocalDate.now(stubClockAtArbitraryDate)
        val dateOfLastAmendment = LocalDate.now(stubClockAtArbitraryDate).minusDays(1)

        when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(Some(startDateNow))
        when(dateService.startOfNextQuarter()) thenReturn startDateNow
        when(dateService.calculateFinalAmendmentDate(any())(any())) thenReturn dateOfLastAmendment

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
            dateOfLastAmendment.format(dateFormatter),
            true,
            None,
            None,
            None,
            false
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view when user answers no to hasMadeSales and yes to previously registered" in {
        val startDateFormatted = arbitraryStartDate.format(dateFormatter)
        val answer1 = basicUserAnswersWithVatInfo.set(HasMadeSalesPage, false).success.value
        val answers = answer1.set(PreviouslyRegisteredPage, true).success.value
        val dateOfLastAmendment = LocalDate.now(stubClockAtArbitraryDate).minusDays(1)

        when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(Some(arbitraryStartDate))
        when(dateService.calculateFinalAmendmentDate(any())(any())) thenReturn dateOfLastAmendment
        when(dateService.startOfNextQuarter()) thenReturn arbitraryStartDate

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
            startDateFormatted,
            dateOfLastAmendment.format(dateFormatter),
            true,
            None,
            None,
            None,
            false
          )(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery when user answers are empty" in {

        when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(Some(arbitraryStartDate))
        when(dateService.startOfNextQuarter()) thenReturn arbitraryStartDate

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

      "in AmendMode" - {

        "must redirect to Amend Journey Recovery for a GET when DateOfFirstSale is missing" in {

          val now = LocalDate.now()
          val dateOfFirstSale = LocalDate.now().withDayOfMonth(5)

          val answer1 = basicUserAnswersWithVatInfo.set(HasMadeSalesPage, true).success.value

          when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(RegistrationData.registration))
          when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(Some(dateOfFirstSale))
          when(dateService.startOfNextQuarter()) thenReturn arbitraryStartDate
          when(dateService.startOfCurrentQuarter) thenReturn now
          when(dateService.lastDayOfCalendarQuarter) thenReturn now
          when(dateService.startOfNextQuarter()) thenReturn now
          when(registrationService.isEligibleSalesAmendable(any())(any(), any(), any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(answer1))
              .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
              .overrides(bind[DateService].toInstance(dateService))
              .overrides(bind[CoreRegistrationValidationService].toInstance(coreRegistrationValidationService))
              .overrides(bind[RegistrationService].toInstance(registrationService))
              .build()

          running(application) {
            val request = FakeRequest(GET, routes.CommencementDateController.onPageLoad(AmendMode).url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustBe amendRoutes.AmendJourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to Amend Journey Recovery when user answers are empty" in {

          when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(RegistrationData.registration))
          when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(Some(arbitraryStartDate))
          when(dateService.startOfNextQuarter()) thenReturn arbitraryStartDate
          when(registrationService.isEligibleSalesAmendable(any())(any(), any(), any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
              .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
              .overrides(bind[DateService].toInstance(dateService))
              .overrides(bind[CoreRegistrationValidationService].toInstance(coreRegistrationValidationService))
              .overrides(bind[RegistrationService].toInstance(registrationService))
              .build()

          running(application) {
            val request = FakeRequest(GET, routes.CommencementDateController.onPageLoad(AmendMode).url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustBe amendRoutes.AmendJourneyRecoveryController.onPageLoad().url
          }
        }

      }

    }

    "must redirect to correct page for POST in Normal Mode" in {
      val now = LocalDate.now()
      val dateOfFirstSale = LocalDate.now().withDayOfMonth(5)

      val answer1 = basicUserAnswersWithVatInfo.set(HasMadeSalesPage, true).success.value
      val answers = answer1.set(DateOfFirstSalePage, dateOfFirstSale).success.value

      when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(Some(dateOfFirstSale))
      when(dateService.startOfNextQuarter()) thenReturn arbitraryStartDate
      when(dateService.startOfCurrentQuarter) thenReturn now
      when(dateService.lastDayOfCalendarQuarter) thenReturn now
      when(dateService.startOfNextQuarter()) thenReturn now

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

      when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(Some(dateOfFirstSale))
      when(dateService.startOfNextQuarter()) thenReturn arbitraryStartDate
      when(dateService.startOfCurrentQuarter) thenReturn now
      when(dateService.lastDayOfCalendarQuarter) thenReturn now
      when(dateService.startOfNextQuarter()) thenReturn now

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