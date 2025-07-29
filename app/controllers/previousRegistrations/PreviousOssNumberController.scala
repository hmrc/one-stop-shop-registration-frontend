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

package controllers.previousRegistrations

import config.FrontendAppConfig
import controllers.GetCountry
import controllers.actions.*
import forms.previousRegistrations.PreviousOssNumberFormProvider
import models.core.Match
import models.domain.PreviousSchemeNumbers
import models.previousRegistrations.{NonCompliantDetails, PreviousSchemeHintText}
import models.requests.AuthenticatedDataRequest
import models.{Country, CountryWithValidationDetails, Index, Mode, PreviousScheme, RejoinMode, WithName}
import pages.previousRegistrations.{PreviousOssNumberPage, PreviousSchemePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.previousRegistration.{AllPreviousSchemesForCountryWithOptionalVatNumberQuery, NonCompliantDetailsQuery}
import services.{CoreRegistrationValidationService, RejoinRedirectService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps
import views.html.previousRegistrations.PreviousOssNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PreviousOssNumberController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             cc: AuthenticatedControllerComponents,
                                             coreRegistrationValidationService: CoreRegistrationValidationService,
                                             formProvider: PreviousOssNumberFormProvider,
                                             appConfig: FrontendAppConfig,
                                             view: PreviousOssNumberView
                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with GetCountry {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, countryIndex: Index, schemeIndex: Index): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      getPreviousCountry(mode, countryIndex) {
        country =>

          val previousSchemeHintText = determinePreviousSchemeHintText(countryIndex)

          val form = request.userAnswers.get(AllPreviousSchemesForCountryWithOptionalVatNumberQuery(countryIndex)) match {
            case Some(previousSchemeDetails) =>

              val previousSchemes = previousSchemeDetails.flatMap(_.previousScheme)
              formProvider(country, previousSchemes)

            case None =>
              formProvider(country, Seq.empty)
          }

          val preparedForm = request.userAnswers.get(PreviousOssNumberPage(countryIndex, schemeIndex)) match {
            case None => form
            case Some(value) => form.fill(value.previousSchemeNumber)
          }
          CountryWithValidationDetails.euCountriesWithVRNValidationRules.filter(_.country.code == country.code).head match {
            case countryWithValidationDetails =>
              Future.successful(Ok(view(preparedForm, mode, countryIndex, schemeIndex, countryWithValidationDetails, previousSchemeHintText)))
          }
      }
  }

  def onSubmit(mode: Mode, countryIndex: Index, schemeIndex: Index): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      getPreviousCountry(mode, countryIndex) {
        country =>

          val previousSchemeHintText = determinePreviousSchemeHintText(countryIndex)

          val form = request.userAnswers.get(AllPreviousSchemesForCountryWithOptionalVatNumberQuery(countryIndex)) match {
            case Some(previousSchemeDetails) =>

              val previousSchemes = previousSchemeDetails.flatMap(_.previousScheme)
              formProvider(country, previousSchemes)

            case None =>
              formProvider(country, Seq.empty)
          }

          form.bindFromRequest().fold(
            formWithErrors =>
              CountryWithValidationDetails.euCountriesWithVRNValidationRules.filter(_.country.code == country.code).head match {
                case countryWithValidationDetails =>
                  Future.successful(BadRequest(view(formWithErrors, mode, countryIndex, schemeIndex, countryWithValidationDetails, previousSchemeHintText)))
              },

            value => {
              val previousScheme = if (value.startsWith("EU")) {
                PreviousScheme.OSSNU
              } else {
                PreviousScheme.OSSU
              }
              searchSchemeThenSaveAndRedirect(mode, countryIndex, schemeIndex, country, value, previousScheme)
            }
          )
      }
  }

  private def searchSchemeThenSaveAndRedirect(
                                               mode: Mode,
                                               countryIndex: Index,
                                               schemeIndex: Index,
                                               country: Country,
                                               value: String,
                                               previousScheme: WithName with PreviousScheme
                                             )(implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] = {
    def defaultResult(activeMatch: Option[Match] = None): Future[Result] = saveAndRedirect(countryIndex, schemeIndex, value, previousScheme, mode, activeMatch)

    if (appConfig.otherCountryRegistrationValidationEnabled && previousScheme == PreviousScheme.OSSU) {
      coreRegistrationValidationService.searchScheme(
        searchNumber = value,
        previousScheme = previousScheme,
        intermediaryNumber = None,
        countryCode = country.code
      ).flatMap { maybeMatch =>
        if (mode == RejoinMode) {
          RejoinRedirectService.redirectOnMatch(maybeMatch).map(_.toFuture).getOrElse(defaultResult())
        } else {
          maybeMatch match {
            case Some(activeMatch) if activeMatch.matchType.isActiveTrader =>
              Future.successful(Redirect(
                controllers.previousRegistrations.routes.SchemeStillActiveController.onPageLoad(mode, activeMatch.memberState, countryIndex, schemeIndex)))
            case Some(activeMatch) if activeMatch.matchType.isQuarantinedTrader =>
              Future.successful(Redirect(controllers.previousRegistrations.routes.SchemeQuarantinedController.onPageLoad(mode, countryIndex, schemeIndex)))
            case Some(activeMatch) =>
              defaultResult(Some(activeMatch))
            case _ =>
              defaultResult()
          }
        }
      }
    } else {
      defaultResult()
    }
  }

  private def saveAndRedirect(countryIndex: Index, schemeIndex: Index, registrationNumber: String, previousScheme: PreviousScheme, mode: Mode, maybeActiveMatch: Option[Match])
                             (implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] = {
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(
        PreviousOssNumberPage(countryIndex, schemeIndex),
        PreviousSchemeNumbers(registrationNumber, None)
      ))
      updatedAnswersWithScheme <- Future.fromTry(updatedAnswers.set(
        PreviousSchemePage(countryIndex, schemeIndex),
        previousScheme
      ))
      updatedAnswersWithNonCompliantDetails <- maybeActiveMatch match {
        case Some(activeMatch) =>
          Future.fromTry(updatedAnswersWithScheme.set(
            NonCompliantDetailsQuery(countryIndex, schemeIndex),
            NonCompliantDetails(activeMatch.nonCompliantReturns, activeMatch.nonCompliantPayments))
          )
        case _ =>
          updatedAnswersWithScheme.toFuture
      }
      _ <- cc.sessionRepository.set(updatedAnswersWithNonCompliantDetails)
    } yield Redirect(PreviousOssNumberPage(countryIndex, schemeIndex).navigate(mode, updatedAnswersWithNonCompliantDetails))
  }

  private def determinePreviousSchemeHintText(countryIndex: Index)(implicit request: AuthenticatedDataRequest[AnyContent]): PreviousSchemeHintText = {
    request.userAnswers.get(AllPreviousSchemesForCountryWithOptionalVatNumberQuery(countryIndex)) match {
      case Some(listSchemeDetails) =>
        val previousSchemes = listSchemeDetails.flatMap(_.previousScheme)
        if (previousSchemes.contains(PreviousScheme.OSSU)) {
          PreviousSchemeHintText.OssNonUnion
        } else if (previousSchemes.contains(PreviousScheme.OSSNU)) {
          PreviousSchemeHintText.OssUnion
        } else {
          PreviousSchemeHintText.Both
        }
      case _ => PreviousSchemeHintText.Both
    }
  }
}
