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

import forms.Validation.Validation.{bicPattern, commonTextPattern, ibanPattern}

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.BankDetails

class BankDetailsFormProvider @Inject() extends Mappings {

  def apply(): Form[BankDetails] = Form(
    mapping(
     "accountName" -> text("bankDetails.error.accountName.required")
       .verifying(firstError(
         maxLength(70, "bankDetails.error.accountName.length"),
         regexp(commonTextPattern, "bankDetails.error.accountName.invalid")
       )),
     "bic" -> optional(text("bankDetails.error.bic.required")
       .verifying(firstError(
         stringLengthRange(8, 11, "bankDetails.error.bic.length"),
         regexp(bicPattern, "bankDetails.error.bic.invalid")
       ))),
      "iban" -> iban("bankDetails.error.iban.required", "bankDetails.error.iban.invalid", "bankDetails.error.iban.checksum")
   )(BankDetails.apply)(BankDetails.unapply)
  )
}
