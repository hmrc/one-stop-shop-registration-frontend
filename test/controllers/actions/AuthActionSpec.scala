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

package controllers.actions

import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testutils.TestAuthRetrievals._
import uk.gov.hmrc.auth.core.AffinityGroup.{Individual, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier

import java.net.URLEncoder
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private type RetrievalsType = Option[Credentials] ~ Enrolments ~ Option[AffinityGroup] ~ ConfidenceLevel ~ Option[CredentialRole]
  private val vatEnrolment = Enrolments(Set(Enrolment("HMRC-MTD-VAT", Seq(EnrolmentIdentifier("VRN", "123456789")), "Activated")))

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction { _ => Results.Ok }
  }

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  override def beforeEach(): Unit = {
    Mockito.reset(mockAuthConnector)
  }

  "Auth Action" - {

    "when the user is logged in as an Organisation Admin with a VAT enrolment and strong credentials" - {

      "must succeed" in {

        val application = applicationBuilder(None).build()

        running(application) {
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]

          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(testCredentials) ~ vatEnrolment ~ Some(Organisation) ~ ConfidenceLevel.L50 ~ Some(User)))

          val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller = new Harness(action)
          val result = controller.onPageLoad()(fakeRequest)

          status(result) mustEqual OK
        }
      }
    }

    "when the user is logged in as an Individual with a VAT enrolment, strong credentials and confidence level 250" - {

      "must succeed" in {

        val application = applicationBuilder(None).build()

        running(application) {
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]

          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(testCredentials) ~ vatEnrolment ~ Some(Individual) ~ ConfidenceLevel.L250 ~ None))

          val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller = new Harness(action)
          val result = controller.onPageLoad()(fakeRequest)

          status(result) mustEqual OK
        }
      }
    }

    "when the user has logged in as an Organisation Assistant with a VAT enrolment and strong credentials" - {

      "must be redirected to the Unsupported Credential Role page" in {

        val application = applicationBuilder(None).build()

        running(application) {
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]

          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(testCredentials) ~ vatEnrolment ~ Some(Organisation) ~ ConfidenceLevel.L50 ~ Some(Assistant)))

          val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller = new Harness(action)
          val result = controller.onPageLoad()(fakeRequest)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.auth.routes.AuthController.unsupportedCredentialRole().url
        }
      }
    }

    "when the user has logged in as an Organisation Admin with strong credentials but no vat enrolment" - {

      "must be redirected to the insufficient Enrolments page" in {

        val application = applicationBuilder(None).build()

        running(application) {
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]

          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(testCredentials) ~ Enrolments(Set.empty) ~ Some(Organisation) ~ ConfidenceLevel.L50 ~ Some(User)))

          val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller = new Harness(action)
          val result = controller.onPageLoad()(fakeRequest)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.auth.routes.AuthController.insufficientEnrolments().url
        }
      }
    }

    "when the user has logged in as an Individual with a VAT enrolment and strong credentials, but confidence level less then 200" - {

      "must be redirected to uplift their confidence level" in {

        val application = applicationBuilder(None).build()

        running(application) {
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]

          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(testCredentials) ~ vatEnrolment ~ Some(Individual) ~ ConfidenceLevel.L200 ~ None))

          val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller = new Harness(action)
          val result = controller.onPageLoad()(fakeRequest)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe "http://localhost:9948/mdtp/uplift?origin=OSS&confidenceLevel=250&completionURL=http%3A%2F%2Flocalhost%3A10200&failureURL=http%3A%2F%2Flocalhost%3A10200%2Fpay-vat-on-goods-sold-to-eu%2Fnorthern-ireland-register%2Fidentity-complete%3FcontinueURL%3Dhttp%253A%252F%252Flocalhost%253A10200"
        }
      }
    }

    "when the user has logged in as an Individual without a VAT enrolment" - {

      "must be redirected to the insufficient Enrolments page" in {

        val application = applicationBuilder(None).build()

        running(application) {
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]

          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some(testCredentials) ~ Enrolments(Set.empty) ~ Some(Individual) ~ ConfidenceLevel.L250 ~ None))

          val action = new AuthenticatedIdentifierAction(mockAuthConnector, appConfig, bodyParsers)
          val controller = new Harness(action)
          val result = controller.onPageLoad()(fakeRequest)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe controllers.auth.routes.AuthController.insufficientEnrolments().url
        }
      }
    }

    "when the user hasn't logged in" - {

      "must redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new MissingBearerToken), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user's session has expired" - {

      "must redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new BearerTokenExpired), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val request    = FakeRequest(GET, "/foo")
          val result = controller.onPageLoad()(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustEqual appConfig.loginUrl + "?continue=" + URLEncoder.encode(appConfig.loginContinueUrl + request.path, "UTF-8")
        }
      }
    }

    "the user used an unsupported auth provider" - {

      "must redirect the user to the unsupported auth provider page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAuthProvider), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.auth.routes.AuthController.unsupportedAuthProvider(appConfig.loginContinueUrl).url + "%2F"
        }
      }
    }

    "the user has an unsupported affinity group" - {

      "must redirect the user to the unsupported affinity group page" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAffinityGroup), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.auth.routes.AuthController.unsupportedAffinityGroup().url
        }
      }
    }

    "the user has weak credentials" - {

      "must redirect the user to an MFA uplift journey" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig   = application.injector.instanceOf[FrontendAppConfig]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new IncorrectCredentialStrength), appConfig, bodyParsers)
          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER

          redirectLocation(result) mustBe Some("http://localhost:9553/bas-gateway/uplift-mfa?origin=OSS&continueUrl=http%3A%2F%2Flocalhost%3A10200%2F")
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
