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

import forms.Validation.Validation.postCodePattern

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.UkAddress

class BusinessAddressFormProvider @Inject() extends Mappings {

   def apply(): Form[UkAddress] = Form(
     mapping(
      "line1" -> text("businessAddress.error.line1.required")
        .verifying(maxLength(250, "businessAddress.error.line1.length")),
      "line2" -> optional(text("businessAddress.error.line2.required")
        .verifying(maxLength(250, "businessAddress.error.line2.length"))),
       "townOrCity" -> text("businessAddress.error.townOrCity.required")
         .verifying(maxLength(250, "businessAddress.error.townOrCity.length")),
       "county" -> optional(text("businessAddress.error.county.required")
         .verifying(maxLength(250, "businessAddress.error.county.length"))),
       "postCode" -> text("businessAddress.error.postCode.required")
         .verifying(firstError(
           maxLength(250, "businessAddress.error.postCode.length"),
           regexp(postCodePattern, "businessAddress.error.postCode.invalid")))
    )(UkAddress.apply)(UkAddress.unapply)
   )
 }
