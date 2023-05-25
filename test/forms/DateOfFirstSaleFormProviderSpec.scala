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

package forms

import base.SpecBase
import controllers.routes
import formats.Format.dateFormatter
import forms.behaviours.DateBehaviours
import models.requests.AuthenticatedDataRequest
import models.NormalMode
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.FormError
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import services.{CoreRegistrationValidationService, DateService}

import java.time.LocalDate

class DateOfFirstSaleFormProviderSpec extends SpecBase with DateBehaviours with MockitoSugar {

  private val coreRegistrationValidationService: CoreRegistrationValidationService = mock[CoreRegistrationValidationService]

  private val dateService = new DateService(stubClockAtArbitraryDate, coreRegistrationValidationService)

  private lazy val dateOfFirstSaleRoute = routes.DateOfFirstSaleController.onPageLoad(NormalMode).url
  private implicit val dataRequest: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest(FakeRequest(GET, dateOfFirstSaleRoute), testCredentials, vrn, None, emptyUserAnswers)

  val form = new DateOfFirstSaleFormProvider(dateService, stubClockAtArbitraryDate)()

  ".value" - {

    val validData = datesBetween(
      min = dateService.earliestSaleAllowed(),
      max = dateService.earliestSaleAllowed().plusDays(9)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "dateOfFirstSale.error.required.all")

    "no passed date" - {
      behave like dateFieldWithMax(
        form,
        "value",
        LocalDate.now(stubClockAtArbitraryDate),
        FormError(
          "value",
          "dateOfFirstSale.error.minMaxToday",
          Seq(dateService.earliestSaleAllowed().format(dateFormatter)))
      )
    }

    "with a passed date not today" - {
      val arbDate = LocalDate.now(stubClockAtArbitraryDate)
      val notToday = arbDate.plusDays(3)
      val notTodayForm = new DateOfFirstSaleFormProvider(dateService, stubClockAtArbitraryDate)(notToday)

      behave like dateFieldWithMax(
        notTodayForm,
        "value",
        arbDate,
        FormError(
          "value",
          "dateOfFirstSale.error.minMax",
          Seq(dateService.earliestSaleAllowed().format(dateFormatter), notToday.format(dateFormatter))
        )
      )
    }
  }
}
