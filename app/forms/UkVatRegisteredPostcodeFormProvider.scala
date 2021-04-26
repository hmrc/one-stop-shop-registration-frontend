package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class UkVatRegisteredPostcodeFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("ukVatRegisteredPostcode.error.required")
        .verifying(maxLength(9, "ukVatRegisteredPostcode.error.length"))
    )
}
