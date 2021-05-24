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

package service

import connectors.RegistrationConnector
import models.{RegistrationResponse, UserAnswers}
import models.requests.{DataRequest, RegistrationRequest}
import pages.{BusinessAddressPage, BusinessContactDetailsPage, HasTradingNamePage, PartOfVatGroupPage, RegisteredCompanyNamePage, UkVatEffectiveDatePage, UkVatNumberPage, UkVatRegisteredPostcodePage, VatRegisteredInEuPage}
import play.api.mvc.Results.Redirect
import queries.{AllEuVatDetailsQuery, AllTradingNames, AllWebsites}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import play.api.Logger
import play.api.i18n.Lang.logger
import uk.gov.hmrc.govukfrontend.controllers.routes

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.Future.successful

class RegistrationService @Inject()(
   registrationConnector: RegistrationConnector
){
//  def submit(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Option[HttpResponse]] = {
//    val request =  RegistrationRequest(
//      userAnswers.get(RegisteredCompanyNamePage).getOrElse(""),
//      userAnswers.get(HasTradingNamePage).getOrElse(""),
//      userAnswers.get(AllTradingNames).getOrElse(""),
//      userAnswers.get(PartOfVatGroupPage).getOrElse(""),
//      userAnswers.get(UkVatNumberPage).getOrElse(""),
//      userAnswers.get(UkVatEffectiveDatePage).getOrElse(""),
//      userAnswers.get(UkVatRegisteredPostcodePage).getOrElse(""),
//      userAnswers.get(VatRegisteredInEuPage).getOrElse(""),
//      userAnswers.get(AllEuVatDetailsQuery).getOrElse(Seq.empty),
//      userAnswers.get(BusinessAddressPage).getOrElse(""),
//      userAnswers.get(BusinessContactDetailsPage).getOrElse(""),
//      userAnswers.get(AllWebsites).getOrElse(Seq.empty)
//    )
//
//    request match => {
//      case Some(request) => registrationConnector.submitRegistration(request)
//      case _ => ""
//    }

  // Build payload for backend

  def submit(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[RegistrationResponse] = {
    val registrationRequest = RegistrationRequest.buildRegistrationRequest(userAnswers)

    registrationRequest match {
      case Some(registrationSubmission) =>
        registrationConnector.submitRegistration(registrationSubmission).flatMap {
          case result =>
            successful(Redirect(routes.ApplicationCompleteController.onPageLoad()))
          case _ =>
            ???
        }
      case None =>
        logger.error("Unable to create a OSS registration request from user answers")
        Redirect(routes.JourneyRecoveryController.onPageLoad())
    }
  }






}
