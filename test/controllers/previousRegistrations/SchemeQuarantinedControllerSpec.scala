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

package controllers.previousRegistrations

import base.SpecBase
import controllers.previousRegistrations.{routes => prevRoutes}
import controllers.amend.{routes => amendRoutes}
import models.domain.PreviousSchemeNumbers
import models.{AmendMode, Country, Index, NormalMode, PreviousScheme, PreviousSchemeType, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{times, verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.previousRegistrations._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.previousRegistration.PreviousSchemeForCountryQuery
import repositories.AuthenticatedUserAnswersRepository
import views.html.previousRegistrations.SchemeQuarantinedView

import scala.concurrent.Future

class SchemeQuarantinedControllerSpec extends SpecBase {

  private val country: Country = Country.euCountries.head

  private val index: Index = Index(0)
  private val index1: Index = Index(1)

  private val answers: UserAnswers = basicUserAnswersWithVatInfo
    .set(PreviouslyRegisteredPage, true).success.value
    .set(PreviousEuCountryPage(index), country).success.value
    .set(PreviousSchemePage(index, index), PreviousScheme.OSSU).success.value
    .set(PreviousSchemeTypePage(index, index), PreviousSchemeType.OSS).success.value
    .set(PreviousOssNumberPage(index, index), PreviousSchemeNumbers(s"${country.code}123", None)).success.value

  "SchemeQuarantinedController Controller" - {

    ".onPageLoad" - {

      "must return OK and the correct view for a GET in NormalMode" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, prevRoutes.SchemeQuarantinedController.onPageLoad(NormalMode, index, index).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SchemeQuarantinedView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(NormalMode, index, index)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET in AmendMode" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), mode = Some(AmendMode)).build()

        running(application) {
          val request = FakeRequest(GET, prevRoutes.SchemeQuarantinedController.onPageLoad(AmendMode, index, index).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[SchemeQuarantinedView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(AmendMode, index, index)(request, messages(application)).toString
        }
      }

    }

    ".deleteAndRedirect" - {

      "must delete the scheme for the country and redirect to Change Your Registration" in {

        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.previousRegistrations.routes.SchemeQuarantinedController.deleteAndRedirect(index, index).url)

          val result = route(application, request).value

          val expectedAnswers = answers.remove(PreviousSchemeForCountryQuery(index, index)).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe amendRoutes.ChangeYourRegistrationController.onPageLoad().url
          verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))

        }
      }

      "must delete the correct scheme when there is more than one for the country and redirect to Change Your Registration" in {

        val additionalAnswers = answers
          .set(PreviousSchemePage(index, index1), PreviousScheme.OSSNU).success.value
          .set(PreviousSchemeTypePage(index, index1), PreviousSchemeType.OSS).success.value
          .set(PreviousOssNumberPage(index, index), PreviousSchemeNumbers(s"${country.code}234", None)).success.value

        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(additionalAnswers))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.previousRegistrations.routes.SchemeQuarantinedController.deleteAndRedirect(index, index).url)

          val result = route(application, request).value

          val expectedAnswers = additionalAnswers.remove(PreviousSchemeForCountryQuery(index, index)).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe amendRoutes.ChangeYourRegistrationController.onPageLoad().url
          verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))

        }
      }

      "must redirect to Amend Journey Recovery if no answers present" in {

        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = None)
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.previousRegistrations.routes.SchemeQuarantinedController.deleteAndRedirect(index, index).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe amendRoutes.AmendJourneyRecoveryController.onPageLoad().url
          verifyNoInteractions(mockSessionRepository)

        }
      }

    }
  }
}
