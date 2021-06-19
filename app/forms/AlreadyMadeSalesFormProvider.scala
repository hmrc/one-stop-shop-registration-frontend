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

import forms.mappings.Mappings
import models.AlreadyMadeSales
import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual

import java.time.{Clock, LocalDate}
import javax.inject.Inject

class AlreadyMadeSalesFormProvider @Inject()(clock: Clock) extends Mappings {

  def apply(): Form[AlreadyMadeSales] =
    Form(
      mapping(
        "answer"    -> boolean("alreadyMadeSales.answer.error.required"),
        "firstSale" -> mandatoryIfEqual("answer", true.toString, localDate(
          invalidKey     = "alreadyMadeSales.firstSale.error.invalid",
          allRequiredKey = "alreadyMadeSales.firstSale.error.allRequired",
          twoRequiredKey = "alreadyMadeSales.firstSale.error.twoRequired",
          requiredKey    = "alreadyMadeSales.firstSale.error.required"
        )
        .verifying(maxDate(LocalDate.now(clock), "alreadyMadeSales.firstSale.error.maxDate"))
      ))(a)(u)
    )

  private def a(answer: Boolean, firstSale: Option[LocalDate]): AlreadyMadeSales =
    if (answer) {
      firstSale.map(date => AlreadyMadeSales.Yes(date)).getOrElse(throw new IllegalArgumentException("Cannot create AlreadyMadeSales as Yes without a date"))
    } else {
      AlreadyMadeSales.No
    }

  private def u(a: AlreadyMadeSales): Option[(Boolean, Option[LocalDate])] = a match {
    case AlreadyMadeSales.Yes(date) => Some((true, Some(date)))
    case AlreadyMadeSales.No        => Some((false, None))
  }
}
