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

import forms.behaviours.OptionFieldBehaviours
import models.Country
import models.euDetails.EuConsumerSalesMethod
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.FormError

class SellsGoodsToEUConsumerMethodFormProviderSpec extends OptionFieldBehaviours {

  val requiredKey = "sellsGoodsToEUConsumerMethod.error.required"
  val country: Country = arbitrary[Country].sample.value
  val form = new SellsGoodsToEUConsumerMethodFormProvider()(country)

  ".value" - {

    val fieldName = "value"

    behave like optionsField[EuConsumerSalesMethod](
      form,
      fieldName,
      validValues = EuConsumerSalesMethod.values,
      invalidError = FormError(fieldName, "error.invalid", Seq(country.name))

    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(country.name))
    )
  }
}
