/*
 * Copyright 2022 HM Revenue & Customs
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
import com.google.inject.Inject
import config.FrontendAppConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{Action, AnyContent, DefaultActionBuilder, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UrlBuilderService
import testutils.TestAuthRetrievals._
import uk.gov.hmrc.auth.core.AffinityGroup.{Individual, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval, ~}
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}

import java.net.URLEncoder
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private type RetrievalsType = Option[Credentials] ~ Enrolments ~ Option[AffinityGroup] ~ ConfidenceLevel ~ Option[CredentialRole]
  private val vatEnrolment = Enrolments(Set(Enrolment("HMRC-MTD-VAT", Seq(EnrolmentIdentifier("VRN", "123456789")), "Activated")))
  private val vatdecEnrolment = Enrolments(Set(Enrolment("HMCE-VATDEC-ORG", Seq(EnrolmentIdentifier("VATRegNo", "123456789")), "Activated")))

  class Harness(authAction: AuthenticatedIdentifierAction, defaultAction: DefaultActionBuilder) {
    def onPageLoad(): Action[AnyContent] = (defaultAction andThen authAction) { _ => Results.Ok }
  }

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  override def beforeEach(): Unit = {
    Mockito.reset(mockAuthConnector)
  }

  "Auth Action" - {

    "when registration enrolment enabled" - {

      "when the user is logged in as an Organisation Admin with a VAT enrolment and strong credentials" - {

        "must succeed" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "true")
            .configure("oss-enrolment" -> "HMRC-OSS-ORG")
            .build()

          running(application) {
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ vatEnrolment ~ Some(Organisation) ~ ConfidenceLevel.L50 ~ Some(User)))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) mustEqual OK
          }
        }
      }

      "when the user is logged in as an Organisation Admin with a VATDEC enrolment and strong credentials" - {

        "must succeed" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "true")
            .configure("oss-enrolment" -> "HMRC-OSS-ORG")
            .build()

          running(application) {
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ vatdecEnrolment ~ Some(Organisation) ~ ConfidenceLevel.L50 ~ Some(User)))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) mustEqual OK
          }
        }
      }

      "when the user is logged in as an Individual with a VAT enrolment, strong credentials and confidence level 200" - {

        "must succeed" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "true")
            .configure("oss-enrolment" -> "HMRC-OSS-ORG")
            .build()

          running(application) {
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ vatEnrolment ~ Some(Individual) ~ ConfidenceLevel.L200 ~ None))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) mustEqual OK
          }
        }
      }

      "when the user has logged in as an Organisation Assistant with a VAT enrolment and strong credentials" - {

        "must be redirected to the Unsupported Credential Role page" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "true")
            .configure("oss-enrolment" -> "HMRC-OSS-ORG")
            .build()

          running(application) {
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ vatEnrolment ~ Some(Organisation) ~ ConfidenceLevel.L50 ~ Some(Assistant)))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.auth.routes.AuthController.unsupportedCredentialRole().url
          }
        }
      }

      "when the user has logged in as an Organisation Admin with strong credentials but no vat enrolment" - {

        "must be redirected to the insufficient Enrolments page" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "true")
            .configure("oss-enrolment" -> "HMRC-OSS-ORG")
            .build()

          running(application) {
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ Enrolments(Set.empty) ~ Some(Organisation) ~ ConfidenceLevel.L50 ~ Some(User)))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.auth.routes.AuthController.insufficientEnrolments().url
          }
        }
      }

      "when the user has logged in as an Individual with a VAT enrolment and strong credentials, but confidence level less than 200" - {

        "must be redirected to uplift their confidence level" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "true")
            .configure("oss-enrolment" -> "HMRC-OSS-ORG")
            .build()

          running(application) {
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ vatEnrolment ~ Some(Individual) ~ ConfidenceLevel.L50 ~ None))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value must startWith(s"${appConfig.ivUpliftUrl}?origin=OSS&confidenceLevel=200")
          }
        }
      }

      "when the user has logged in as an Individual without a VAT enrolment" - {

        "must be redirected to the insufficient Enrolments page" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "true")
            .configure("oss-enrolment" -> "HMRC-OSS-ORG")
            .build()

          running(application) {
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ Enrolments(Set.empty) ~ Some(Individual) ~ ConfidenceLevel.L200 ~ None))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustBe controllers.auth.routes.AuthController.insufficientEnrolments().url
          }
        }
      }

      "when the user has logged in as an Individual with no credentials" - {
        "must redirect the user to an Unauthorised page" in {

          val application = applicationBuilder(userAnswers = None)
            .configure("features.enrolments-enabled" -> "true")
            .configure("oss-enrolment" -> "HMRC-OSS-ORG")
            .build()

          running(application) {
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]
            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(None ~ Enrolments(Set.empty) ~ Some(Individual) ~ ConfidenceLevel.L50 ~ Some(User)))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder)
            val controller = new Harness(action, actionBuilder)
            val request = FakeRequest().withHeaders(HeaderNames.xSessionId -> "123")
            val result = controller.onPageLoad()(request)

            status(result) mustBe SEE_OTHER

            redirectLocation(result).value mustBe controllers.routes.UnauthorisedController.onPageLoad().url
          }
        }

      }

      "when the user hasn't logged in" - {

        "must redirect the user to log in " in {

          val application = applicationBuilder(userAnswers = None)
            .configure("features.enrolments-enabled" -> "true")
            .configure("oss-enrolment" -> "HMRC-OSS-ORG")
            .build()

          running(application) {
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new MissingBearerToken), appConfig, urlBuilder)
            val controller = new Harness(authAction, actionBuilder)
            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value must startWith(appConfig.loginUrl)
          }
        }
      }

      "the user's session has expired" - {

        "must redirect the user to log in " in {

          val application = applicationBuilder(userAnswers = None)
            .configure("features.enrolments-enabled" -> "true")
            .configure("oss-enrolment" -> "HMRC-OSS-ORG")
            .build()

          running(application) {
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new BearerTokenExpired), appConfig, urlBuilder)
            val controller = new Harness(authAction, actionBuilder)
            val request    = FakeRequest(GET, "/foo")
            val result = controller.onPageLoad()(request)

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustEqual appConfig.loginUrl + "?continue=" + URLEncoder.encode(urlBuilder.loginContinueUrl(request), "UTF-8")
          }
        }
      }

      "the user used an unsupported auth provider" - {

        "must redirect the user to the unsupported auth provider page" in {

          val application = applicationBuilder(userAnswers = None)
            .configure("features.enrolments-enabled" -> "true")
            .configure("oss-enrolment" -> "HMRC-OSS-ORG")
            .build()

          running(application) {
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAuthProvider), appConfig, urlBuilder)
            val controller = new Harness(authAction, actionBuilder)
            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe controllers.auth.routes.AuthController.unsupportedAuthProvider(urlBuilder.loginContinueUrl(FakeRequest())).url
          }
        }
      }

      "the user has an unsupported affinity group" - {

        "must redirect the user to the unsupported affinity group page" in {

          val application = applicationBuilder(userAnswers = None)
            .configure("features.enrolments-enabled" -> "true")
            .configure("oss-enrolment" -> "HMRC-OSS-ORG")
            .build()

          running(application) {
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAffinityGroup), appConfig, urlBuilder)
            val controller = new Harness(authAction, actionBuilder)
            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe controllers.auth.routes.AuthController.unsupportedAffinityGroup().url
          }
        }
      }

      "the user has weak credentials" - {

        "must redirect the user to an MFA uplift journey" in {

          val application = applicationBuilder(userAnswers = None)
            .configure("features.enrolments-enabled" -> "true")
            .configure("oss-enrolment" -> "HMRC-OSS-ORG")
            .build()

          running(application) {
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new IncorrectCredentialStrength), appConfig, urlBuilder)
            val controller = new Harness(authAction, actionBuilder)
            val request = FakeRequest().withHeaders(HeaderNames.xSessionId -> "123")
            val result = controller.onPageLoad()(request)

            status(result) mustBe SEE_OTHER

            redirectLocation(result).value mustBe "http://localhost:9553/bas-gateway/uplift-mfa?origin=OSS&continueUrl=http%3A%2F%2Flocalhost%3A10200%2F%3Fk%3D123"
          }
        }
      }

      "the connector returns other AuthorisationException" - {

        val exceptions = List( new InternalError(), new FailedRelationship(),  IncorrectNino)
        exceptions.foreach { e =>
          s"$e must redirect the user to an Unauthorised page" in {

            val application = applicationBuilder(userAnswers = None)
              .configure("features.enrolments-enabled" -> "true")
              .configure("oss-enrolment" -> "HMRC-OSS-ORG")
              .build()

            running(application) {
              val urlBuilder = application.injector.instanceOf[UrlBuilderService]
              val appConfig = application.injector.instanceOf[FrontendAppConfig]
              val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

              val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(e), appConfig, urlBuilder)
              val controller = new Harness(authAction, actionBuilder)
              val request = FakeRequest().withHeaders(HeaderNames.xSessionId -> "123")
              val result = controller.onPageLoad()(request)

              status(result) mustBe SEE_OTHER

              redirectLocation(result).value mustBe controllers.routes.UnauthorisedController.onPageLoad().url
            }
          }
        }
      }
    }

    "when registration enrolment not enabled" - {

      "when the user is logged in as an Organisation Admin with a VAT enrolment and strong credentials" - {

        "must succeed" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "false")
            .build()

          running(application) {
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ vatEnrolment ~ Some(Organisation) ~ ConfidenceLevel.L50 ~ Some(User)))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) mustEqual OK
          }
        }
      }

      "when the user is logged in as an Organisation Admin with a VATDEC enrolment and strong credentials" - {

        "must succeed" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "false")
            .build()

          running(application) {
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ vatdecEnrolment ~ Some(Organisation) ~ ConfidenceLevel.L50 ~ Some(User)))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) mustEqual OK
          }
        }
      }

      "when the user is logged in as an Individual with a VAT enrolment, strong credentials and confidence level 200" - {

        "must succeed" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "false")
            .build()

          running(application) {
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ vatEnrolment ~ Some(Individual) ~ ConfidenceLevel.L200 ~ None))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) mustEqual OK
          }
        }
      }

      "when the user has logged in as an Organisation Assistant with a VAT enrolment and strong credentials" - {

        "must be redirected to the Unsupported Credential Role page" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "false")
            .build()

          running(application) {
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ vatEnrolment ~ Some(Organisation) ~ ConfidenceLevel.L50 ~ Some(Assistant)))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.auth.routes.AuthController.unsupportedCredentialRole().url
          }
        }
      }

      "when the user has logged in as an Organisation Admin with strong credentials but no vat enrolment" - {

        "must be redirected to the insufficient Enrolments page" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "false")
            .build()

          running(application) {
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ Enrolments(Set.empty) ~ Some(Organisation) ~ ConfidenceLevel.L50 ~ Some(User)))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.auth.routes.AuthController.insufficientEnrolments().url
          }
        }
      }

      "when the user has logged in as an Individual with a VAT enrolment and strong credentials, but confidence level less than 200" - {

        "must be redirected to uplift their confidence level" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "false")
            .build()

          running(application) {
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ vatEnrolment ~ Some(Individual) ~ ConfidenceLevel.L50 ~ None))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value must startWith(s"${appConfig.ivUpliftUrl}?origin=OSS&confidenceLevel=200")
          }
        }
      }

      "when the user has logged in as an Individual without a VAT enrolment" - {

        "must be redirected to the insufficient Enrolments page" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "false")
            .build()

          running(application) {
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ Enrolments(Set.empty) ~ Some(Individual) ~ ConfidenceLevel.L200 ~ None))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustBe controllers.auth.routes.AuthController.insufficientEnrolments().url
          }
        }
      }

      "when the user has logged in as an Individual with no credentials" - {
        "must redirect the user to an Unauthorised page" in {

          val application = applicationBuilder(userAnswers = None)
            .configure("features.enrolments-enabled" -> "false")
            .build()

          running(application) {
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]
            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(None ~ Enrolments(Set.empty) ~ Some(Individual) ~ ConfidenceLevel.L50 ~ Some(User)))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder)
            val controller = new Harness(action, actionBuilder)
            val request = FakeRequest().withHeaders(HeaderNames.xSessionId -> "123")
            val result = controller.onPageLoad()(request)

            status(result) mustBe SEE_OTHER

            redirectLocation(result).value mustBe controllers.routes.UnauthorisedController.onPageLoad().url
          }
        }

      }

      "when the user hasn't logged in" - {

        "must redirect the user to log in " in {

          val application = applicationBuilder(userAnswers = None)
            .configure("features.enrolments-enabled" -> "false")
            .build()

          running(application) {
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new MissingBearerToken), appConfig, urlBuilder)
            val controller = new Harness(authAction, actionBuilder)
            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value must startWith(appConfig.loginUrl)
          }
        }
      }

      "the user's session has expired" - {

        "must redirect the user to log in " in {

          val application = applicationBuilder(userAnswers = None)
            .configure("features.enrolments-enabled" -> "false")
            .build()

          running(application) {
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new BearerTokenExpired), appConfig, urlBuilder)
            val controller = new Harness(authAction, actionBuilder)
            val request    = FakeRequest(GET, "/foo")
            val result = controller.onPageLoad()(request)

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustEqual appConfig.loginUrl + "?continue=" + URLEncoder.encode(urlBuilder.loginContinueUrl(request), "UTF-8")
          }
        }
      }

      "the user used an unsupported auth provider" - {

        "must redirect the user to the unsupported auth provider page" in {

          val application = applicationBuilder(userAnswers = None)
            .configure("features.enrolments-enabled" -> "false")
            .build()

          running(application) {
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAuthProvider), appConfig, urlBuilder)
            val controller = new Harness(authAction, actionBuilder)
            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe controllers.auth.routes.AuthController.unsupportedAuthProvider(urlBuilder.loginContinueUrl(FakeRequest())).url
          }
        }
      }

      "the user has an unsupported affinity group" - {

        "must redirect the user to the unsupported affinity group page" in {

          val application = applicationBuilder(userAnswers = None)
            .configure("features.enrolments-enabled" -> "false")
            .build()

          running(application) {
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAffinityGroup), appConfig, urlBuilder)
            val controller = new Harness(authAction, actionBuilder)
            val result = controller.onPageLoad()(FakeRequest())

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe controllers.auth.routes.AuthController.unsupportedAffinityGroup().url
          }
        }
      }

      "the user has weak credentials" - {

        "must redirect the user to an MFA uplift journey" in {

          val application = applicationBuilder(userAnswers = None)
            .configure("features.enrolments-enabled" -> "false")
            .build()

          running(application) {
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val appConfig   = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new IncorrectCredentialStrength), appConfig, urlBuilder)
            val controller = new Harness(authAction, actionBuilder)
            val request = FakeRequest().withHeaders(HeaderNames.xSessionId -> "123")
            val result = controller.onPageLoad()(request)

            status(result) mustBe SEE_OTHER

            redirectLocation(result).value mustBe "http://localhost:9553/bas-gateway/uplift-mfa?origin=OSS&continueUrl=http%3A%2F%2Flocalhost%3A10200%2F%3Fk%3D123"
          }
        }
      }

      "the connector returns other AuthorisationException" - {

        val exceptions = List( new InternalError(), new FailedRelationship(),  IncorrectNino)
        exceptions.foreach { e =>
          s"$e must redirect the user to an Unauthorised page" in {

            val application = applicationBuilder(userAnswers = None)
              .configure("features.enrolments-enabled" -> "false")
              .build()

            running(application) {
              val urlBuilder = application.injector.instanceOf[UrlBuilderService]
              val appConfig = application.injector.instanceOf[FrontendAppConfig]
              val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

              val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(e), appConfig, urlBuilder)
              val controller = new Harness(authAction, actionBuilder)
              val request = FakeRequest().withHeaders(HeaderNames.xSessionId -> "123")
              val result = controller.onPageLoad()(request)

              status(result) mustBe SEE_OTHER

              redirectLocation(result).value mustBe controllers.routes.UnauthorisedController.onPageLoad().url
            }
          }
        }
      }
    }
  }
}

class FakeFailingAuthConnector @Inject()(exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.failed(exceptionToReturn)
}
