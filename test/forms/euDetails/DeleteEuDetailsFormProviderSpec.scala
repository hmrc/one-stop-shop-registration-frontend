/*
 * Copyright 2024 HM Revenue & Customs
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

import forms.behaviours.BooleanFieldBehaviours
import models.Country
import models.euDetails.{EuConsumerSalesMethod, EuDetails, RegistrationType}
import play.api.data.FormError

class DeleteEuDetailsFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "deleteEuVatDetails.error.required"
  val invalidKey = "error.boolean"

  private val country = Country.euCountries.head
  private val euVatDetails =
    EuDetails(
      country, sellsGoodsToEUConsumers = true, Some(EuConsumerSalesMethod.DispatchWarehouse), Some(RegistrationType.TaxId), vatRegistered = Some(false), None, Some("12345678"), None, None, None, None)
  val form = new DeleteEuDetailsFormProvider()(euVatDetails.euCountry.name)

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey, Seq(euVatDetails.euCountry.name))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(euVatDetails.euCountry.name))
    )
  }
}
