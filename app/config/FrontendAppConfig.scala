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

package config

import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl

import java.net.URI
import java.time.Clock
import javax.inject.{Inject, Singleton}

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, clock: Clock) {

  val host: String    = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")
  val origin: String  = configuration.get[String]("origin")

  private val contactHost = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "one-stop-shop-registration-frontend"

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${SafeRedirectUrl(host + request.uri).encodedUrl}"

  val loginUrl: String         = configuration.get[String]("urls.login")
  val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  val registerUrl: String      = configuration.get[String]("urls.register")
  val signOutUrl: String       = configuration.get[String]("urls.signOut")
  val mfaUpliftUrl: String     = configuration.get[String]("urls.mfaUplift")
  val ivUpliftUrl: String      = configuration.get[String]("urls.ivUplift")
  val emailVerificationUrl: String      = configuration.get[String]("urls.emailVerificationUrl")
  val ossCompleteReturnUrl: String = configuration.get[String]("urls.ossCompleteReturnGuidanceUrl")

  val ivEvidenceStatusUrl: String =
    s"${configuration.get[Service]("microservice.services.identity-verification").baseUrl}/disabled-evidences?origin=$origin"

  val ivJourneyServiceUrl: String =
    s"${configuration.get[Service]("microservice.services.identity-verification").baseUrl}/journey/"

  def ivJourneyResultUrl(journeyId: String): String = new URI(s"$ivJourneyServiceUrl$journeyId").toString

  private val exitSurveyBaseUrl: String = configuration.get[String]("feedback-frontend.host") + configuration.get[String]("feedback-frontend.url")
  lazy val exitSurveyUrl: String        = s"$exitSurveyBaseUrl/${origin.toLowerCase}"

  val userResearchUrl : String = configuration.get[String]("urls.userResearchUrl")

  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  val registrationValidationEnabled: Boolean = configuration.get[Boolean]("features.reg-validation-enabled")

  val languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val cacheTtl: Int = configuration.get[Int]("mongodb.timeToLiveInSeconds")
  val enrolmentsEnabled: Boolean = configuration.get[Boolean]("features.enrolments-enabled")
  val ossEnrolment: String       = configuration.get[String]("oss-enrolment")

  val saveForLaterTtl: Int = configuration.get[Int]("mongodb.saveForLaterTTLInDays")

  val otherCountryRegistrationValidationEnabled: Boolean = configuration.get[Boolean]("features.other-country-reg-validation-enabled")

  val accessibilityStatementUrl: String = configuration.get[String]("accessibility-statement.service-path")

  val coreValidationUrl: Service = configuration.get[Service]("microservice.services.core-validation")

  val emailVerificationEnabled: Boolean = configuration.get[Boolean]("features.email-verification-enabled")

  val registrationEmailEnabled: Boolean = configuration.get[Boolean]("features.registration.email-enabled")

}
