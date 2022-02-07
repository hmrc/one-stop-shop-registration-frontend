/*
 * Copyright 2022 HM Revenue & Customs
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

package utils

import models.Index
import models.euDetails.EuOptionalDetails
import models.requests.AuthenticatedDataRequest
import play.api.mvc.{AnyContent, Result}
import queries.EuOptionalDetailsQuery

trait CompletionChecks {


  protected def withCompleteDataModel[A](index: Index, data: Index => Option[A], onFailure: Option[A] => Result)
                                   (onSuccess: => Result)
                                   (implicit request: AuthenticatedDataRequest[AnyContent]): Result = {

    val incomplete = data(index)
    if (incomplete.isEmpty) {
      onSuccess
    } else {
      onFailure(incomplete)
    }
  }


  def getIncompleteEuDetails(index: Index)(implicit request: AuthenticatedDataRequest[AnyContent]): Option[EuOptionalDetails] = {
    request.userAnswers
      .get(EuOptionalDetailsQuery(index))
      .find(details =>
        details.vatRegistered.isEmpty ||
          details.hasFixedEstablishment.isEmpty ||
          (details.vatRegistered.contains(true) && details.euVatNumber.isEmpty) ||
          (details.hasFixedEstablishment.contains(true) &&
            (details.fixedEstablishmentTradingName.isEmpty || details.fixedEstablishmentAddress.isEmpty))
      )

  }
}
