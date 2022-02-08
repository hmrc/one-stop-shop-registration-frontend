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

import models.{CheckMode, Country, Index}
import models.euDetails.EuOptionalDetails
import models.previousRegistrations.PreviousRegistrationDetailsWithOptionalVatNumber
import models.requests.AuthenticatedDataRequest
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import queries.{AllEuOptionalDetailsQuery, AllPreviousRegistrationsWithOptionalVatNumberQuery, EuOptionalDetailsQuery}

import scala.concurrent.Future

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

  protected def withCompleteDataAsync[A](data: () => Seq[A], onFailure: Seq[A] => Future[Result])
                                        (onSuccess: => Future[Result])
                                        (implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] = {

    val incomplete = data()
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

  def getAllIncompleteEuDetails()(implicit request: AuthenticatedDataRequest[AnyContent]): Seq[EuOptionalDetails] = {
    request.userAnswers
      .get(AllEuOptionalDetailsQuery).map(
      _.filter(details =>
        details.vatRegistered.isEmpty ||
          details.hasFixedEstablishment.isEmpty ||
          (details.vatRegistered.contains(true) && details.euVatNumber.isEmpty) ||
          (details.hasFixedEstablishment.contains(true) &&
            (details.fixedEstablishmentTradingName.isEmpty || details.fixedEstablishmentAddress.isEmpty))
      )
    ).getOrElse(List.empty)
  }

  def getAllIncompleteDeregisteredDetails()(implicit request: AuthenticatedDataRequest[AnyContent]): Seq[PreviousRegistrationDetailsWithOptionalVatNumber] = {
    request.userAnswers
      .get(AllPreviousRegistrationsWithOptionalVatNumberQuery).map(
      _.filter(_.previousEuVatNumber.isEmpty)
    ).getOrElse(List.empty)
  }

  def firstIndexedIncompleteDeregisteredCountry(incompleteCountries: Seq[Country])
                                               (implicit request: AuthenticatedDataRequest[AnyContent]): Option[(PreviousRegistrationDetailsWithOptionalVatNumber, Int)] = {
    request.userAnswers.get(AllPreviousRegistrationsWithOptionalVatNumberQuery)
      .getOrElse(List.empty).zipWithIndex
      .find(indexedDetails => incompleteCountries.contains(indexedDetails._1.previousEuCountry))
  }

  def firstIndexedIncompleteEuDetails(incompleteCountries: Seq[Country])
                                     (implicit request: AuthenticatedDataRequest[AnyContent]): Option[(EuOptionalDetails, Int)] = {
    request.userAnswers.get(AllEuOptionalDetailsQuery)
      .getOrElse(List.empty).zipWithIndex
      .find(indexedDetails => incompleteCountries.contains(indexedDetails._1.euCountry))
  }

  def validate()(implicit request: AuthenticatedDataRequest[AnyContent]): Boolean = {
    getAllIncompleteDeregisteredDetails.isEmpty && getAllIncompleteEuDetails.isEmpty
  }

  def getFirstValidationError()(implicit request: AuthenticatedDataRequest[AnyContent]): Option[Result] = {

    val incompleteEuDetails = firstIndexedIncompleteEuDetails(getAllIncompleteEuDetails().map(
      _.euCountry
    )).map(
      incompleteCountry =>
        Redirect(controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(CheckMode, Index(incompleteCountry._2)))
    )

    val incompleteDeregisteredCountry = firstIndexedIncompleteDeregisteredCountry(getAllIncompleteDeregisteredDetails().map(
      _.previousEuCountry
    )).map(
      incompleteCountry =>
        Redirect(controllers.previousRegistrations.routes.PreviousEuVatNumberController.onPageLoad(CheckMode, Index(incompleteCountry._2)))
    )

    (incompleteEuDetails ++ incompleteDeregisteredCountry).headOption
  }

}
