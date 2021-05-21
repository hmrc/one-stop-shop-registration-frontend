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

package models.requests

import models.{BusinessAddress, BusinessContactDetails, EuVatDetails, UserAnswers}
import pages._
import play.api.libs.json.{Json, OFormat}
import queries.{AllEuVatDetailsQuery, AllTradingNames, AllWebsites}
import uk.gov.hmrc.domain.Vrn

import java.time.LocalDate

final case class RegistrationRequest(
                                      registeredCompanyName: String,
                                      hasTradingName: Boolean,
                                      tradingNames: List[String],
                                      partOfVatGroup: Boolean,
                                      ukVatNumber: Vrn,
                                      ukVatEffectiveDate: LocalDate,
                                      ukVatRegisteredPostcode: String,
                                      vatRegisteredInEu: Boolean,
                                      euVatDetails: Seq[EuVatDetails],
                                      businessAddress: BusinessAddress,
                                      businessContactDetails: BusinessContactDetails,
                                      websites: Seq[String]
  )

  case object RegistrationRequest {
    implicit val format: OFormat[RegistrationRequest] = Json.format[RegistrationRequest]

    def buildRegistrationRequest(userAnswers: UserAnswers): Option[RegistrationRequest] = {

      for {
        registeredCompanyName       <- userAnswers.get(RegisteredCompanyNamePage)
        hasTradingName              <- userAnswers.get(HasTradingNamePage)
        tradingNames                <- userAnswers.get(AllTradingNames)
        partOfVatGroup              <- userAnswers.get(PartOfVatGroupPage)
        ukVatNumber                 <- userAnswers.get(UkVatNumberPage)
        ukVatEffectiveDate          <- userAnswers.get(UkVatEffectiveDatePage)
        ukVatRegisteredPostcode     <- userAnswers.get(UkVatRegisteredPostcodePage)
        vatRegisteredInEu           <- userAnswers.get(VatRegisteredInEuPage)
        euVatDetails                <- userAnswers.get(AllEuVatDetailsQuery)
        businessAddress             <- userAnswers.get(BusinessAddressPage)
        businessContactDetails      <- userAnswers.get(BusinessContactDetailsPage)
        websites                    <- userAnswers.get(AllWebsites)
      } yield
        RegistrationRequest(
          registeredCompanyName,
          hasTradingName,
          tradingNames,
          partOfVatGroup,
          ukVatNumber,
          ukVatEffectiveDate,
          ukVatRegisteredPostcode,
          vatRegisteredInEu,
          euVatDetails,
          businessAddress,
          businessContactDetails,
          websites
        )
    }

}
