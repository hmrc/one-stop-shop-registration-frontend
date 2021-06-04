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

package forms.euVatDetails

import forms.mappings.Mappings
import models.Country
import play.api.data.Form

import javax.inject.Inject

class FixedEstablishmentTradingNameFormProvider @Inject() extends Mappings {

  val fixedEstablishmentTradingNamePattern = """^[A-Za-z0-9À-ÿ \!\)\(.,_/’'"&-]+$"""

  def apply(country: Country): Form[String] =
    Form(
      "value" -> text("fixedEstablishmentTradingName.error.required", Seq(country.name))
        .verifying(firstError(
          maxLength(100, "fixedEstablishmentTradingName.error.length"),
          regexp(fixedEstablishmentTradingNamePattern, "fixedEstablishmentTradingName.error.invalid")))
    )
}
