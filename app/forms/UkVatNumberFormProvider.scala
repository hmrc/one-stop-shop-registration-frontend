package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class UkVatNumberFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("ukVatNumber.error.required")
        .verifying(maxLength(11, "ukVatNumber.error.length"))
    )
}
