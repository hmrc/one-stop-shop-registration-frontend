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

package forms.euDetails

import forms.Validation.Validation.euVatNumberPattern
import forms.behaviours.StringFieldBehaviours
import models.Country
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.FormError

class EuVatNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "euVatNumber.error.required"
  val lengthKey = "euVatNumber.error.length"
  val invalidKey = "euVatNumber.error.invalid"
  val validData = "ES123456789*"
  val maxLength = 12

  val country: Country = arbitrary[Country].sample.value

  val formProvider: EuVatNumberFormProvider = new EuVatNumberFormProvider()
  val form = formProvider(country)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validData
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(country.name))
    )

    "must not bind invalid EU VAT number" in {
      val invalidEuVatNumber = "abc?123"
      val result = form.bind(Map(fieldName -> invalidEuVatNumber)).apply(fieldName)
      result.errors mustBe Seq(FormError(fieldName, invalidKey, Seq(euVatNumberPattern)))
    }

    "bind lowercase letters" in {
      val dataItem = "aa123456789"
      val result = form.bind(Map(fieldName -> dataItem)).apply(fieldName)
      result.value.value mustBe dataItem
      result.errors mustBe empty
    }
  }
}
