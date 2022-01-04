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

package forms

import formats.Format.dateFormatter

import java.time.LocalDate
import forms.mappings.Mappings

import javax.inject.Inject
import play.api.data.Form

class UkVatEffectiveDateFormProvider @Inject() extends Mappings {

  def apply(): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey     = "ukVatEffectiveDate.error.invalid",
        allRequiredKey = "ukVatEffectiveDate.error.required.all",
        twoRequiredKey = "ukVatEffectiveDate.error.required.two",
        requiredKey    = "ukVatEffectiveDate.error.required"
      )
      .verifying(maxDate(LocalDate.now(), "ukVatEffectiveDate.error.future-date", LocalDate.now().format(dateFormatter)))
    )
}
