/*
 * Copyright 2026 HM Revenue & Customs
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

package services

import base.SpecBase
import config.FrontendAppConfig
import connectors.RegistrationConnector
import models.etmp.intermediary.{EtmpTradingName, IntermediaryRegistrationWrapper}
import models.iossRegistration.IossEtmpDisplayRegistration
import models.{BankDetails, BusinessContactDetails, CompositeAccount}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, verifyNoInteractions, when}
import org.scalatest.{BeforeAndAfterEach, PrivateMethodTester}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Application
import play.api.inject.bind
import play.api.mvc.Results.InternalServerError
import play.api.test.Helpers.running
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureSyntax.FutureOps

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CompositeAccountServiceSpec extends SpecBase with BeforeAndAfterEach with PrivateMethodTester {

  private implicit val hc: HeaderCarrier = new HeaderCarrier()

  private val intermediaryEnrolmentKey: String = "HMRC-IOSS-INT"
  private val intermediaryEnrolmentIdentifier: String = "IntNumber"

  private val iossEnrolmentKey: String = "HMRC-IOSS-ORG"
  private val iossEnrolmentIdentifier: String = "IOSSNumber"

  private val intermediaryNumber: String = "IN9001234567"

  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]
  private val mockFrontendAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  when(mockFrontendAppConfig.intermediaryEnrolment) thenReturn intermediaryEnrolmentKey
  when(mockFrontendAppConfig.iossEnrolment) thenReturn iossEnrolmentKey

  private val enrolments: Enrolments = Enrolments(
    enrolments = Set(
      Enrolment(
        key = iossEnrolmentKey,
        identifiers = Seq(EnrolmentIdentifier(iossEnrolmentIdentifier, iossNumber)),
        state = "state"
      ),
      Enrolment(
        key = intermediaryEnrolmentKey,
        identifiers = Seq(EnrolmentIdentifier(intermediaryEnrolmentIdentifier, intermediaryNumber)),
        state = "state"
      )
    )
  )

  private val intermediaryDisplayRegistration = registrationWrapper.etmpDisplayRegistration
  private val iossEtmpDisplayRegistration: IossEtmpDisplayRegistration = arbitraryIossEtmpDisplayRegistration.arbitrary.sample.value

  private val application: Application = applicationBuilder()
    .overrides(
      bind[RegistrationConnector].toInstance(mockRegistrationConnector)
    )
    .build()

  override def beforeEach(): Unit = {
    Mockito.reset(
      mockRegistrationConnector
    )
  }

  "IntermediaryRegistrationService" - {

    ".getCompositeAccount" - {

      "must return None when there are no additional accounts retrieved from enrolments" in {

        val noEnrolments = enrolments.copy(enrolments = Set.empty)

        val app = application

        running(app) {

          val service = CompositeAccountService(mockRegistrationConnector, mockFrontendAppConfig)

          val result = service.getCompositeAccount(noEnrolments, Some(iossNumber)).futureValue

          result `mustBe` None
          verifyNoInteractions(mockRegistrationConnector)
        }
      }

      "with OSS enrolment" - {

        "must return None when the server responds with anything other than a successful response" in {

          val app = application

          when(mockRegistrationConnector.getIossRegistration(any())(any())) thenReturn Left(InternalServerError).toFuture

          running(app) {

            val service = CompositeAccountService(mockRegistrationConnector, mockFrontendAppConfig)

            val result = service.getCompositeAccount(enrolments, Some(iossNumber)).futureValue

            result `mustBe` None
            verify(mockRegistrationConnector, times(1)).getIossRegistration(eqTo(iossNumber))(any())
          }
        }

        "must return a CompositeAccount when an OSS account is retrieved from enrolments" in {

          val iossRegistration: IossEtmpDisplayRegistration = arbitraryIossEtmpDisplayRegistration.arbitrary.sample.value

          val app = application

          when(mockRegistrationConnector.getIossRegistration(any())(any())) thenReturn Right(iossRegistration).toFuture

          running(app) {

            val service = CompositeAccountService(mockRegistrationConnector, mockFrontendAppConfig)

            val result = service.getCompositeAccount(enrolments, Some(iossNumber)).futureValue

            val expectedResult = CompositeAccount(
              tradingNames = iossRegistration.tradingNames.map(tn => EtmpTradingName(tn.tradingName)),
              contactDetails = BusinessContactDetails(
                fullName = iossRegistration.schemeDetails.contactName,
                telephoneNumber = iossRegistration.schemeDetails.businessTelephoneNumber,
                emailAddress = iossRegistration.schemeDetails.businessEmailId
              ),
              bankDetails = BankDetails(
                accountName = iossRegistration.bankDetails.accountName,
                bic = iossRegistration.bankDetails.bic,
                iban = iossRegistration.bankDetails.iban
              )
            )

            result `mustBe` Some(expectedResult)
            verify(mockRegistrationConnector, times(1)).getIossRegistration(eqTo(iossNumber))(any())
          }
        }
      }

      "with Intermediary enrolment" - {

        "must return None when the server responds with anything other than a successful response" in {

          val intermediaryEnrolment: Enrolments = enrolments.copy(enrolments = enrolments.enrolments.tail)

          val app = application

          when(mockRegistrationConnector.getIntermediaryRegistration(any())(any())) thenReturn Left(InternalServerError).toFuture

          running(app) {

            val service = CompositeAccountService(mockRegistrationConnector, mockFrontendAppConfig)

            val result = service.getCompositeAccount(intermediaryEnrolment, Some(iossNumber)).futureValue

            result `mustBe` None
            verify(mockRegistrationConnector, times(1)).getIntermediaryRegistration(eqTo(intermediaryNumber))(any())
          }
        }

        "must return a CompositeAccount when an Intermediary account is retrieved from enrolments" in {

          val intermediaryEnrolment: Enrolments = enrolments.copy(enrolments = enrolments.enrolments.tail)

          val app = application

          when(mockRegistrationConnector.getIntermediaryRegistration(any())(any())) thenReturn Right(registrationWrapper).toFuture

          running(app) {

            val service = CompositeAccountService(mockRegistrationConnector, mockFrontendAppConfig)

            val result = service.getCompositeAccount(intermediaryEnrolment, Some(iossNumber)).futureValue

            val expectedResult = CompositeAccount(
              tradingNames = intermediaryDisplayRegistration.tradingNames.map(x => EtmpTradingName(x.tradingName)),
              contactDetails = BusinessContactDetails(
                fullName = intermediaryDisplayRegistration.schemeDetails.contactName,
                telephoneNumber = intermediaryDisplayRegistration.schemeDetails.businessTelephoneNumber,
                emailAddress = intermediaryDisplayRegistration.schemeDetails.businessEmailId
              ),
              bankDetails = BankDetails(
                accountName = intermediaryDisplayRegistration.bankDetails.accountName,
                bic = intermediaryDisplayRegistration.bankDetails.bic,
                iban = intermediaryDisplayRegistration.bankDetails.iban,
              )
            )

            result `mustBe` Some(expectedResult)
            verify(mockRegistrationConnector, times(1)).getIntermediaryRegistration(eqTo(intermediaryNumber))(any())
          }
        }
      }
    }

    ".getLatestIossRegistration"- {

      "must return an IOSS registration when the server responds with one" in {

        when(mockRegistrationConnector.getIossRegistration(any())(any())) thenReturn Right(iossEtmpDisplayRegistration).toFuture

        val app = application

        running(app) {

          val service = CompositeAccountService(mockRegistrationConnector, mockFrontendAppConfig)

          val getLatestIossRegistration = PrivateMethod[Future[Option[IossEtmpDisplayRegistration]]](Symbol("getLatestIossRegistration"))
          val result = service invokePrivate getLatestIossRegistration(Some(iossNumber), hc)

          result.futureValue `mustBe` Some(iossEtmpDisplayRegistration)
          verify(mockRegistrationConnector, times(1)).getIossRegistration(eqTo(iossNumber))(any())
        }
      }

      "must return None when the server returns anything other than an Intermediary registration" in {

        val app = application

        when(mockRegistrationConnector.getIossRegistration(any())(any())) thenReturn Left(InternalServerError).toFuture

        running(app) {

          val service = CompositeAccountService(mockRegistrationConnector, mockFrontendAppConfig)

          val getLatestIossRegistration = PrivateMethod[Future[Option[IossEtmpDisplayRegistration]]](Symbol("getLatestIossRegistration"))
          val result = service invokePrivate getLatestIossRegistration(Some(iossNumber), hc)

          result.futureValue `mustBe` None
          verify(mockRegistrationConnector, times(1)).getIossRegistration(eqTo(iossNumber))(any())
        }
      }
    }

    ".getIntermediaryRegistration" - {

      "must return None when no Intermediary registration is retrieved" in {

        val nonIntermediaryEnrolment: Enrolments = enrolments
          .copy(enrolments = enrolments.enrolments.dropRight(2))

        val app = application

        running(app) {

          val service = CompositeAccountService(mockRegistrationConnector, mockFrontendAppConfig)

          val getIntermediaryRegistration = PrivateMethod[Future[Option[IntermediaryRegistrationWrapper]]](Symbol("getIntermediaryRegistration"))
          val result = service invokePrivate getIntermediaryRegistration(nonIntermediaryEnrolment, hc)

          result.futureValue `mustBe` None
          verifyNoInteractions(mockRegistrationConnector)
        }
      }

      "must return an intermediaryDisplayRegistration when he server retrieves one" in {

        val app = application

        when(mockRegistrationConnector.getIntermediaryRegistration(any())(any())) thenReturn Right(registrationWrapper).toFuture

        running(app) {

          val service = CompositeAccountService(mockRegistrationConnector, mockFrontendAppConfig)

          val getIntermediaryRegistration = PrivateMethod[Future[Option[IntermediaryRegistrationWrapper]]](Symbol("getIntermediaryRegistration"))
          val result = service invokePrivate getIntermediaryRegistration(enrolments, hc)

          result.futureValue `mustBe` Some(registrationWrapper)
          verify(mockRegistrationConnector, times(1)).getIntermediaryRegistration(eqTo(intermediaryNumber))(any())
        }
      }

      "must return None when the server returns anything other than an Intermediary registration" in {

        val app = application

        when(mockRegistrationConnector.getIntermediaryRegistration(any())(any())) thenReturn Left(InternalServerError).toFuture

        running(app) {

          val service = CompositeAccountService(mockRegistrationConnector, mockFrontendAppConfig)

          val getIntermediaryRegistration = PrivateMethod[Future[Option[IntermediaryRegistrationWrapper]]](Symbol("getIntermediaryRegistration"))
          val result = service invokePrivate getIntermediaryRegistration(enrolments, hc)

          result.futureValue `mustBe` None
          verify(mockRegistrationConnector, times(1)).getIntermediaryRegistration(eqTo(intermediaryNumber))(any())
        }
      }
    }

    ".getIntermediaryEnrolment" - {

      "must return the intermediary number from an intermediary enrolment when one is found" in {

        val service = CompositeAccountService(mockRegistrationConnector, mockFrontendAppConfig)

        val getIntermediaryEnrolment = PrivateMethod[Option[String]](Symbol("getIntermediaryEnrolment"))

        val result = service invokePrivate getIntermediaryEnrolment(enrolments, intermediaryEnrolmentKey, intermediaryEnrolmentIdentifier)

        result `mustBe` Some(intermediaryNumber)
      }

      "must return None when an intermediary enrolment is not found" in {

        val nonIntermediaryEnrolment: Enrolments = Enrolments(
          enrolments = Set(
            Enrolment(
              key = "HMRC-IOSS-ORG",
              identifiers = Seq(EnrolmentIdentifier("IOSSNumber", "IM9001234567")),
              state = "state"
            )
          )
        )
        val service = CompositeAccountService(mockRegistrationConnector, mockFrontendAppConfig)

        val getIntermediaryEnrolment = PrivateMethod[Option[String]](Symbol("getIntermediaryEnrolment"))

        val result = service invokePrivate getIntermediaryEnrolment(nonIntermediaryEnrolment, intermediaryEnrolmentKey, intermediaryEnrolmentIdentifier)

        result `mustBe` None
      }
    }
  }
}
