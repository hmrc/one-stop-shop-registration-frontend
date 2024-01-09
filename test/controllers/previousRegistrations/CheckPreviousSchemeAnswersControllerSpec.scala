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

package controllers.previousRegistrations

import base.SpecBase
import controllers.routes
import controllers.amend.{routes => amendRoutes}
import connectors.RegistrationConnector
import forms.previousRegistrations.CheckPreviousSchemeAnswersFormProvider
import models.domain.PreviousSchemeNumbers
import models.{AmendMode, Country, Index, NormalMode, PreviousScheme}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.previousRegistrations._
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import testutils.RegistrationData
import viewmodels.checkAnswers.previousRegistrations._
import viewmodels.govuk.SummaryListFluency
import views.html.previousRegistrations.CheckPreviousSchemeAnswersView

import scala.concurrent.Future

class CheckPreviousSchemeAnswersControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar with BeforeAndAfterEach {


  private val index = Index(0)
  private val country = Country.euCountries.head
  private val formProvider = new CheckPreviousSchemeAnswersFormProvider()
  private val form = formProvider(country)
  private val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]
  private val mockRegistrationConnector = mock[RegistrationConnector]

  private val baseUserAnswers =
    basicUserAnswersWithVatInfo
      .set(PreviouslyRegisteredPage, true).success.value
      .set(PreviousEuCountryPage(index), country).success.value
      .set(PreviousSchemePage(index, index), PreviousScheme.values.head).success.value
      .set(PreviousOssNumberPage(index, index), PreviousSchemeNumbers("123456789", None)).success.value

  override def beforeEach(): Unit = {
    Mockito.reset(mockSessionRepository)
  }

  "CheckPreviousSchemeAnswersController" - {

    "must return OK and the correct view for a GET when answers are complete" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)
        val request = FakeRequest(GET, controllers.previousRegistrations.routes.CheckPreviousSchemeAnswersController.onPageLoad(NormalMode, index).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckPreviousSchemeAnswersView]
        val lists = Seq(SummaryListViewModel(
          Seq(
            PreviousSchemeSummary.row(baseUserAnswers, index, index, country, Seq.empty, NormalMode),
            PreviousSchemeNumberSummary.row(baseUserAnswers, index, index)
          ).flatten
        ))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, lists, index, country, canAddScheme = true)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when there are existing previous schemes in Amend mode" in {

      when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(RegistrationData.registration))

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers), mode = Some(AmendMode))
        .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
        .build()

      val previousSchemes = Seq(PreviousScheme.OSSNU)

      running(application) {
        implicit val msgs: Messages = messages(application)
        val request = FakeRequest(GET, controllers.previousRegistrations.routes.CheckPreviousSchemeAnswersController.onPageLoad(AmendMode, index).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckPreviousSchemeAnswersView]
        val lists = Seq(SummaryListViewModel(
          Seq(
            PreviousSchemeSummary.row(baseUserAnswers, index, index, country, previousSchemes, AmendMode),
            PreviousSchemeNumberSummary.row(baseUserAnswers, index, index)
          ).flatten
        ))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, AmendMode, lists, index, country, canAddScheme = true)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery if user answers are empty" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.previousRegistrations.routes.CheckPreviousSchemeAnswersController.onPageLoad(NormalMode, index).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery if user answers are empty in AmendMode" in {

      when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(RegistrationData.registration))

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo),  mode = Some(AmendMode))
        .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.previousRegistrations.routes.CheckPreviousSchemeAnswersController.onPageLoad(AmendMode, index).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual amendRoutes.AmendJourneyRecoveryController.onPageLoad().url
      }
    }

    "on a POST" - {

      "must save the answer and redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(baseUserAnswers))
            .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, controllers.previousRegistrations.routes.CheckPreviousSchemeAnswersController.onSubmit(NormalMode, index).url)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value
          val expectedAnswers = baseUserAnswers.set(CheckPreviousSchemeAnswersPage(index), true).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual CheckPreviousSchemeAnswersPage(index).navigate(NormalMode, expectedAnswers).url
          verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, controllers.previousRegistrations.routes.CheckPreviousSchemeAnswersController.onSubmit(NormalMode, index).url)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[CheckPreviousSchemeAnswersView]
          implicit val msgs: Messages = messages(application)

          val list = Seq(SummaryListViewModel(
            Seq(
              PreviousSchemeSummary.row(baseUserAnswers, index, index, country, Seq.empty, NormalMode),
              PreviousSchemeNumberSummary.row(baseUserAnswers, index, index)
            ).flatten
          ))

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, list, index, country, canAddScheme = true)(request, implicitly).toString
        }
      }

      "must redirect to Journey Recovery if user answers are empty" in {

        val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

        running(application) {
          val request =
            FakeRequest(POST, controllers.previousRegistrations.routes.CheckPreviousSchemeAnswersController.onSubmit(NormalMode, index).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        }
      }

      "must redirect to Journey Recovery if user answers are empty in AmendMode" in {

        when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(RegistrationData.registration))

        val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .build()

        running(application) {
          val request =
            FakeRequest(POST, controllers.previousRegistrations.routes.CheckPreviousSchemeAnswersController.onSubmit(AmendMode, index).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual amendRoutes.AmendJourneyRecoveryController.onPageLoad().url

        }
      }

    }
  }

}
