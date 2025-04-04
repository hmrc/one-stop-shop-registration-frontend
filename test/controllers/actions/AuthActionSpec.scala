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
import com.google.inject.Inject
import config.Constants.iossEnrolmentKey
import config.FrontendAppConfig
import connectors.RegistrationConnector
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.{Action, AnyContent, DefaultActionBuilder, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.UrlBuilderService
import services.ioss.{AccountService, IossRegistrationService}
import testutils.TestAuthRetrievals.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.{Individual, Organisation}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval, ~}
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}
import uk.gov.hmrc.play.bootstrap.binders.OnlyRelative
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
import utils.FutureSyntax.FutureOps

import java.net.URLEncoder
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private type RetrievalsType = Option[Credentials] ~ Enrolments ~ Option[AffinityGroup] ~ ConfidenceLevel
  private val vatEnrolment = Enrolments(Set(Enrolment("HMRC-MTD-VAT", Seq(EnrolmentIdentifier("VRN", "123456789")), "Activated")))
  private val vatdecEnrolment = Enrolments(Set(Enrolment("HMCE-VATDEC-ORG", Seq(EnrolmentIdentifier("VATRegNo", "123456789")), "Activated")))

  private val iossNumber2: String = "IM9001234568"

  private val iossSingleEnrolment =
    Enrolments(Set(vatEnrolment.enrolments.head, Enrolment("HMRC-IOSS-ORG", Seq(EnrolmentIdentifier(iossEnrolmentKey, iossNumber)), "Activated")))

  private val iossMultipleEnrolment =
    Enrolments(Set(vatEnrolment.enrolments.head, iossSingleEnrolment.enrolments.tail.head, Enrolment("HMRC-IOSS-ORG", Seq(EnrolmentIdentifier(iossEnrolmentKey, iossNumber2)), "Activated")))

  class Harness(authAction: AuthenticatedIdentifierAction, defaultAction: DefaultActionBuilder) {
    def onPageLoad(): Action[AnyContent] = (defaultAction andThen authAction) { _ => Results.Ok }
  }

  private val mockAuthConnector: AuthConnector = mock[AuthConnector]
  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]
  private val mockIossRegistrationService: IossRegistrationService = mock[IossRegistrationService]
  private val mockAccountService: AccountService = mock[AccountService]

  override def beforeEach(): Unit = {
    Mockito.reset(mockAuthConnector)
    Mockito.reset(mockRegistrationConnector)
    Mockito.reset(mockIossRegistrationService)
    Mockito.reset(mockAccountService)
  }

  "Auth Action" - {

    "when registration enrolment enabled" - {

      "when the user is logged in as an Organisation Admin with a VAT enrolment and strong credentials" - {

        "must succeed" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "true")
            .configure("oss-enrolment" -> "HMRC-OSS-ORG")
            .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
            .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
            .overrides(bind[IossRegistrationService].toInstance(mockIossRegistrationService))
            .build()

          running(application) {
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ vatEnrolment ~ Some(Organisation) ~ ConfidenceLevel.L50))

            when(mockRegistrationConnector.getRegistration()(any())) thenReturn None.toFuture
            when(mockIossRegistrationService.getIossRegistration(any())(any())) thenReturn None.toFuture

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) `mustBe` OK
          }
        }
      }

      "when the user is logged in as an Organisation Admin with a VATDEC enrolment and strong credentials" - {

        "must succeed" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "true")
            .configure("oss-enrolment" -> "HMRC-OSS-ORG")
            .overrides(bind[AuthConnector].toInstance(mockAuthConnector))
            .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
            .overrides(bind[IossRegistrationService].toInstance(mockIossRegistrationService))
            .build()

          running(application) {
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ vatdecEnrolment ~ Some(Organisation) ~ ConfidenceLevel.L50))

            when(mockRegistrationConnector.getRegistration()(any())) thenReturn None.toFuture
            when(mockIossRegistrationService.getIossRegistration(any())(any())) thenReturn None.toFuture

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) `mustBe` OK
          }
        }
      }

      "when the user is logged in as an Individual with a VAT enrolment, strong credentials and confidence level 200" - {

        "must succeed" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "true")
            .configure("oss-enrolment" -> "HMRC-OSS-ORG")
            .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
            .build()

          running(application) {
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ vatEnrolment ~ Some(Individual) ~ ConfidenceLevel.L200))

            when(mockRegistrationConnector.getRegistration()(any())) thenReturn None.toFuture
            when(mockIossRegistrationService.getIossRegistration(any())(any())) thenReturn None.toFuture

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) `mustBe` OK
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
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ Enrolments(Set.empty) ~ Some(Organisation) ~ ConfidenceLevel.L50))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` controllers.auth.routes.AuthController.insufficientEnrolments().url
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
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ vatEnrolment ~ Some(Individual) ~ ConfidenceLevel.L50))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) `mustBe` SEE_OTHER
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
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ Enrolments(Set.empty) ~ Some(Individual) ~ ConfidenceLevel.L200))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` controllers.auth.routes.AuthController.insufficientEnrolments().url
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
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]
            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(None ~ Enrolments(Set.empty) ~ Some(Individual) ~ ConfidenceLevel.L50))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(action, actionBuilder)
            val request = FakeRequest().withHeaders(HeaderNames.xSessionId -> "123")
            val result = controller.onPageLoad()(request)

            status(result) `mustBe` SEE_OTHER

            redirectLocation(result).value `mustBe` controllers.routes.UnauthorisedController.onPageLoad().url
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
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            val authAction = new AuthenticatedIdentifierAction(
              new FakeFailingAuthConnector(new MissingBearerToken),
              appConfig,
              urlBuilder,
              mockAccountService,
              mockIossRegistrationService,
              mockRegistrationConnector
            )
            val controller = new Harness(authAction, actionBuilder)
            val result = controller.onPageLoad()(FakeRequest("", "/endpoint"))

            status(result) `mustBe` SEE_OTHER
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
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            val authAction = new AuthenticatedIdentifierAction(
              new FakeFailingAuthConnector(new BearerTokenExpired),
              appConfig,
              urlBuilder,
              mockAccountService,
              mockIossRegistrationService,
              mockRegistrationConnector
            )
            val controller = new Harness(authAction, actionBuilder)
            val request = FakeRequest(GET, "/foo")
            val result = controller.onPageLoad()(request)

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` appConfig.loginUrl + "?continue=" + URLEncoder.encode(urlBuilder.loginContinueUrl(request).get(OnlyRelative).url, "UTF-8")
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
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            val authAction = new AuthenticatedIdentifierAction(
              new FakeFailingAuthConnector(new UnsupportedAuthProvider),
              appConfig,
              urlBuilder,
              mockAccountService,
              mockIossRegistrationService,
              mockRegistrationConnector
            )
            val controller = new Harness(authAction, actionBuilder)
            val result = controller.onPageLoad()(FakeRequest("", "/endpoint"))

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` controllers.auth.routes.AuthController.unsupportedAuthProvider(urlBuilder.loginContinueUrl(FakeRequest("", "/endpoint"))).url
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
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            val authAction = new AuthenticatedIdentifierAction(
              new FakeFailingAuthConnector(new UnsupportedAffinityGroup),
              appConfig,
              urlBuilder,
              mockAccountService,
              mockIossRegistrationService,
              mockRegistrationConnector
            )
            val controller = new Harness(authAction, actionBuilder)
            val result = controller.onPageLoad()(FakeRequest())

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` controllers.auth.routes.AuthController.unsupportedAffinityGroup().url
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
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            val authAction = new AuthenticatedIdentifierAction(
              new FakeFailingAuthConnector(new IncorrectCredentialStrength),
              appConfig,
              urlBuilder,
              mockAccountService,
              mockIossRegistrationService,
              mockRegistrationConnector
            )
            val controller = new Harness(authAction, actionBuilder)
            val request = FakeRequest().withHeaders(HeaderNames.xSessionId -> "123")
            val result = controller.onPageLoad()(request)

            status(result) `mustBe` SEE_OTHER

            redirectLocation(result).value `mustBe` "http://localhost:9553/bas-gateway/uplift-mfa?origin=OSS&continueUrl=http%3A%2F%2Flocalhost%3A10200%2F%3Fk%3D123"
          }
        }
      }

      "the connector returns other AuthorisationException" - {

        val exceptions = List(new InternalError(), new FailedRelationship(), IncorrectNino)
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

              val authAction = new AuthenticatedIdentifierAction(
                new FakeFailingAuthConnector(e),
                appConfig,
                urlBuilder,
                mockAccountService,
                mockIossRegistrationService,
                mockRegistrationConnector
              )
              val controller = new Harness(authAction, actionBuilder)
              val request = FakeRequest().withHeaders(HeaderNames.xSessionId -> "123")
              val result = controller.onPageLoad()(request)

              status(result) `mustBe` SEE_OTHER

              redirectLocation(result).value `mustBe` controllers.routes.UnauthorisedController.onPageLoad().url
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
            .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
            .build()

          running(application) {
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ vatEnrolment ~ Some(Organisation) ~ ConfidenceLevel.L50))

            when(mockRegistrationConnector.getRegistration()(any())) thenReturn None.toFuture
            when(mockIossRegistrationService.getIossRegistration(any())(any())) thenReturn None.toFuture

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) `mustBe` OK
          }
        }
      }

      "when the user is logged in as an Organisation Admin with a VATDEC enrolment and strong credentials" - {

        "must succeed" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "false")
            .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
            .build()

          running(application) {
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ vatdecEnrolment ~ Some(Organisation) ~ ConfidenceLevel.L50))

            when(mockRegistrationConnector.getRegistration()(any())) thenReturn None.toFuture
            when(mockIossRegistrationService.getIossRegistration(any())(any())) thenReturn None.toFuture

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) `mustBe` OK
          }
        }
      }

      "when the user is logged in as an Individual with a VAT enrolment, strong credentials and confidence level 200" - {

        "must succeed" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "false")
            .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
            .build()

          running(application) {
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ vatEnrolment ~ Some(Individual) ~ ConfidenceLevel.L200))

            when(mockRegistrationConnector.getRegistration()(any())) thenReturn None.toFuture
            when(mockIossRegistrationService.getIossRegistration(any())(any())) thenReturn None.toFuture

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) `mustBe` OK
          }
        }
      }

      "when the user has logged in as an Organisation Admin with strong credentials but no vat enrolment" - {

        "must be redirected to the insufficient Enrolments page" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "false")
            .build()

          running(application) {
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ Enrolments(Set.empty) ~ Some(Organisation) ~ ConfidenceLevel.L50))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` controllers.auth.routes.AuthController.insufficientEnrolments().url
          }
        }
      }

      "when the user has logged in as an Individual with a VAT enrolment and strong credentials, but confidence level less than 200" - {

        "must be redirected to uplift their confidence level" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "false")
            .build()

          running(application) {
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ vatEnrolment ~ Some(Individual) ~ ConfidenceLevel.L50))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) `mustBe` SEE_OTHER
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
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ Enrolments(Set.empty) ~ Some(Individual) ~ ConfidenceLevel.L200))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` controllers.auth.routes.AuthController.insufficientEnrolments().url
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
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]
            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(None ~ Enrolments(Set.empty) ~ Some(Individual) ~ ConfidenceLevel.L50))

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(action, actionBuilder)
            val request = FakeRequest().withHeaders(HeaderNames.xSessionId -> "123")
            val result = controller.onPageLoad()(request)

            status(result) `mustBe` SEE_OTHER

            redirectLocation(result).value `mustBe` controllers.routes.UnauthorisedController.onPageLoad().url
          }
        }
      }

      "when the user is logged in as an Organisation Admin with a VAT enrolment and strong credentials and has a single IOSS enrolment" - {

        "must succeed" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "false")
            .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
            .build()

          running(application) {
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ iossSingleEnrolment ~ Some(Organisation) ~ ConfidenceLevel.L50))

            when(mockRegistrationConnector.getRegistration()(any())) thenReturn None.toFuture
            when(mockIossRegistrationService.getIossRegistration(any())(any())) thenReturn Some(arbitraryIossEtmpDisplayRegistration.arbitrary.sample.value).toFuture

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) `mustBe` OK
          }
        }
      }

      "when the user is logged in as an Organisation Admin with a VAT enrolment and strong credentials and has multiple IOSS enrolments" - {

        "must succeed" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "false")
            .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
            .overrides(bind[AccountService].toInstance(mockAccountService))
            .build()

          running(application) {
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ iossMultipleEnrolment ~ Some(Organisation) ~ ConfidenceLevel.L50))

            when(mockRegistrationConnector.getRegistration()(any())) thenReturn None.toFuture
            when(mockIossRegistrationService.getIossRegistration(any())(any())) thenReturn Some(arbitraryIossEtmpDisplayRegistration.arbitrary.sample.value).toFuture
            when(mockAccountService.getLatestAccount()(any())) thenReturn Some(iossNumber).toFuture

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) `mustBe` OK
          }
        }
      }

      "when the user is logged in as an Individual Admin with a VAT enrolment and strong credentials and has a single IOSS enrolment" - {

        "must succeed" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "false")
            .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
            .build()

          running(application) {
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ iossSingleEnrolment ~ Some(Individual) ~ ConfidenceLevel.L200))

            when(mockRegistrationConnector.getRegistration()(any())) thenReturn None.toFuture
            when(mockIossRegistrationService.getIossRegistration(any())(any())) thenReturn Some(arbitraryIossEtmpDisplayRegistration.arbitrary.sample.value).toFuture

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) `mustBe` OK
          }
        }
      }

      "when the user is logged in as an Individual Admin with a VAT enrolment and strong credentials and has multiple IOSS enrolments" - {

        "must succeed" in {

          val application = applicationBuilder(None)
            .configure("features.enrolments-enabled" -> "false")
            .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
            .overrides(bind[AccountService].toInstance(mockAccountService))
            .build()

          running(application) {
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val urlBuilder = application.injector.instanceOf[UrlBuilderService]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
              .thenReturn(Future.successful(Some(testCredentials) ~ iossMultipleEnrolment ~ Some(Individual) ~ ConfidenceLevel.L200))

            when(mockRegistrationConnector.getRegistration()(any())) thenReturn None.toFuture
            when(mockIossRegistrationService.getIossRegistration(any())(any())) thenReturn Some(arbitraryIossEtmpDisplayRegistration.arbitrary.sample.value).toFuture
            when(mockAccountService.getLatestAccount()(any())) thenReturn Some(iossNumber).toFuture

            val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(action, actionBuilder)
            val result = controller.onPageLoad()(fakeRequest)

            status(result) `mustBe` OK
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
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new MissingBearerToken), appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(authAction, actionBuilder)
            val result = controller.onPageLoad()(FakeRequest("", "/endpoint"))

            status(result) `mustBe` SEE_OTHER
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
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new BearerTokenExpired), appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(authAction, actionBuilder)
            val request = FakeRequest(GET, "/foo")
            val result = controller.onPageLoad()(request)

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` appConfig.loginUrl + "?continue=" + URLEncoder.encode(urlBuilder.loginContinueUrl(request).get(OnlyRelative).url, "UTF-8")
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
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAuthProvider), appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(authAction, actionBuilder)
            val result = controller.onPageLoad()(FakeRequest("", "/endpoint"))

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` controllers.auth.routes.AuthController.unsupportedAuthProvider(urlBuilder.loginContinueUrl(FakeRequest("", "/endpoint"))).url
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
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAffinityGroup), appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(authAction, actionBuilder)
            val result = controller.onPageLoad()(FakeRequest())

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` controllers.auth.routes.AuthController.unsupportedAffinityGroup().url
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
            val appConfig = application.injector.instanceOf[FrontendAppConfig]
            val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

            val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new IncorrectCredentialStrength), appConfig, urlBuilder, mockAccountService, mockIossRegistrationService, mockRegistrationConnector)
            val controller = new Harness(authAction, actionBuilder)
            val request = FakeRequest().withHeaders(HeaderNames.xSessionId -> "123")
            val result = controller.onPageLoad()(request)

            status(result) `mustBe` SEE_OTHER

            redirectLocation(result).value `mustBe` "http://localhost:9553/bas-gateway/uplift-mfa?origin=OSS&continueUrl=http%3A%2F%2Flocalhost%3A10200%2F%3Fk%3D123"
          }
        }
      }

      "the connector returns other AuthorisationException" - {

        val exceptions = List(new InternalError(), new FailedRelationship(), IncorrectNino)
        exceptions.foreach { e =>
          s"$e must redirect the user to an Unauthorised page" in {

            val application = applicationBuilder(userAnswers = None)
              .configure("features.enrolments-enabled" -> "false")
              .build()

            running(application) {
              val urlBuilder = application.injector.instanceOf[UrlBuilderService]
              val appConfig = application.injector.instanceOf[FrontendAppConfig]
              val actionBuilder = application.injector.instanceOf[DefaultActionBuilder]

              val authAction = new AuthenticatedIdentifierAction(
                new FakeFailingAuthConnector(e),
                appConfig,
                urlBuilder,
                mockAccountService,
                mockIossRegistrationService,
                mockRegistrationConnector
              )
              val controller = new Harness(authAction, actionBuilder)
              val request = FakeRequest().withHeaders(HeaderNames.xSessionId -> "123")
              val result = controller.onPageLoad()(request)

              status(result) `mustBe` SEE_OTHER

              redirectLocation(result).value `mustBe` controllers.routes.UnauthorisedController.onPageLoad().url
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
