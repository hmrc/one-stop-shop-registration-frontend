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

import org.scalacheck.Arbitrary.arbitrary
import forms.Validation.Validation.bicPattern
import forms.behaviours.StringFieldBehaviours
import models.Iban
import org.scalacheck.Gen
import play.api.data.FormError

class BankDetailsFormProviderSpec extends StringFieldBehaviours {

  val form = new BankDetailsFormProvider()()

  ".accountName" - {

    val fieldName = "accountName"
    val requiredKey = "bankDetails.error.accountName.required"
    val lengthKey = "bankDetails.error.accountName.length"
    val maxLength = 70

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
    val lengthKey = "bankDetails.error.bic.length"
    val minLength = 8
    val maxLength = 11

    val validData = Gen.listOfN(maxLength, Gen.oneOf(Gen.numChar, Gen.alphaUpperChar)).map(_.mkString)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validData
    )

    s"not bind strings outside range of $minLength and $maxLength characters in length" in {
      forAll(stringsOutsideOfLengthRange(minLength, maxLength)) {
        invalidInput =>
          val result = form.bind(Map(fieldName -> invalidInput)).apply(fieldName)

          result.errors must contain(FormError(fieldName, lengthKey, Seq(minLength, maxLength)))
      }
    }

    s"bind strings inside range of $minLength and $maxLength characters in length" in {

      forAll(alphaNumStringWithLength(minLength, maxLength)) {
        validInput =>
          val result = form.bind(Map(fieldName -> validInput)).apply(fieldName)
          result.errors mustBe empty
      }
    }

    "not bind any strings containing characters other than digits or alpha characters" in {

      val result = form.bind(Map(fieldName -> "invalid.")).apply(fieldName)
      result.errors must contain only FormError(fieldName, invalidKey, Seq(bicPattern))
    }
  }

  ".iban" - {

    val fieldName = "iban"
    val requiredKey = "bankDetails.error.iban.required"
    val invalidKey = "bankDetails.error.iban.invalid"
    val checksumKey = "bankDetails.error.iban.checksum"

    val validData = arbitrary[Iban].map(_.toString)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validData
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not bind values in the wrong format" in {

      forAll(arbitrary[String] suchThat (_.trim.nonEmpty)) {
        value =>

          whenever(Iban(value).isLeft) {
            val result = form.bind(Map(fieldName -> value)).apply(fieldName)
            result.errors mustEqual Seq(FormError(fieldName, invalidKey))
          }
      }
    }

    "must not bind values with an invalid checksum" in {

      forAll(arbitrary[Iban]) {
        iban =>
          val value = iban.toString.take(2) + "00" + iban.toString.substring(4)
          val result = form.bind(Map(fieldName -> value)).apply(fieldName)
          result.errors mustEqual Seq(FormError(fieldName, checksumKey))
      }
    }
  }
}
