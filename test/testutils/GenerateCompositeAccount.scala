/*
 * Copyright 2026 HM Revenue & Customs
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

package testutils

import models.etmp.intermediary.{EtmpTradingName, IntermediaryRegistrationWrapper}
import models.iossRegistration.IossEtmpDisplayRegistration
import models.{BankDetails, BusinessContactDetails, CompositeAccount}

object GenerateCompositeAccount {

  def generateCompositeAccount(
                                iossRegistration: Option[IossEtmpDisplayRegistration] = None,
                                intermediaryRegistration: Option[IntermediaryRegistrationWrapper] = None
                              ): Option[CompositeAccount] = {
    (iossRegistration, intermediaryRegistration) match {
      case (Some(iossReg), _) =>
        Some(createCompositeAccount(
          tradingNames = iossReg.tradingNames.map(tn => EtmpTradingName(tn.tradingName)),
          fullName = iossReg.schemeDetails.contactName,
          telephoneNumber = iossReg.schemeDetails.businessTelephoneNumber,
          emailAddress = iossReg.schemeDetails.businessEmailId,
          bankDetails = BankDetails(
            accountName = iossReg.bankDetails.accountName,
            bic = iossReg.bankDetails.bic,
            iban = iossReg.bankDetails.iban)
        ))

      case (_, Some(intReg)) =>
        Some(createCompositeAccount(
          tradingNames = intReg.etmpDisplayRegistration.tradingNames.map(tn => EtmpTradingName(tn.tradingName)),
          fullName = intReg.etmpDisplayRegistration.schemeDetails.contactName,
          telephoneNumber = intReg.etmpDisplayRegistration.schemeDetails.businessTelephoneNumber,
          emailAddress = intReg.etmpDisplayRegistration.schemeDetails.businessEmailId,
          bankDetails =
            BankDetails(
              accountName = intReg.etmpDisplayRegistration.bankDetails.accountName,
              bic = intReg.etmpDisplayRegistration.bankDetails.bic,
              iban = intReg.etmpDisplayRegistration.bankDetails.iban
            )
        ))

      case _ => None
    }
  }

  private def createCompositeAccount(
                                      tradingNames: Seq[EtmpTradingName],
                                      fullName: String,
                                      telephoneNumber: String,
                                      emailAddress: String,
                                      bankDetails: BankDetails
                                    ): CompositeAccount = {
    CompositeAccount(
      tradingNames = tradingNames,
      contactDetails = BusinessContactDetails(
        fullName = fullName,
        telephoneNumber = telephoneNumber,
        emailAddress = emailAddress
      ),
      bankDetails = bankDetails
    )
  }
}
