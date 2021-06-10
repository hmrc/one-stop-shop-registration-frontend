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

package models.emails

sealed trait Email[T] {
  val to: Seq[String]
  val templateId: String
  val parameters: T // Must render to JSON as a Map[String, String]
  val force: Boolean            = false
  val eventUrl: Option[String]  = None
  val onSendUrl: Option[String] = None
}

case class RegistrationConfirmationEmail(
  override val to: Seq[String],
  override val parameters: RegistrationConfirmationEmailParameters
) extends Email[RegistrationConfirmationEmailParameters] {
  override val templateId: String = EmailType.REGISTRATION_CONFIRMATION.toString
}

case class RegistrationConfirmationEmailParameters(reference: String)

object EmailType extends Enumeration {
  type EmailType = Value
  val REGISTRATION_CONFIRMATION = Value("oss_registration_confirmation")
}