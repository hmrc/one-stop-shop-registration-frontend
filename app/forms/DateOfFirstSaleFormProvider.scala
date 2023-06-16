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

import formats.Format.dateFormatter
import forms.mappings.Mappings
import models.requests.AuthenticatedDataRequest
import play.api.data.Form
import services.DateService

import java.time.{Clock, LocalDate}
import javax.inject.Inject

class DateOfFirstSaleFormProvider @Inject()(
                                             dateService: DateService,
                                             clock: Clock
                                           ) extends Mappings {

  def apply(maximumDate: LocalDate = LocalDate.now(clock))(implicit request: AuthenticatedDataRequest[_]): Form[LocalDate] = {

    val minimumDate = request.registration.flatMap(_.submissionReceived) match {
      case Some(submissionReceived) =>
        dateService.earliestSaleAllowed(submissionReceived.atZone(clock.getZone).toLocalDate)
      case _ =>
        dateService.earliestSaleAllowed()
    }

    val today = LocalDate.now(clock)

    Form(
      "value" -> localDate(
        invalidKey = "dateOfFirstSale.error.invalid",
        allRequiredKey = "dateOfFirstSale.error.required.all",
        twoRequiredKey = "dateOfFirstSale.error.required.two",
        requiredKey = "dateOfFirstSale.error.required"
      )
        .verifying(
          if (maximumDate != today) {
            minDate(minimumDate, "dateOfFirstSale.error.minMax", minimumDate.format(dateFormatter), maximumDate.format(dateFormatter))
          } else {
            minDate(minimumDate, "dateOfFirstSale.error.minMaxToday", minimumDate.format(dateFormatter))
          }
        )
        .verifying(
          if (maximumDate != today) {
            maxDate(maximumDate, "dateOfFirstSale.error.minMax", minimumDate.format(dateFormatter), maximumDate.format(dateFormatter))
          } else {
            maxDate(today, "dateOfFirstSale.error.minMaxToday", minimumDate.format(dateFormatter))
          }
        )
    )
  }
}
