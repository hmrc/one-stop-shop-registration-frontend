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

package controllers.amend

import base.SpecBase
import cats.data.NonEmptyChain
import cats.data.Validated.{Invalid, Valid}
import connectors.RegistrationConnector
import controllers.amend.{routes => amendRoutes}
import controllers.routes
import models.{BusinessContactDetails, CheckMode, DataMissingError, Index, NormalMode, PreviousScheme, PreviousSchemeType}
import models.audit.{RegistrationAuditModel, SubmissionResult}
import models.emails.EmailSendingResult.EMAIL_ACCEPTED
import models.requests.AuthenticatedDataRequest
import models.responses.{ConflictFound, UnexpectedResponseStatus}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages._
import pages.euDetails.{EuCountryPage, EuTaxReferencePage, TaxRegisteredInEuPage}
import pages.previousRegistrations.{PreviousEuCountryPage, PreviouslyRegisteredPage, PreviousSchemePage, PreviousSchemeTypePage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.mvc.AnyContent
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.{running, _}
import queries.EmailConfirmationQuery
import repositories.AuthenticatedUserAnswersRepository
import services._
import testutils.RegistrationData
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.govuk.SummaryListFluency
import views.html.amend.ChangeYourRegistrationView

import java.time.LocalDate
import scala.concurrent.Future

class StartAmendJourneyControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val request = AuthenticatedDataRequest(FakeRequest("GET", "/"), testCredentials, vrn, emptyUserAnswers)
  private implicit val dataRequest: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest(request, testCredentials, vrn, emptyUserAnswers)

  private val registration = RegistrationData.registration

  private val mockRegistrationConnector = mock[RegistrationConnector]
  private val mockRegistrationService = mock[RegistrationService]
  private val mockAuthenticatedUserAnswersRepository = mock[AuthenticatedUserAnswersRepository]

  override def beforeEach(): Unit = {
    Mockito.reset(mockRegistrationConnector)
    Mockito.reset(mockRegistrationService)
    Mockito.reset(mockAuthenticatedUserAnswersRepository)
  }

  "Start Amend Controller" - {

    "GET" - {
      "must set user answers from registration and redirect to change your registration" in {

        when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
        when(mockRegistrationConnector.getVatCustomerInfo()(any())) thenReturn Future.successful(Right(vatCustomerInfo))
        when(mockRegistrationService.toUserAnswers(any(), any(), any())) thenReturn Future.successful(completeUserAnswers)
        when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .overrides(bind[RegistrationService].toInstance(mockRegistrationService))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockAuthenticatedUserAnswersRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, amendRoutes.StartAmendJourneyController.onPageLoad().url)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual amendRoutes.ChangeYourRegistrationController.onPageLoad().url
        }
      }

    }

  }
}
