package forms.euDetails

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class EuSendGoodsFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "euSendGoods.error.required"
  val invalidKey = "error.boolean"

  val form = new EuSendGoodsFormProvider()()

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
