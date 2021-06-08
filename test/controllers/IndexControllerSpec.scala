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
import connectors.RegistrationConnector
import models.{NormalMode, UserAnswers, responses}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.FirstAuthedPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockConnector  = mock[RegistrationConnector]
  private val mockRepository = mock[SessionRepository]

  private def appBuilder(answers: Option[UserAnswers]) =
    applicationBuilder(answers)
      .overrides(
        bind[RegistrationConnector].toInstance(mockConnector),
        bind[SessionRepository].toInstance(mockRepository)
      )

  override def beforeEach(): Unit = {
    Mockito.reset(mockConnector, mockRepository)
  }

  "Index Controller" - {

    "when we already have some user answers" - {

      "must redirect to the next page without makings calls to get data or updating the user answers" in {

        val application = appBuilder(Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual FirstAuthedPage.navigate(NormalMode, emptyUserAnswers).url
          verify(mockConnector, never()).getVatCustomerInfo()(any())
          verify(mockRepository, never()).set(any())
        }
      }
    }

    "when we don't already have user answers" - {

      "and we can find their VAT details" - {

        "must create user answers with their VAT details, then redirect to the next page" in {

          val application = appBuilder(None).build()

          when(mockConnector.getVatCustomerInfo()(any())) thenReturn Future.successful(Right(vatCustomerInfo))
          when(mockRepository.set(any())) thenReturn Future.successful(true)

          running(application) {

            val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual FirstAuthedPage.navigate(NormalMode, emptyUserAnswersWithVatInfo).url
            verify(mockRepository, times(1)).set(eqTo(emptyUserAnswersWithVatInfo))
          }
        }
      }

      "and we cannot find their VAT details" - {

        "must create user answers with no VAT details, then redirect to the next page" in {

          val application = appBuilder(None).build()

          when(mockConnector.getVatCustomerInfo()(any())) thenReturn Future.successful(Left(responses.NotFound))
          when(mockRepository.set(any())) thenReturn Future.successful(true)

          running(application) {

            val request = FakeRequest(GET, routes.IndexController.onPageLoad().url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual FirstAuthedPage.navigate(NormalMode, emptyUserAnswers).url
            verify(mockRepository, times(1)).set(eqTo(emptyUserAnswers))
          }
        }
      }
    }
  }
}
