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

import forms.Validation.Validation.{commonTextPattern, noDoubleSpaces, noLeadingOrTrailingSpaces, postCodePattern}
import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class UkAddressFormProviderSpec extends StringFieldBehaviours {

  val formProvider = new UkAddressFormProvider()
  val form = formProvider()

  ".line1" - {

    val fieldName = "line1"
    val requiredKey = "ukAddress.error.line1.required"
    val lengthKey = "ukAddress.error.line1.length"
    val invalidKey = "ukAddress.error.line1.invalid"
    val leadingTrailingSpacesKey = "ukAddress.error.line1.leadingtrailing"
    val doubleSpacesKey = "ukAddress.error.line1.doublespaces"
    val maxLength = 35

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

    behave like stringFieldWithSpacesRules(
      form,
      fieldName,
      leadingTrailingSpacesError = FormError(fieldName, leadingTrailingSpacesKey, Seq(noLeadingOrTrailingSpaces)),
      doubleSpacesError = FormError(fieldName, doubleSpacesKey, Seq(noDoubleSpaces))
    )

    "must not bind invalid Line 1" in {
      val invalidLine1 = "^Invalid~ !@=£"
      val result = form.bind(Map(fieldName -> invalidLine1)).apply(fieldName)
      result.errors mustBe Seq(FormError(fieldName, invalidKey, Seq(commonTextPattern)))
    }
  }

  ".line2" - {

    val fieldName = "line2"
    val lengthKey = "ukAddress.error.line2.length"
    val invalidKey = "ukAddress.error.line2.invalid"
    val leadingTrailingSpacesKey = "ukAddress.error.line2.leadingtrailing"
    val doubleSpacesKey = "ukAddress.error.line2.doublespaces"
    val maxLength = 35

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

    behave like stringFieldWithSpacesRules(
      form,
      fieldName,
      leadingTrailingSpacesError = FormError(fieldName, leadingTrailingSpacesKey, Seq(noLeadingOrTrailingSpaces)),
      doubleSpacesError = FormError(fieldName, doubleSpacesKey, Seq(noDoubleSpaces))
    )

    "must not bind invalid Line 2" in {
      val invalidLine2 = "^Invalid~ !@=£"
      val result = form.bind(Map(fieldName -> invalidLine2)).apply(fieldName)
      result.errors mustBe Seq(FormError(fieldName, invalidKey, Seq(commonTextPattern)))
    }
  }

  ".townOrCity" - {

    val fieldName = "townOrCity"
    val requiredKey = "ukAddress.error.townOrCity.required"
    val lengthKey = "ukAddress.error.townOrCity.length"
    val invalidKey = "ukAddress.error.townOrCity.invalid"
    val leadingTrailingSpacesKey = "ukAddress.error.townOrCity.leadingtrailing"
    val doubleSpacesKey = "ukAddress.error.townOrCity.doublespaces"
    val maxLength = 35

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

    behave like stringFieldWithSpacesRules(
      form,
      fieldName,
      leadingTrailingSpacesError = FormError(fieldName, leadingTrailingSpacesKey, Seq(noLeadingOrTrailingSpaces)),
      doubleSpacesError = FormError(fieldName, doubleSpacesKey, Seq(noDoubleSpaces))
    )

    "must not bind invalid Town or City" in {
      val invalidTownOrCity = "^Invalid~ !@=£"
      val result = form.bind(Map(fieldName -> invalidTownOrCity)).apply(fieldName)
      result.errors mustBe Seq(FormError(fieldName, invalidKey, Seq(commonTextPattern)))
    }
  }

  ".county" - {

    val fieldName = "county"
    val lengthKey = "ukAddress.error.county.length"
    val invalidKey = "ukAddress.error.county.invalid"
    val leadingTrailingSpacesKey = "ukAddress.error.county.leadingtrailing"
    val doubleSpacesKey = "ukAddress.error.county.doublespaces"
    val maxLength = 35

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

    behave like stringFieldWithSpacesRules(
      form,
      fieldName,
      leadingTrailingSpacesError = FormError(fieldName, leadingTrailingSpacesKey, Seq(noLeadingOrTrailingSpaces)),
      doubleSpacesError = FormError(fieldName, doubleSpacesKey, Seq(noDoubleSpaces))
    )

    "must not bind invalid County" in {
      val invalidCounty = "^Invalid~ !@=£"
      val result = form.bind(Map(fieldName -> invalidCounty)).apply(fieldName)
      result.errors mustBe Seq(FormError(fieldName, invalidKey, Seq(commonTextPattern)))
    }
  }

  ".postCode" - {

    val fieldName = "postCode"
    val requiredKey = "ukAddress.error.postCode.required"
    val lengthKey = "ukAddress.error.postCode.length"
    val invalidKey = "ukAddress.error.postCode.invalid"
    val validData = "AA11 1AA"
    val maxLength = 10

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
