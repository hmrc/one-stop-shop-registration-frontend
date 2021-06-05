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

import forms.Validation.Validation.postcodePattern
import forms.mappings.Mappings
import models.euVatDetails.FixedEstablishmentAddress
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class FixedEstablishmentAddressFormProvider @Inject() extends Mappings {

   def apply(): Form[FixedEstablishmentAddress] = Form(
     mapping(
       "line1" -> text("fixedEstablishmentAddress.error.line1.required")
         .verifying(maxLength(100, "fixedEstablishmentAddress.error.line1.length")),
       "line2" -> optional(text("fixedEstablishmentAddress.error.line2.required")
         .verifying(maxLength(100, "fixedEstablishmentAddress.error.line2.length"))),
       "townOrCity" -> text("fixedEstablishmentAddress.error.townOrCity.required")
         .verifying(maxLength(100, "fixedEstablishmentAddress.error.townOrCity.length")),
       "county" -> optional(text("fixedEstablishmentAddress.error.county.required")
         .verifying(maxLength(100, "fixedEstablishmentAddress.error.county.length"))),
       "postCode" -> optional(text("fixedEstablishmentAddress.error.postCode.required")
         .verifying(firstError(
           maxLength(100, "fixedEstablishmentAddress.error.postCode.length"),
           regexp(postcodePattern, "fixedEstablishmentAddress.error.postCode.invalid"))))
     )(FixedEstablishmentAddress.apply)(FixedEstablishmentAddress.unapply)
   )
 }
