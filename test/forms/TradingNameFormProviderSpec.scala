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
import models.Index
import play.api.data.FormError

class TradingNameFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "tradingName.error.required"
  val lengthKey = "tradingName.error.length"
  val invalidKey = "tradingName.error.invalid"
  val maxLength = 160
  val index = Index(0)
  val emptyExistingAnswers = Seq.empty[String]
  val validData = "Delicious Chocolate Company"

  val formProvider: TradingNameFormProvider = new TradingNameFormProvider()
  val form = formProvider(index, emptyExistingAnswers)

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

    "must fail to bind when given a duplicate value" in {
      val existingAnswers = Seq("foo", "bar")
      val answer = "bar"
      val form = new TradingNameFormProvider()(index, existingAnswers)

      val result = form.bind(Map(fieldName ->  answer)).apply(fieldName)
      result.errors must contain only FormError(fieldName, "tradingName.error.duplicate")
    }

    "must not bind invalid Trading Name" in {
      val invalidTradingName = "^Invalid~ tr@ding=nam£"
      val result = form.bind(Map(fieldName -> invalidTradingName)).apply(fieldName)
      result.errors mustBe Seq(FormError(fieldName, invalidKey, Seq(formProvider.tradingNamePattern)))
    }
  }
}
