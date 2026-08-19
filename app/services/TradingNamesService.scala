/*
 * Copyright 2025 HM Revenue & Customs
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

import models.CheckVatDetails.Yes
import models.{CheckVatDetails, CompositeAccount, UserAnswers}
import pages.HasTradingNamePage
import queries.AllTradingNames

import javax.inject.Inject
import scala.util.{Success, Try}

class TradingNamesService @Inject()() {

  def updateTradingNameAnswers(
                                checkVatDetails: CheckVatDetails,
                                userAnswers: UserAnswers,
                                compositeAccount: Option[CompositeAccount]
                              ): Try[UserAnswers] = {
    
    if (checkVatDetails == Yes) {
      compositeAccount
        .map(_.tradingNames.map(_.tradingName).toList)
        .filter(_.nonEmpty) match {
        
        case Some(names) =>
          for {
            answers <- userAnswers.set(HasTradingNamePage, true)
            updatedAnswers <- answers.set(AllTradingNames, names)
          } yield updatedAnswers

        case None =>
          Success(userAnswers)
      }
    } else {
      Success(userAnswers)
    }
  }
}
