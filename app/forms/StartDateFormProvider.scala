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

import formats.Format.dateFormatter
import forms.mappings.Mappings
import models.StartDateOption.{EarlierDate, NextPeriod}
import models.{StartDate, StartDateOption}
import play.api.data.Form
import play.api.data.Forms.mapping
import services.StartDateService
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual

import java.time.LocalDate
import javax.inject.Inject

class StartDateFormProvider @Inject()(startDateService: StartDateService) extends Mappings {

  def apply(): Form[StartDate] =
    Form(
      mapping(
        "choice" -> enumerable[StartDateOption]("startDate.choice.error.required"),
        "earlierDate" -> mandatoryIfEqual("choice", EarlierDate.toString, localDate(
          invalidKey     = "startDate.earlierDate.error.invalid",
          allRequiredKey = "startDate.earlierDate.error.allRequired",
          twoRequiredKey = "startDate.earlierDate.error.twoRequired",
          requiredKey    = "startDate.earlierDate.error.required"
        )
        .verifying(minDate(startDateService.earliestAlternativeDate, "startDate.earlierDate.error.minDate", startDateService.earliestAlternativeDate.format(dateFormatter)))
        .verifying(maxDate(startDateService.latestAlternativeDate, "startDate.earlierDate.error.maxDate", startDateService.latestAlternativeDate.format(dateFormatter)))
        ))(a)(u)
    )

  private def a(choice: StartDateOption, earlierDate: Option[LocalDate]): StartDate = (choice, earlierDate) match {
    case (NextPeriod, _)           => StartDate(NextPeriod, startDateService.startOfNextPeriod)
    case (EarlierDate, Some(date)) => StartDate(EarlierDate, date)
    case (EarlierDate, None)       => throw new IllegalArgumentException("Tried to bind a form for an earlier date, but no date was supplied")
  }

  private def u(startDate: StartDate): Option[(StartDateOption, Option[LocalDate])] = startDate.option match {
    case NextPeriod  => Some((NextPeriod, None))
    case EarlierDate => Some((EarlierDate, Some(startDate.date)))
  }
}
