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

package forms

import forms.Validation.Validation.{commonTextPattern, postCodePattern}

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.UkAddress

class UkAddressFormProvider @Inject() extends Mappings {

   def apply(): Form[UkAddress] = Form(
     mapping(
      "line1" -> text("ukAddress.error.line1.required")
        .verifying(firstError(
          maxLength(35, "ukAddress.error.line1.length"),
          regexp(commonTextPattern, "ukAddress.error.line1.invalid"))),
      "line2" -> optional(text("ukAddress.error.line2.required")
        .verifying(firstError(
          maxLength(35, "ukAddress.error.line2.length"),
          regexp(commonTextPattern, "ukAddress.error.line2.invalid")))),
       "townOrCity" -> text("ukAddress.error.townOrCity.required")
       .verifying(firstError(
         maxLength(35, "ukAddress.error.townOrCity.length"),
         regexp(commonTextPattern, "ukAddress.error.townOrCity.invalid"))),
       "county" -> optional(text("ukAddress.error.county.required")
         .verifying(firstError(
           maxLength(35, "ukAddress.error.county.length"),
           regexp(commonTextPattern, "ukAddress.error.county.invalid")))),
       "postCode" -> text("ukAddress.error.postCode.required")
         .verifying(firstError(
           maxLength(10, "ukAddress.error.postCode.length"),
           regexp(postCodePattern, "ukAddress.error.postCode.invalid")))
    )(UkAddress.apply)(UkAddress.unapply)
   )
 }
