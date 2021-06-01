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

package viewmodels

import models.Country
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

class CurrentCountryOfRegistrationViewModel(countries: Seq[Country]) {

  def options(implicit messages: Messages): Seq[RadioItem] =
    countries.sortBy(_.name).zipWithIndex.map {
      case (value, index) =>
        RadioItem(
          content = Text(value.name),
          value   = Some(value.code),
          id      = Some(s"value_$index")
        )
    }
}
