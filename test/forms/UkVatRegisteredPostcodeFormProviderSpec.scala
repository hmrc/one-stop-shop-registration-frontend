package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class UkVatRegisteredPostcodeFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "ukVatRegisteredPostcode.error.required"
  val lengthKey = "ukVatRegisteredPostcode.error.length"
  val maxLength = 9

  val form = new UkVatRegisteredPostcodeFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
