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
import cats.data.Validated.Valid
import config.FrontendAppConfig
import connectors.RegistrationConnector
import models.audit.{RegistrationAuditModel, RegistrationAuditType, SubmissionResult}
import models.requests.AuthenticatedDataRequest
import models.responses.UnexpectedResponseStatus
import models.{BusinessContactDetails, RejoinMode}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{doNothing, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.{BusinessContactDetailsPage, DateOfFirstSalePage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.AuthenticatedUserAnswersRepository
import services.*
import testutils.RegistrationData
import viewmodels.checkAnswers.{DateOfFirstSaleSummary, HasMadeSalesSummary}
import viewmodels.govuk.SummaryListFluency
import views.html.rejoin.HybridReversalView

import java.time.LocalDate
import scala.concurrent.Future

class HybridReversalControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency with BeforeAndAfterEach {

  private val registration = RegistrationData.registration

  private val registrationValidationService = mock[RegistrationValidationService]
  private val registrationConnector = mock[RegistrationConnector]
  private val rejoinRegistrationService = mock[RejoinRegistrationService]
  private val auditService = mock[AuditService]

  override def beforeEach(): Unit = {
    Mockito.reset(registrationConnector)
    Mockito.reset(registrationValidationService)
    Mockito.reset(rejoinRegistrationService)
    Mockito.reset(auditService)
  }

  "RejoinRegistration Controller" - {

    "GET" - {

      "must redirect to Cannot rejoin if can rejoin is false" in {

        when(rejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn false
        when(rejoinRegistrationService.canReverse(any(), any())) thenReturn false


        val application = applicationBuilder(userAnswers = Some(completeUserAnswers), registration = Some(registration))
          .overrides(bind[RejoinRegistrationService].toInstance(rejoinRegistrationService))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.rejoin.routes.HybridReversalController.onPageLoad().url)

          val result = route(application, request).value

          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` controllers.rejoin.routes.CannotReverseController.onPageLoad().url
        }
      }

      "must return OK and the correct view for a GET" in {

        when(rejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true
        when(rejoinRegistrationService.canReverse(any(), any())) thenReturn true

        val answers = completeUserAnswers.set(DateOfFirstSalePage, LocalDate.now()).success.value

        val application = applicationBuilder(userAnswers = Some(answers), registration = Some(registration))
          .overrides(bind[RejoinRegistrationService].toInstance(rejoinRegistrationService))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.rejoin.routes.HybridReversalController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[HybridReversalView]
          val config = application.injector.instanceOf[FrontendAppConfig]
          implicit val msgs: Messages = messages(application)
          val list = SummaryListViewModel(rows = Seq(
            HasMadeSalesSummary.row(answers, RejoinMode),
            DateOfFirstSaleSummary.row(answers, RejoinMode)
          ).flatten)

          status(result) `mustBe` OK
          contentAsString(result) `mustBe` view(list, RejoinMode, config.ossYourAccountUrl)(request, messages(application)).toString
        }
      }

    }

    "on submit" - {

      "must audit the event and redirect to the next page and successfully" in {

        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn Future.successful(Valid(registration))
        when(registrationConnector.amendRegistration(any())(any())) thenReturn Future.successful(Right(()))
        when(rejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true
        when(rejoinRegistrationService.canReverse(any(), any())) thenReturn true
        doNothing().when(auditService).audit(any())(any(), any())

        val contactDetails = BusinessContactDetails("name", "0111 2223334", "email@example.com")
        val userAnswers = completeUserAnswers.set(BusinessContactDetailsPage, contactDetails).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers), registration = Some(registration))
          .overrides(
            bind[RegistrationValidationService].toInstance(registrationValidationService),
            bind[RegistrationConnector].toInstance(registrationConnector),
            bind[RejoinRegistrationService].toInstance(rejoinRegistrationService),
            bind[AuditService].toInstance(auditService),
            bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository)
          ).build()

        running(application) {
          val request = FakeRequest(POST, controllers.rejoin.routes.HybridReversalController.onSubmit().url)
          val result = route(application, request).value
          val dataRequest = AuthenticatedDataRequest(request, testCredentials, vrn, None, userAnswers)
          val expectedAuditEvent = RegistrationAuditModel.build(RegistrationAuditType.AmendRegistration, registration, SubmissionResult.Success, dataRequest)


          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` controllers.rejoin.routes.RejoinCompleteController.onPageLoad().url
          verify(auditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
        }
      }

      "when the submission fails because of a technical issue" - {

        "the user is redirected to the Error Submitting Rejoin Controller" in {

          val errorResponse = UnexpectedResponseStatus(INTERNAL_SERVER_ERROR, "foo")

          when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn Future.successful(Valid(registration))
          when(registrationConnector.amendRegistration(any())(any())) thenReturn Future.successful(Left(errorResponse))
          when(rejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true
          when(rejoinRegistrationService.canReverse(any(), any())) thenReturn true
          doNothing().when(auditService).audit(any())(any(), any())

          val application = applicationBuilder(userAnswers = Some(completeUserAnswers), registration = Some(registration))
            .overrides(
              bind[RegistrationValidationService].toInstance(registrationValidationService),
              bind[RegistrationConnector].toInstance(registrationConnector),
              bind[RejoinRegistrationService].toInstance(rejoinRegistrationService),
              bind[AuditService].toInstance(auditService)
            ).build()

          running(application) {
            val request = FakeRequest(POST, controllers.rejoin.routes.HybridReversalController.onSubmit().url)
            val result = route(application, request).value
            val dataRequest = AuthenticatedDataRequest(request, testCredentials, vrn, None, basicUserAnswersWithVatInfo)
            val expectedAuditEvent = RegistrationAuditModel.build(RegistrationAuditType.AmendRegistration, registration, SubmissionResult.Failure, dataRequest)


            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` controllers.rejoin.routes.ErrorSubmittingRejoinController.onPageLoad().url
            verify(auditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
          }
        }
      }
    }
  }
}
