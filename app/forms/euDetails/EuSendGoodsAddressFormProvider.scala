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

package forms.euDetails

import forms.Validation.Validation.{commonTextPattern, postcodePattern}
import forms.mappings.Mappings
import models.{Country, InternationalAddress}
import play.api.data.Form
import play.api.data.Forms.{mapping, optional}

import javax.inject.Inject

class EuSendGoodsAddressFormProvider @Inject() extends Mappings {

  def apply(country: Country): Form[InternationalAddress] =
    Form(
      mapping(
        "line1" -> text("euSendGoodsAddress.error.line1.required")
          .verifying(maxLength(35, "euSendGoodsAddress.error.line1.length"))
          .verifying(regexp(commonTextPattern, "euSendGoodsAddress.error.line1.format")),
        "line2" -> optional(text("euSendGoodsAddress.error.line2.required")
          .verifying(maxLength(35, "euSendGoodsAddress.error.line2.length"))
          .verifying(regexp(commonTextPattern, "euSendGoodsAddress.error.line2.format"))),
        "townOrCity" -> text("euSendGoodsAddress.error.townOrCity.required")
          .verifying(maxLength(35, "euSendGoodsAddress.error.townOrCity.length"))
          .verifying(regexp(commonTextPattern, "euSendGoodsAddress.error.townOrCity.format")),
        "stateOrRegion" -> optional(text("euSendGoodsAddress.error.stateOrRegion.required")
          .verifying(maxLength(35, "euSendGoodsAddress.error.stateOrRegion.length"))
          .verifying(regexp(commonTextPattern, "euSendGoodsAddress.error.stateOrRegion.format"))),
        "postCode" -> optional(text("euSendGoodsAddress.error.postCode.required")
          .verifying(firstError(
            maxLength(50, "euSendGoodsAddress.error.postCode.length"),
            regexp(postcodePattern, "euSendGoodsAddress.error.postCode.invalid"))))
      )(InternationalAddress(_, _, _, _, _, country))(x => Some((x.line1, x.line2, x.townOrCity, x.stateOrRegion, x.postCode)))
    )
}
