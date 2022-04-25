package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form

class BusinessTaxIdNumberFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> text("businessTaxIdNumber.error.required")
        .verifying(maxLength(100, "businessTaxIdNumber.error.length"))
    )
}
