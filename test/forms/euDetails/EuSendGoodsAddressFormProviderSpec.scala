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

package forms.euDetails

import forms.Validation.Validation.{commonTextPattern, postcodePattern}
import forms.behaviours.StringFieldBehaviours
import models.Country
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.FormError

class EuSendGoodsAddressFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "euSendGoodsAddress.error.required"
  val lengthKey = "euSendGoodsAddress.error.length"
  val maxLength = 100

  private val country = arbitrary[Country].sample.value

  val formProvider = new EuSendGoodsAddressFormProvider()
  val form = formProvider(country)

  ".line1" - {

    val fieldName = "line1"
    val requiredKey = "euSendGoodsAddress.error.line1.required"
    val lengthKey = "euSendGoodsAddress.error.line1.length"
    val formatKey = "euSendGoodsAddress.error.line1.format"
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
    val lengthKey = "euSendGoodsAddress.error.line2.length"
    val formatKey = "euSendGoodsAddress.error.line2.format"
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
    val requiredKey = "euSendGoodsAddress.error.townOrCity.required"
    val lengthKey = "euSendGoodsAddress.error.townOrCity.length"
    val formatKey = "euSendGoodsAddress.error.townOrCity.format"
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

  ".stateOrRegion" - {

    val fieldName = "stateOrRegion"
    val lengthKey = "euSendGoodsAddress.error.stateOrRegion.length"
    val formatKey = "euSendGoodsAddress.error.stateOrRegion.format"
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
    val lengthKey = "euSendGoodsAddress.error.postCode.length"
    val invalidKey = "euSendGoodsAddress.error.postCode.invalid"
    val maxLength = 40
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
