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

import forms.Validation.Validation.bicPattern
import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

class BankDetailsFormProviderSpec extends StringFieldBehaviours {

  val form = new BankDetailsFormProvider()()

  ".accountName" - {

    val fieldName = "accountName"
    val requiredKey = "bankDetails.error.accountName.required"
    val lengthKey = "bankDetails.error.accountName.length"
    val maxLength = 100

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeInputsWithMaxLength(maxLength)
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

  ".bic" - {

    val fieldName = "bic"
    val invalidKey = "bankDetails.error.bic.invalid"
    val maxLength = 11

    val validData = Gen.listOfN(maxLength, Gen.oneOf(Gen.numChar, Gen.alphaUpperChar)).map(_.mkString)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validData
    )

    "not bind any strings containing characters other than digits or alpha characters" in {

      val result = form.bind(Map(fieldName -> "invalid.")).apply(fieldName)
      result.errors must contain only FormError(fieldName, invalidKey, Seq(bicPattern))
    }
  }

  ".iban" - {

    val fieldName = "iban"
    val requiredKey = "bankDetails.error.iban.required"
    val maxKey = "bankDetails.error.iban.max"
    val minKey = "bankDetails.error.iban.min"
    val maxLength = 34
    val minLength = 5

    val validData = for {
      ibanChars <- Gen.choose(minLength, maxLength)
      iban <- Gen.listOfN(ibanChars, Gen.oneOf(Gen.alphaChar, Gen.numChar))
    } yield iban.mkString

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validData
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, maxKey, Seq(maxLength))
    )

    behave like fieldWithMinLength(
      form,
      fieldName,
      minLength = minLength,
      lengthError = FormError(fieldName, minKey, Seq(minLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
