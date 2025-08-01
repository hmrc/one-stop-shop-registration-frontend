/*
 * Copyright 2024 HM Revenue & Customs
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

package services

import cats.implicits.*
import models.domain.*
import models.previousRegistrations.NonCompliantDetails
import models.{Country, DataMissingError, Index, PreviousScheme, UserAnswers, ValidationResult}
import pages.previousRegistrations.{PreviousEuCountryPage, PreviousOssNumberPage, PreviousSchemePage, PreviouslyRegisteredPage}
import queries.previousRegistration.{AllPreviousRegistrationsRawQuery, AllPreviousSchemesRawQuery, NonCompliantDetailsQuery}

trait PreviousRegistrationsValidations {


  def getPreviousRegistrations(answers: UserAnswers): ValidationResult[List[PreviousRegistration]] = {
    answers.get(PreviouslyRegisteredPage) match {
      case Some(true) =>
        answers.get(AllPreviousRegistrationsRawQuery) match {
          case Some(details) if details.value.nonEmpty =>
            details.value.zipWithIndex.map {
              case (_, index) =>
                processPreviousRegistration(answers, Index(index))
            }.toList.sequence
          case _ =>
            DataMissingError(AllPreviousRegistrationsRawQuery).invalidNec
        }

      case Some(false) =>
        answers.get(AllPreviousRegistrationsRawQuery) match {
          case Some(_) => DataMissingError(AllPreviousRegistrationsRawQuery).invalidNec
          case None => List.empty.validNec
        }

      case None =>
        DataMissingError(PreviouslyRegisteredPage).invalidNec
    }
  }

  private def processPreviousRegistration(answers: UserAnswers, index: Index): ValidationResult[PreviousRegistration] = {
    (
      getPreviousCountry(answers, index),
      getPreviousSchemes(answers, index)
    ).mapN((previousCountry, previousSchemes) =>
      PreviousRegistrationNew(previousCountry, previousSchemes)
    )
  }

  private def getPreviousCountry(answers: UserAnswers, countryIndex: Index): ValidationResult[Country] =
    answers.get(PreviousEuCountryPage(countryIndex)) match {
      case Some(country) =>
        country.validNec
      case None =>
        DataMissingError(PreviousEuCountryPage(countryIndex)).invalidNec
    }

  private def getPreviousScheme(answers: UserAnswers, countryIndex: Index, schemeIndex: Index): ValidationResult[PreviousScheme] =
    answers.get(PreviousSchemePage(countryIndex, schemeIndex)) match {
      case Some(scheme) =>
        scheme.validNec
      case None =>
        DataMissingError(PreviousSchemePage(countryIndex, schemeIndex)).invalidNec
    }

  private def getPreviousSchemeNumber(answers: UserAnswers, countryIndex: Index, schemeIndex: Index): ValidationResult[PreviousSchemeNumbers] =
    answers.get(PreviousOssNumberPage(countryIndex, schemeIndex)) match {
      case Some(vatNumber) =>
        vatNumber.validNec
      case None =>
        DataMissingError(PreviousOssNumberPage(countryIndex, schemeIndex)).invalidNec
    }

  private def getPreviousSchemes(answers: UserAnswers, countryIndex: Index): ValidationResult[List[PreviousSchemeDetails]] = {
    answers.get(AllPreviousSchemesRawQuery(countryIndex)) match {
      case Some(previousSchemes) if previousSchemes.value.nonEmpty =>
        previousSchemes.value.zipWithIndex.map {
          case (_, index) =>
            processPreviousSchemes(answers, countryIndex, Index(index))
        }.toList.sequence
      case _ =>
        DataMissingError(AllPreviousSchemesRawQuery(countryIndex)).invalidNec
    }
  }

  private def processPreviousSchemes(answers: UserAnswers, countryIndex: Index, schemeIndex: Index): ValidationResult[PreviousSchemeDetails] = {
    (
      getPreviousScheme(answers, countryIndex, schemeIndex),
      getPreviousSchemeNumber(answers, countryIndex, schemeIndex),
      getNonCompliantDetails(answers, countryIndex, schemeIndex)
    ).mapN((previousScheme, previousSchemeNumber, nonCompliantDetails) =>
      PreviousSchemeDetails(previousScheme, previousSchemeNumber, nonCompliantDetails)
    )
  }
  
  private def getNonCompliantDetails(answers: UserAnswers, countryIndex: Index, schemeIndex: Index): ValidationResult[Option[NonCompliantDetails]] = {
    answers.get(NonCompliantDetailsQuery(countryIndex, schemeIndex)) match {
      case Some(nonCompliantDetails) =>
        Some(nonCompliantDetails).validNec
      case None =>
        None.validNec
    }
  }
}
