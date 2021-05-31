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

package services

import models.UserAnswers
import models.domain.{EuVatRegistration, FixedEstablishment, Registration}
import pages._
import queries.{AllEuVatDetailsQuery, AllTradingNames, AllWebsites}
import uk.gov.hmrc.domain.Vrn

class RegistrationService {

  def fromUserAnswers(userAnswers: UserAnswers, vrn: Vrn): Option[Registration] =
    for {
      registeredCompanyName   <- userAnswers.get(RegisteredCompanyNamePage)
      tradingNames            = getTradingNames(userAnswers)
      partOfVatGroup          <- userAnswers.get(PartOfVatGroupPage)
      ukVatEffectiveDate      <- userAnswers.get(UkVatEffectiveDatePage)
      ukVatRegisteredPostcode <- userAnswers.get(UkVatRegisteredPostcodePage)
      euVatRegistrations      = buildEuVatRegistrations(userAnswers)
      startDate               <- userAnswers.get(StartDatePage)
      businessAddress         <- userAnswers.get(BusinessAddressPage)
      businessContactDetails  <- userAnswers.get(BusinessContactDetailsPage)
      websites                <- userAnswers.get(AllWebsites)
    } yield Registration(
      vrn,
      registeredCompanyName,
      tradingNames,
      partOfVatGroup,
      ukVatEffectiveDate,
      ukVatRegisteredPostcode,
      euVatRegistrations,
      businessAddress,
      businessContactDetails,
      websites,
      startDate.date
    )

  private def getTradingNames(userAnswers: UserAnswers): List[String] =
    userAnswers.get(AllTradingNames).getOrElse(List.empty)

  private def buildEuVatRegistrations(answers: UserAnswers): List[EuVatRegistration] =
    answers
      .get(AllEuVatDetailsQuery).getOrElse(List.empty)
      .map {
        detail =>
          val fixedEstablishment = (detail.fixedEstablishmentTradingName, detail.fixedEstablishmentAddress) match {
            case (Some(tradingName), Some(address)) =>
              Some(FixedEstablishment(tradingName, address))
            case _ => None
          }

          EuVatRegistration(detail.euCountry, detail.euVatNumber, fixedEstablishment)
    }
}
