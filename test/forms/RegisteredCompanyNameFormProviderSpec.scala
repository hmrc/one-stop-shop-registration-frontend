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

import forms.Validation.Validation.{commonTextPattern, noDoubleSpaces, noLeadingOrTrailingSpaces}
import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class RegisteredCompanyNameFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "registeredCompanyName.error.required"
  val lengthKey = "registeredCompanyName.error.length"
  val invalidKey = "registeredCompanyName.error.invalid"
  val leadingTrailingSpacesKey = "registeredCompanyName.error.leadingtrailing"
  val doubleSpacesKey = "registeredCompanyName.error.doublespaces"
  val validData = "Delicious Chocolate Co"
  val maxLength = 100

  val formProvider = new RegisteredCompanyNameFormProvider()
  val form = formProvider()

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
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like stringFieldWithSpacesRules(
      form,
      fieldName,
      leadingTrailingSpacesError = FormError(fieldName, leadingTrailingSpacesKey, Seq(noLeadingOrTrailingSpaces)),
      doubleSpacesError = FormError(fieldName, doubleSpacesKey, Seq(noDoubleSpaces))
    )

    "must not bind invalid Registered Company Name" in {
      val invalidRegisteredCompanyName = "Invalid%comp@ny name?*]"
      val result = form.bind(Map(fieldName -> invalidRegisteredCompanyName)).apply(fieldName)
      result.errors mustBe Seq(FormError(fieldName, invalidKey, Seq(commonTextPattern)))
    }
  }
}
