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

package queries

import models.{Index, UserAnswers}
import models.euDetails.EuDetails
import pages.{CurrentCountryOfRegistrationPage, CurrentlyRegisteredInCountryPage, CurrentlyRegisteredInEuPage}
import play.api.libs.json.JsPath

import scala.util.Try

case class EuDetailsQuery(index: Index) extends Gettable[EuDetails] with Settable[EuDetails] {

  override def path: JsPath = JsPath \ "euDetails" \ index.position

  override def cleanup(value: Option[EuDetails], userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.get(AllEuDetailsQuery).map(_.filter(_.vatRegistered)) match {
      case Some(Nil) | None =>
        userAnswers
          .remove(CurrentlyRegisteredInCountryPage)
          .flatMap(_.remove(CurrentlyRegisteredInEuPage))
          .flatMap(_.remove(CurrentCountryOfRegistrationPage))

      case Some(singleItem :: Nil) =>
        if (userAnswers.get(CurrentCountryOfRegistrationPage).contains(singleItem.euCountry)) {
          super.cleanup(value, userAnswers)
        } else {
          userAnswers
            .remove(CurrentlyRegisteredInEuPage)
            .flatMap(_.remove(CurrentCountryOfRegistrationPage))
        }

      case Some(items) =>
        userAnswers.get(CurrentCountryOfRegistrationPage).map {
          country =>
            if (items.map(_.euCountry) contains country) {
              super.cleanup(value, userAnswers)
            } else {
              userAnswers
                .remove(CurrentlyRegisteredInEuPage)
                .flatMap(_.remove(CurrentCountryOfRegistrationPage))
            }
        } getOrElse {
          super.cleanup(value, userAnswers)
        }
    }
}
