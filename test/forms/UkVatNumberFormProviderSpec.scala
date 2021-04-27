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

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.FormError
import uk.gov.hmrc.domain.Vrn

class UkVatNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "ukVatNumber.error.required"
  val lengthKey = "ukVatNumber.error.length"
  val maxLength = 11

  val formProvider = new UkVatNumberFormProvider()
  val form = formProvider()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      arbitrary[Vrn].map(_.vrn)
    )

    "must not bind invalid data" in {
      val invalidData = "invalid"
      val result = form.bind(Map(fieldName -> invalidData)).apply(fieldName)
      result.errors must contain only FormError(fieldName, "ukVatNumber.error.invalid", Seq(formProvider.pattern))
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
