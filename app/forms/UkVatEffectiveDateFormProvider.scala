package forms

import java.time.LocalDate

import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class UkVatEffectiveDateFormProvider @Inject() extends Mappings {

  def apply(): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey     = "ukVatEffectiveDate.error.invalid",
        allRequiredKey = "ukVatEffectiveDate.error.required.all",
        twoRequiredKey = "ukVatEffectiveDate.error.required.two",
        requiredKey    = "ukVatEffectiveDate.error.required"
      )
    )
}
