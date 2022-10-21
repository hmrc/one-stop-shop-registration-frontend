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

package services

import base.SpecBase
import connectors.{SaveForLaterConnector, SavedUserAnswers}
import controllers.routes
import models.NormalMode
import models.requests.{AuthenticatedDataRequest, SaveForLaterRequest}
import models.responses.NotFound
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.AnyContent
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SaveForLaterServiceSpec extends SpecBase {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  val request = AuthenticatedDataRequest(FakeRequest("GET", "/"), testCredentials, vrn, emptyUserAnswers)
  implicit val dataRequest: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest(request, testCredentials, vrn, basicUserAnswers)

  private val mockSaveForLaterConnector = mock[SaveForLaterConnector]
  private val saveForLaterService = new SaveForLaterService(mockSaveForLaterConnector)
  private val redirectLocation = routes.BankDetailsController.onPageLoad(NormalMode)
  private val originLocation = routes.BusinessContactDetailsController.onPageLoad(NormalMode)
  private val errorLocation = routes.JourneyRecoveryController.onPageLoad()

  private val instantDate = Instant.now()
  private val saveForLaterRequest: SaveForLaterRequest = SaveForLaterRequest(vrn, Json.toJson("test"), None)
  private val savedUserAnswers: SavedUserAnswers = SavedUserAnswers(
    saveForLaterRequest.vrn,
    JsObject(Seq("saveForLaterRequest" -> Json.toJson(saveForLaterRequest.data))),
    None,
    instantDate
  )

  ".saveAnswers" - {

    "must Redirect to redirect location when answers are submitted successfully" in {

      when(mockSaveForLaterConnector.submit(eqTo(saveForLaterRequest))(any())) thenReturn Future.successful(Right(Some(savedUserAnswers)))

      val result = saveForLaterService.saveAnswers(basicUserAnswers, saveForLaterRequest, redirectLocation, originLocation, errorLocation)(request)

      result.futureValue mustBe Redirect(redirectLocation)
    }

    "must Redirect to error location when there is an unexpected result on submit" in {

      when(mockSaveForLaterConnector.submit(eqTo(saveForLaterRequest))(any())) thenReturn Future.successful(Right(None))

      val result = saveForLaterService.saveAnswers(basicUserAnswers, saveForLaterRequest, redirectLocation, originLocation, errorLocation)(request)

      result.futureValue mustBe Redirect(errorLocation)
    }

    "must Redirect to error location when an Unexpected Response Status is received" in {

      when(mockSaveForLaterConnector.submit(eqTo(saveForLaterRequest))(any())) thenReturn Future.successful(Left(NotFound))

      val result = saveForLaterService.saveAnswers(basicUserAnswers, saveForLaterRequest, redirectLocation, originLocation, errorLocation)(request)

      result.futureValue mustBe Redirect(errorLocation)
    }
  }

}
