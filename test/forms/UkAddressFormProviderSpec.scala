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

package forms

import forms.Validation.Validation.postCodePattern
import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class UkAddressFormProviderSpec extends StringFieldBehaviours {

  val formProvider = new UkAddressFormProvider()
  val form = formProvider()

  ".line1" - {

    val fieldName = "line1"
    val requiredKey = "ukAddress.error.line1.required"
    val lengthKey = "ukAddress.error.line1.length"
    val maxLength = 250

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
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
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".line2" - {

    val fieldName = "line2"
    val lengthKey = "ukAddress.error.line2.length"
    val maxLength = 250

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )
  }

  ".townOrCity" - {

    val fieldName = "townOrCity"
    val requiredKey = "ukAddress.error.townOrCity.required"
    val lengthKey = "ukAddress.error.townOrCity.length"
    val maxLength = 250

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
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
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".county" - {

    val fieldName = "county"
    val lengthKey = "ukAddress.error.county.length"
    val maxLength = 250

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )
  }

  ".postCode" - {

    val fieldName = "postCode"
    val requiredKey = "ukAddress.error.postCode.required"
    val lengthKey = "ukAddress.error.postCode.length"
    val invalidKey = "ukAddress.error.postCode.invalid"
    val validData = "AA11 1AA"
    val maxLength = 250

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validData
    )

    "must not bind invalid Post Code data" in {
      val invalidPostCode = "invalid"
      val result = form.bind(Map(fieldName -> invalidPostCode)).apply(fieldName)
      result.errors mustBe Seq(FormError(fieldName, invalidKey, Seq(postCodePattern)))
    }

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}