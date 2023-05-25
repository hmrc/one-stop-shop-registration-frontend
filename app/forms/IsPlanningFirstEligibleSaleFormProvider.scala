/*
 * Copyright 2023 HM Revenue & Customs
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

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import services.DateService
import formats.Format.dateFormatter

import java.time.{Clock, LocalDate}

class IsPlanningFirstEligibleSaleFormProvider @Inject()(dateService: DateService, clock: Clock) extends Mappings {

  def apply(maybeRegistrationDate: Option[LocalDate]): Form[Boolean] = {

    val firstDayOfNextCalendarQuarter =
      maybeRegistrationDate match {
        case Some(registrationDate) =>
          dateService.startOfNextQuarter(registrationDate)
        case _ =>
          dateService.startOfNextQuarter(LocalDate.now(clock))
      }

    Form(
      "value" -> boolean(
        "isPlanningFirstEligibleSale.error.required", args = Seq(firstDayOfNextCalendarQuarter.format(dateFormatter)))
    )
  }
}
