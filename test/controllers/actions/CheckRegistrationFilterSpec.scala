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

package controllers.actions

import base.SpecBase
import config.FrontendAppConfig
import connectors.RegistrationConnector
import controllers.ioss.routes as iossExclusionsRoutes
import controllers.routes
import models.domain.Registration
import models.requests.AuthenticatedIdentifierRequest
import models.{AmendLoopMode, AmendMode, NormalMode, RejoinLoopMode, RejoinMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.DataMigrationService
import services.ioss.IossExclusionService
import testutils.RegistrationData
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.http.HttpResponse
import utils.FutureSyntax.FutureOps

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckRegistrationFilterSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  class Harness(connector: RegistrationConnector, config: FrontendAppConfig, migrationService: DataMigrationService, iossExclusionService: IossExclusionService)
    extends CheckRegistrationFilterImpl(None, connector, config, migrationService, iossExclusionService) {
    def callFilter(request: AuthenticatedIdentifierRequest[_]): Future[Option[Result]] = filter(request)
  }

  private val mockRegistrationConnector = mock[RegistrationConnector]
  private val mockDataMigrationService = mock[DataMigrationService]
  private val mockIossExclusionService = mock[IossExclusionService]
  private val registrationEnrolment = Enrolments(
    Set(
      Enrolment(
        "HMRC-MTD-VAT",
        Seq(EnrolmentIdentifier("VRN", "123456789")),
        "Activated"
      ),
      Enrolment(
        "HMRC-OSS-ORG",
        Seq(EnrolmentIdentifier("VRN", "123456789")),
        "Activated"
      )
    )
  )

  private val registration: Registration = RegistrationData.registration
  private val iossEnrolmentKey: String = "HMRC-IOSS-ORG"
  private val iossNumber: String = "IM9001234567"
  private val iossEnrolment = Enrolment(iossEnrolmentKey, Seq(EnrolmentIdentifier("IOSSNumber", iossNumber)), "test", None)

  private val iossEnrolments: Enrolments = Enrolments(
    Set(
      iossEnrolment
    )
  )

  private val ossAndIossEnrolments: Enrolments =
    Enrolments(registrationEnrolment.enrolments ++ Set(iossEnrolment))

  override def beforeEach(): Unit = {
    Mockito.reset(mockRegistrationConnector)
  }

  ".filter" - {

    "must return None when an existing registration or enrolment is not found" in {

      val app = applicationBuilder(None).build()

      running(app) {
        val config = app.injector.instanceOf[FrontendAppConfig]
        val request = AuthenticatedIdentifierRequest(FakeRequest(), testCredentials, vrn, Enrolments(Set.empty), None)
        val controller = new Harness(mockRegistrationConnector, config, mockDataMigrationService, mockIossExclusionService)

        val result = controller.callFilter(request).futureValue

        result must not be defined
      }
    }

    "must redirect to Already Registered and migrate data when an existing registration is found" in {

      when(mockDataMigrationService.migrate(any(), any())) thenReturn UserAnswers(userAnswersId).toFuture

      val app = applicationBuilder(None)
        .overrides(
          bind[DataMigrationService].toInstance(mockDataMigrationService)
        ).build()

      running(app) {
        val config = app.injector.instanceOf[FrontendAppConfig]
        val request = AuthenticatedIdentifierRequest(FakeRequest(GET, "/test/url?k=session-id"), testCredentials, vrn, Enrolments(Set.empty), Some(registration))
        val controller = new Harness(mockRegistrationConnector, config, mockDataMigrationService, mockIossExclusionService)

        val result = controller.callFilter(request).futureValue
        verify(mockDataMigrationService, times(1)).migrate(any(), any())

        result.value `mustBe` Redirect(routes.AlreadyRegisteredController.onPageLoad())
      }
    }

    "must redirect to Already Registered when an enrolment is found" in {

      val app = applicationBuilder(None).overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
        .configure(
          "features.enrolments-enabled" -> true,
          "oss-enrolment" -> "HMRC-OSS-ORG"
        )
        .build()

      running(app) {
        val config = app.injector.instanceOf[FrontendAppConfig]
        val request = AuthenticatedIdentifierRequest(FakeRequest(GET, "/test/url?k=session-id"), testCredentials, vrn, registrationEnrolment, None)
        val controller = new Harness(mockRegistrationConnector, config, mockDataMigrationService, mockIossExclusionService)

        val result = controller.callFilter(request).futureValue

        result.value `mustBe` Redirect(routes.AlreadyRegisteredController.onPageLoad())
      }
    }

    "must redirect to Cannot Register Quarantined Ioss Trader when trader is quarantined code 4 on IOSS service when in Normal mode" in {

      when(mockIossExclusionService.isQuarantinedCode4()(any())) thenReturn true.toFuture

      val app = applicationBuilder(None, mode = Some(NormalMode))
        .overrides(bind[IossExclusionService].toInstance(mockIossExclusionService))
        .configure(
          "features.enrolments-enabled" -> true,
          "ioss-enrolment" -> "HMRC-IOSS-ORG"
        )
        .build()

      running(app) {
        val config = app.injector.instanceOf[FrontendAppConfig]
        val request = AuthenticatedIdentifierRequest(FakeRequest(GET, "/test/url?k=session-id"), testCredentials, vrn, iossEnrolments, None)
        val controller = new Harness(mockRegistrationConnector, config, mockDataMigrationService, mockIossExclusionService)

        val result = controller.callFilter(request).futureValue

        result.value `mustBe` Redirect(iossExclusionsRoutes.CannotRegisterQuarantinedIossTraderController.onPageLoad())
      }
    }

    "must redirect to Cannot Register Quarantined Ioss Trader when trader is quarantined code 4 on IOSS service when in Rejoin mode" in {

      when(mockIossExclusionService.isQuarantinedCode4()(any())) thenReturn true.toFuture

      val app = applicationBuilder(None, mode = Some(RejoinMode))
        .overrides(bind[IossExclusionService].toInstance(mockIossExclusionService))
        .configure(
          "features.enrolments-enabled" -> true,
          "ioss-enrolment" -> "HMRC-IOSS-ORG"
        )
        .build()

      running(app) {
        val config = app.injector.instanceOf[FrontendAppConfig]
        val request = AuthenticatedIdentifierRequest(FakeRequest(GET, "/test/url?k=session-id"), testCredentials, vrn, iossEnrolments, None)
        val controller = new Harness(mockRegistrationConnector, config, mockDataMigrationService, mockIossExclusionService)

        val result = controller.callFilter(request).futureValue

        result.value `mustBe` Redirect(iossExclusionsRoutes.CannotRegisterQuarantinedIossTraderController.onPageLoad())
      }
    }

    "must redirect to Cannot Register Quarantined Ioss Trader when trader is quarantined code 4 on IOSS service when in Rejoin Loop mode" in {

      when(mockIossExclusionService.isQuarantinedCode4()(any())) thenReturn true.toFuture

      val app = applicationBuilder(None, mode = Some(RejoinLoopMode))
        .overrides(bind[IossExclusionService].toInstance(mockIossExclusionService))
        .configure(
          "features.enrolments-enabled" -> true,
          "ioss-enrolment" -> "HMRC-IOSS-ORG"
        )
        .build()

      running(app) {
        val config = app.injector.instanceOf[FrontendAppConfig]
        val request = AuthenticatedIdentifierRequest(FakeRequest(GET, "/test/url?k=session-id"), testCredentials, vrn, iossEnrolments, None)
        val controller = new Harness(mockRegistrationConnector, config, mockDataMigrationService, mockIossExclusionService)

        val result = controller.callFilter(request).futureValue

        result.value `mustBe` Redirect(iossExclusionsRoutes.CannotRegisterQuarantinedIossTraderController.onPageLoad())
      }
    }
  }

  Seq(AmendMode, AmendLoopMode).foreach {
    mode =>

      s".filter in $mode" - {
        class AmendHarness(connector: RegistrationConnector, config: FrontendAppConfig, migrationService: DataMigrationService, iossExclusionService: IossExclusionService)
          extends CheckRegistrationFilterImpl(Some(mode), connector, config, migrationService, iossExclusionService) {
          def callFilter(request: AuthenticatedIdentifierRequest[_]): Future[Option[Result]] = filter(request)
        }

        s"must return None when an existing registration is found without enrolments" in {

          when(mockRegistrationConnector.enrolUser()(any())) thenReturn HttpResponse(NO_CONTENT, "").toFuture

          val app = applicationBuilder(None, mode = Some(mode))
            .build()

          running(app) {
            val config = app.injector.instanceOf[FrontendAppConfig]
            val request = AuthenticatedIdentifierRequest(FakeRequest(), testCredentials, vrn, Enrolments(Set.empty), Some(registration))
            val controller = new AmendHarness(mockRegistrationConnector, config, mockDataMigrationService, mockIossExclusionService)

            val result = controller.callFilter(request).futureValue

            result must not be defined
          }
        }

        s"must return None when an existing registration is found with enrolments" in {

          val app = applicationBuilder(None, mode = Some(mode))
            .configure("features.enrolments-enabled" -> true)
            .build()

          running(app) {
            val config = app.injector.instanceOf[FrontendAppConfig]
            val request = AuthenticatedIdentifierRequest(FakeRequest(), testCredentials, vrn, registrationEnrolment, Some(registration))
            val controller = new AmendHarness(mockRegistrationConnector, config, mockDataMigrationService, mockIossExclusionService)

            val result = controller.callFilter(request).futureValue

            result must not be defined
          }
        }

        s"must redirect to Not Registered Controller when a registration is not found" in {

          val app = applicationBuilder(None, mode = Some(mode))
            .build()

          running(app) {
            val config = app.injector.instanceOf[FrontendAppConfig]
            val request = AuthenticatedIdentifierRequest(FakeRequest(), testCredentials, vrn, Enrolments(Set.empty), None)
            val controller = new AmendHarness(mockRegistrationConnector, config, mockDataMigrationService, mockIossExclusionService)

            val result = controller.callFilter(request).futureValue

            result.value `mustBe` Redirect(routes.NotRegisteredController.onPageLoad())
          }
        }

        s"must return None when trader is quarantined code 4 on IOSS service" in {

          when(mockIossExclusionService.isQuarantinedCode4()(any())) thenReturn true.toFuture

          val app = applicationBuilder(None, mode = Some(mode))
            .overrides(bind[IossExclusionService].toInstance(mockIossExclusionService))
            .configure(
              "features.enrolments-enabled" -> true,
              "ioss-enrolment" -> "HMRC-IOSS-ORG"
            )
            .build()

          running(app) {
            val config = app.injector.instanceOf[FrontendAppConfig]
            val request = AuthenticatedIdentifierRequest(FakeRequest(GET, "/test/url?k=session-id"), testCredentials, vrn, ossAndIossEnrolments, Some(registration))
            val controller = new AmendHarness(mockRegistrationConnector, config, mockDataMigrationService, mockIossExclusionService)

            val result = controller.callFilter(request).futureValue

            result must not be defined
          }
        }
      }
  }
}
