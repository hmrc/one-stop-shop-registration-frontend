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

package controllers.euDetails

import config.FrontendAppConfig
import controllers.GetCountry
import controllers.actions.*
import forms.euDetails.EuVatNumberFormProvider
import models.{CountryWithValidationDetails, Index, Mode, RejoinMode}
import pages.euDetails.{EuVatNumberPage, SellsGoodsToEUConsumersPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{CoreRegistrationValidationService, RejoinRedirectService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FutureSyntax.FutureOps
import views.html.euDetails.EuVatNumberView

import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EuVatNumberController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       cc: AuthenticatedControllerComponents,
                                       formProvider: EuVatNumberFormProvider,
                                       coreRegistrationValidationService: CoreRegistrationValidationService,
                                       appConfig: FrontendAppConfig,
                                       clock: Clock,
                                       view: EuVatNumberView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with GetCountry {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      getCountry(mode, index) {
        country =>

          val form = formProvider(country)

          val preparedForm = request.userAnswers.get(EuVatNumberPage(index)) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          CountryWithValidationDetails.euCountriesWithVRNValidationRules.filter(_.country.code == country.code).head match {
            case countryWithValidationDetails =>
              Future.successful(Ok(view(preparedForm, mode, index, countryWithValidationDetails)))
          }

      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      getCountry(mode, index) {
        country =>

          val form = formProvider(country)

          form.bindFromRequest().fold(
            formWithErrors =>
              CountryWithValidationDetails.euCountriesWithVRNValidationRules.filter(_.country.code == country.code).head match {
                case countryWithValidationDetails =>
                  Future.successful(BadRequest(view(formWithErrors, mode, index, countryWithValidationDetails)))
              },

            value => {
              lazy val successResult: Future[Result] = for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(EuVatNumberPage(index), value))
                _ <- cc.sessionRepository.set(updatedAnswers)
              } yield Redirect(EuVatNumberPage(index).navigate(mode, updatedAnswers))

              if (appConfig.otherCountryRegistrationValidationEnabled) {
                val isOtherMS = !request.userAnswers.get(SellsGoodsToEUConsumersPage(index)).getOrElse(false)

                coreRegistrationValidationService.searchEuVrn(value, country.code, isOtherMS).flatMap { maybeMatch =>
                  if (mode == RejoinMode) {
                    RejoinRedirectService.redirectOnMatch(maybeMatch, clock).map(_.toFuture).getOrElse(successResult)
                  } else {
                    maybeMatch match {
                      case Some(activeMatch) if activeMatch.isActiveTrader =>
                        Future.successful(Redirect(controllers.routes.FixedEstablishmentVRNAlreadyRegisteredController.onPageLoad(mode, index)))

                      case Some(activeMatch) if activeMatch.isQuarantinedTrader(clock) =>
                        Future.successful(Redirect(controllers.routes.ExcludedVRNController.onPageLoad()))

                      case _ => successResult
                    }
                  }
                }
              } else {
                successResult
              }
            }
          )
      }
  }

}

