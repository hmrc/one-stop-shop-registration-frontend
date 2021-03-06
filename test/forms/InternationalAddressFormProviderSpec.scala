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

import forms.Validation.Validation.commonTextPattern
import forms.behaviours.StringFieldBehaviours
import models.Country
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.data.FormError

class InternationalAddressFormProviderSpec extends StringFieldBehaviours {

  val form = new InternationalAddressFormProvider()()

  ".line1" - {

    val fieldName = "line1"
    val requiredKey = "internationalAddress.error.line1.required"
    val lengthKey = "internationalAddress.error.line1.length"
    val formatKey = "internationalAddress.error.line1.format"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like commonTextField(
      form,
      fieldName,
      FormError(fieldName, formatKey, Seq(commonTextPattern)),
      FormError(fieldName, lengthKey, Seq(maxLength)),
      maxLength
    )
  }

  ".line2" - {

    val fieldName = "line2"
    val lengthKey = "internationalAddress.error.line2.length"
    val formatKey = "internationalAddress.error.line2.format"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like commonTextField(
      form,
      fieldName,
      FormError(fieldName, formatKey, Seq(commonTextPattern)),
      FormError(fieldName, lengthKey, Seq(maxLength)),
      maxLength
    )
  }

  ".townOrCity" - {

    val fieldName = "townOrCity"
    val requiredKey = "internationalAddress.error.townOrCity.required"
    val lengthKey = "internationalAddress.error.townOrCity.length"
    val formatKey = "internationalAddress.error.townOrCity.format"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like commonTextField(
      form,
      fieldName,
      FormError(fieldName, formatKey, Seq(commonTextPattern)),
      FormError(fieldName, lengthKey, Seq(maxLength)),
      maxLength
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".stateOrRegion" - {

    val fieldName = "stateOrRegion"
    val lengthKey = "internationalAddress.error.stateOrRegion.length"
    val formatKey = "internationalAddress.error.stateOrRegion.format"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like commonTextField(
      form,
      fieldName,
      FormError(fieldName, formatKey, Seq(commonTextPattern)),
      FormError(fieldName, lengthKey, Seq(maxLength)),
      maxLength
    )
  }

  ".postCode" - {

    val fieldName = "postCode"
    val lengthKey = "internationalAddress.error.postCode.length"
    val maxLength = 50

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

  ".country" - {

    val fieldName = "country"
    val requiredKey = "internationalAddress.error.country.required"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
     Gen.oneOf(Country.internationalCountries).map(_.code)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not bind any values other than valid country codes" in {

      val invalidAnswers = arbitrary[String].retryUntil(x => !Country.internationalCountries.map(_.code).contains(x))

      forAll(invalidAnswers) {
        answer =>
          val result = form.bind(Map("value" -> answer)).apply(fieldName)
          result.errors must contain only FormError(fieldName, requiredKey)
      }
    }
  }
}
