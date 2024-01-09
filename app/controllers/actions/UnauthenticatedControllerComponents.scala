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

package controllers.actions

import models.requests.{UnauthenticatedDataRequest, UnauthenticatedOptionalDataRequest}
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc._
import repositories.UnauthenticatedUserAnswersRepository
import services.FeatureFlagService

import javax.inject.Inject

trait UnauthenticatedControllerComponents extends MessagesControllerComponents {

  def actionBuilder: DefaultActionBuilder
  def sessionRepository: UnauthenticatedUserAnswersRepository
  def identify: SessionIdentifierAction
  def getData: UnauthenticatedDataRetrievalAction
  def requireData: UnauthenticatedDataRequiredAction
  def features: FeatureFlagService

  def identifyAndGetData: ActionBuilder[UnauthenticatedDataRequest, AnyContent] =
    actionBuilder andThen
      identify andThen
      getData andThen
      requireData

  def identifyAndGetOptionalData: ActionBuilder[UnauthenticatedOptionalDataRequest, AnyContent] =
    actionBuilder andThen
      identify andThen
      getData
}

case class DefaultUnauthenticatedControllerComponents @Inject()(
                                                                 messagesActionBuilder: MessagesActionBuilder,
                                                                 actionBuilder: DefaultActionBuilder,
                                                                 parsers: PlayBodyParsers,
                                                                 messagesApi: MessagesApi,
                                                                 langs: Langs,
                                                                 fileMimeTypes: FileMimeTypes,
                                                                 executionContext: scala.concurrent.ExecutionContext,
                                                                 sessionRepository: UnauthenticatedUserAnswersRepository,
                                                                 identify: SessionIdentifierAction,
                                                                 getData: UnauthenticatedDataRetrievalAction,
                                                                 requireData: UnauthenticatedDataRequiredAction,
                                                                 features: FeatureFlagService
                                                             ) extends UnauthenticatedControllerComponents
