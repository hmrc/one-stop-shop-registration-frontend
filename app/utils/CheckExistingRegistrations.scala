/*
 * Copyright 2023 HM Revenue & Customs
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

import logging.Logging
import models.{Country, Index, PreviousScheme}
import models.domain.{PreviousRegistrationLegacy, PreviousRegistrationNew, PreviousSchemeDetails, Registration}
import models.previousRegistrations.SchemeDetailsWithOptionalVatNumber
import models.requests.AuthenticatedDataRequest
import play.api.mvc.AnyContent

object CheckExistingRegistrations extends Logging {

  def checkExistingRegistration()(implicit request: AuthenticatedDataRequest[AnyContent]): Registration = {
    val registration: Registration = checkRegistration(request)

    registration.previousRegistrations.map {
      case previousRegistrationNew: PreviousRegistrationNew =>
        previousRegistrationNew.country
      case previousRegistrationLegacy: PreviousRegistrationLegacy =>
        previousRegistrationLegacy.country
    }
    registration
  }

  def getExistingRegistrationSchemes(country: Country)(implicit request: AuthenticatedDataRequest[AnyContent]): Seq[PreviousScheme] = {
    val registration: Registration = checkRegistration(request)

    registration.previousRegistrations.find {
      case previousRegistrationNew: PreviousRegistrationNew => previousRegistrationNew.country == country
      case previousRegistrationLegacy: PreviousRegistrationLegacy => previousRegistrationLegacy.country == country
    } match {
      case Some(previousRegistrationNew: PreviousRegistrationNew) => previousRegistrationNew.previousSchemesDetails.map(_.previousScheme)
      case Some(_: PreviousRegistrationLegacy) => Seq(PreviousScheme.OSSU)
      case None => Seq.empty
    }
  }

  private def checkRegistration(request: AuthenticatedDataRequest[AnyContent]) = {
    val registration = request.registration match {
      case Some(registration) => registration
      case None =>
        val exception = new IllegalStateException("Can't amend a non-existent registration")
        logger.error(exception.getMessage, exception)
        throw exception
    }
    registration
  }
}
