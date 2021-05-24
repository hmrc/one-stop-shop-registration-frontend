package service

import base.SpecBase
import connectors.RegistrationConnector
import models.RegistrationResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify}
import org.mockito.MockitoSugar.{mock, when}
import play.api.Application
import play.api.http.Status.ACCEPTED
import testutils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future.successful

class RegistrationServiceSpec extends SpecBase with WireMockHelper {

  private val mockRegistrationConnector = mock[RegistrationConnector]
  private val mockRegistrationResponse = mock[RegistrationResponse]

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  private def application: Application =
    applicationBuilder()
      .configure("microservice.services.one-stop-shop-registration.port" -> server.port)
      .build()

  "submit" - {

    "must create and send valid Registration to One Stop Registration back end service" in {
      when(mockRegistrationConnector.submitRegistration(any()))
        .thenReturn(successful(mockRegistrationResponse)) // CREATED

      val registrationService = application.injector.instanceOf[RegistrationService]

      val response = registrationService.submit(emptyUserAnswers).futureValue.get

      response.status mustBe ACCEPTED
      verify(mockRegistrationConnector, times(1)).submitRegistration(any())
    }

  }



}
