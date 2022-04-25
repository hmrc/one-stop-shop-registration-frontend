package forms.euDetails

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class EuSendGoodsFormProvider @Inject() extends Mappings {

  def apply(): Form[Boolean] =
    Form(
      "value" -> boolean("euSendGoods.error.required")
    )
}
