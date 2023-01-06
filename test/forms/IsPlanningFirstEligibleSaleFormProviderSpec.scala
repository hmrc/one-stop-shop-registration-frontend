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
import formats.Format.dateFormatter
import forms.behaviours.BooleanFieldBehaviours
import org.mockito.Mockito.when
import org.mockito.MockitoSugar.mock
import play.api.data.FormError
import services.DateService

import java.time.LocalDate

class IsPlanningFirstEligibleSaleFormProviderSpec extends SpecBase with BooleanFieldBehaviours {

  private val dateService = mock[DateService]
  private val date = LocalDate.now()
  private val dateFormatted = date.format(dateFormatter)

  val requiredKey = "isPlanningFirstEligibleSale.error.required"
  val invalidKey = "error.boolean"

  ".value" - {

    when(dateService.startOfNextQuarter).thenReturn(date)

    val form = new IsPlanningFirstEligibleSaleFormProvider(dateService)()
    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey, Seq(dateFormatted))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(dateFormatted))
    )
  }
}
