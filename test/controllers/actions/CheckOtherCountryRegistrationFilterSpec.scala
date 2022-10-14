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
import models.core.{Match, MatchType}
import models.requests.AuthenticatedIdentifierRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import services.CoreRegistrationValidationService
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.domain.Vrn

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckOtherCountryRegistrationFilterSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val genericMatch = Match(
    MatchType.FixedEstablishmentActiveNETP,
    "333333333",
    None,
    "DE",
    Some(2),
    None,
    None,
    None,
    None
  )

  class Harness(service: CoreRegistrationValidationService) extends
    CheckOtherCountryRegistrationFilterImpl(service) {
    def callFilter(request: AuthenticatedIdentifierRequest[_]): Future[Option[Result]] = filter(request)
  }

  private val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]

  ".filter" - {
    "must redirect to AlreadyRegisteredOtherCountry page when the user is registered in another OSS service" in {
      val vrn = Vrn("333333331")
      val app = applicationBuilder(None)
        .overrides(
          bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
        ).build()

      running(app) {

        when(mockCoreRegistrationValidationService.searchUkVrn(eqTo(vrn))(any(),any())) thenReturn Future.successful(Option(genericMatch))

        val request = AuthenticatedIdentifierRequest(FakeRequest(), testCredentials, vrn, Enrolments(Set.empty))
        val controller = new Harness(mockCoreRegistrationValidationService)

        val result = controller.callFilter(request).futureValue

        result mustBe Some(Redirect(controllers.routes.AlreadyRegisteredOtherCountryController.onPageLoad(genericMatch.memberState).url))
      }
    }

    "must return none e when the user is not registered in another OSS service" in {
      val vrn = Vrn("333333331")
      val app = applicationBuilder(None)
        .overrides(
          bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
        ).build()

      running(app) {

        when(mockCoreRegistrationValidationService.searchUkVrn(eqTo(vrn))(any(),any())) thenReturn Future.successful(None)

        val request = AuthenticatedIdentifierRequest(FakeRequest(), testCredentials, vrn, Enrolments(Set.empty))
        val controller = new Harness(mockCoreRegistrationValidationService)

        val result = controller.callFilter(request).futureValue

        result mustBe None
      }
    }
  }
}
