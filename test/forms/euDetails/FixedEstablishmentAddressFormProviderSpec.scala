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

import org.scalacheck.Arbitrary.arbitrary
import forms.Validation.Validation.postcodePattern
import forms.behaviours.StringFieldBehaviours
import models.Country
import play.api.data.FormError

class FixedEstablishmentAddressFormProviderSpec extends StringFieldBehaviours {

  private val country = arbitrary[Country].sample.value
  private val formProvider = new FixedEstablishmentAddressFormProvider()
  private val form = formProvider(country)

  ".line1" - {

    val fieldName = "line1"
    val requiredKey = "fixedEstablishmentAddress.error.line1.required"
    val lengthKey = "fixedEstablishmentAddress.error.line1.length"
    val maxLength = 100

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
    val lengthKey = "fixedEstablishmentAddress.error.line2.length"
    val maxLength = 100

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
    val requiredKey = "fixedEstablishmentAddress.error.townOrCity.required"
    val lengthKey = "fixedEstablishmentAddress.error.townOrCity.length"
    val maxLength = 100

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
  
  ".stateOrRegion" - {

    val fieldName = "stateOrRegion"
    val lengthKey = "fixedEstablishmentAddress.error.stateOrRegion.length"
    val maxLength = 100

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
    val lengthKey = "fixedEstablishmentAddress.error.postCode.length"
    val invalidKey = "fixedEstablishmentAddress.error.postCode.invalid"
    val maxLength = 100
    val validData = "75700 PARIS CEDEX"

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

    "must not bind invalid postcode" in {
      val invalidPostcode = "*@[]%abc"
      val result = form.bind(Map(fieldName -> invalidPostcode)).apply(fieldName)
      result.errors mustBe Seq(FormError(fieldName, invalidKey, Seq(postcodePattern)))
    }
  }
}
