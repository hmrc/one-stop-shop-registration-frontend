package forms.euDetails

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class DeleteAllEuDetailsFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "deleteAllEuDetails.error.required"
  val invalidKey = "error.boolean"

  val form = new DeleteAllEuDetailsFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
