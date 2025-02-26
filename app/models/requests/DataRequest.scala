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

package models.requests

import models.UserAnswers
import models.domain.Registration
import play.api.mvc.{Request, WrappedRequest}
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.domain.Vrn

trait AuthenticatedVrnRequest[+A] extends Request[A] {
  def request: Request[A]

  def credentials: Credentials

  def vrn: Vrn
}

case class AuthenticatedOptionalDataRequest[A](
                                                request: Request[A],
                                                credentials: Credentials,
                                                vrn: Vrn,
                                                registration: Option[Registration],
                                                userAnswers: Option[UserAnswers]
                                              ) extends WrappedRequest[A](request) with AuthenticatedVrnRequest[A] {

  val userId: String = credentials.providerId
}

case class UnauthenticatedOptionalDataRequest[A](
                                                  request: Request[A],
                                                  userId: String,
                                                  userAnswers: Option[UserAnswers]
                                                ) extends WrappedRequest[A](request)

case class AuthenticatedDataRequest[A](
                                        request: Request[A],
                                        credentials: Credentials,
                                        vrn: Vrn,
                                        registration: Option[Registration],
                                        userAnswers: UserAnswers
                                      ) extends WrappedRequest[A](request) with AuthenticatedVrnRequest[A] {

  val userId: String = credentials.providerId
}

case class UnauthenticatedDataRequest[A](
                                          request: Request[A],
                                          userId: String,
                                          userAnswers: UserAnswers
                                        ) extends WrappedRequest[A](request)
