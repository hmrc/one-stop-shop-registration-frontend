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

import models.requests.{AuthenticatedDataRequest, AuthenticatedOptionalDataRequest}
import models.Mode
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{ActionBuilder, AnyContent, DefaultActionBuilder, MessagesActionBuilder, MessagesControllerComponents, PlayBodyParsers}
import repositories.AuthenticatedUserAnswersRepository
import services.FeatureFlagService

import javax.inject.Inject

trait AuthenticatedControllerComponents extends MessagesControllerComponents {

  def actionBuilder: DefaultActionBuilder
  def sessionRepository: AuthenticatedUserAnswersRepository
  def identify: AuthenticatedIdentifierAction
  def getData: AuthenticatedDataRetrievalAction
  def requireData: AuthenticatedDataRequiredAction
  def checkRegistration: CheckRegistrationFilterProvider
  def checkVrnAllowList: VrnAllowListFilter
  def limitIndex: MaximumIndexFilterProvider
  def features: FeatureFlagService
  def checkNiProtocol: CheckNiProtocolFilter
  def checkNiProtocolExpired: CheckNiProtocolExpiredFilter
  def checkNiProtocolExpiredOptional: CheckNiProtocolExpiredOptionalFilter
  def retrieveSavedAnswers: SavedAnswersRetrievalActionProvider
  def checkOtherCountryRegistration: CheckOtherCountryRegistrationFilter
  def checkRejoinOtherCountryRegistration: CheckRejoinOtherCountryRegistrationFilter
  def checkEmailVerificationStatus: CheckEmailVerificationFilterProvider
  def checkEligibleSalesAmendable: CheckEligibleSalesAmendableFilterProvider
  def checkVatExpiredFilter: CheckVatExpiredFilter
  def authAndGetData(mode: Option[Mode] = None): ActionBuilder[AuthenticatedDataRequest, AnyContent] =
    actionBuilder andThen
      identify andThen
      checkVrnAllowList andThen
      checkRegistration(mode) andThen
      getData andThen
      requireData(mode) andThen
      checkVatExpiredFilter(mode) andThen
      checkNiProtocolExpired(mode) andThen
      checkNiProtocol(mode) andThen
      checkOtherCountryRegistration(mode)

  def authAndGetOptionalData(mode: Option[Mode] = None): ActionBuilder[AuthenticatedOptionalDataRequest, AnyContent] =
    actionBuilder andThen
      identify andThen
      checkVrnAllowList andThen
      checkRegistration(mode) andThen
      getData andThen
      checkNiProtocolExpiredOptional(mode) andThen
      checkRejoinOtherCountryRegistration(mode)

  def authAndGetDataAndCheckVerifyEmail(mode: Option[Mode] = None): ActionBuilder[AuthenticatedDataRequest, AnyContent] =
    authAndGetData(mode) andThen
      checkEmailVerificationStatus(mode)

}

case class DefaultAuthenticatedControllerComponents @Inject()(
                                                               messagesActionBuilder: MessagesActionBuilder,
                                                               actionBuilder: DefaultActionBuilder,
                                                               parsers: PlayBodyParsers,
                                                               messagesApi: MessagesApi,
                                                               langs: Langs,
                                                               fileMimeTypes: FileMimeTypes,
                                                               executionContext: scala.concurrent.ExecutionContext,
                                                               sessionRepository: AuthenticatedUserAnswersRepository,
                                                               identify: AuthenticatedIdentifierAction,
                                                               checkVrnAllowList: VrnAllowListFilter,
                                                               checkRegistration: CheckRegistrationFilterProvider,
                                                               getData: AuthenticatedDataRetrievalAction,
                                                               requireData: AuthenticatedDataRequiredAction,
                                                               limitIndex: MaximumIndexFilterProvider,
                                                               features: FeatureFlagService,
                                                               checkNiProtocol: CheckNiProtocolFilter,
                                                               checkNiProtocolExpired: CheckNiProtocolExpiredFilter,
                                                               checkNiProtocolExpiredOptional: CheckNiProtocolExpiredOptionalFilter,
                                                               retrieveSavedAnswers: SavedAnswersRetrievalActionProvider,
                                                               checkOtherCountryRegistration: CheckOtherCountryRegistrationFilter,
                                                               checkRejoinOtherCountryRegistration: CheckRejoinOtherCountryRegistrationFilter,
                                                               checkEmailVerificationStatus: CheckEmailVerificationFilterProvider,
                                                               checkEligibleSalesAmendable: CheckEligibleSalesAmendableFilterProvider,
                                                               checkVatExpiredFilter: CheckVatExpiredFilter,
                                                             ) extends AuthenticatedControllerComponents
