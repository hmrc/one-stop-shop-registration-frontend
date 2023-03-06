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

package controllers.euDetails

import config.FrontendAppConfig
import controllers.GetCountry
import controllers.actions._
import forms.euDetails.EuVatNumberFormProvider
import models.{CountryWithValidationDetails, Index, Mode}
import pages.euDetails.EuVatNumberPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CoreRegistrationValidationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.euDetails.EuVatNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EuVatNumberController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       cc: AuthenticatedControllerComponents,
                                       formProvider: EuVatNumberFormProvider,
                                       coreRegistrationValidationService: CoreRegistrationValidationService,
                                       appConfig: FrontendAppConfig,
                                       view: EuVatNumberView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with GetCountry {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getCountry(index) {
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

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getCountry(index) {
        country =>

          val form = formProvider(country)

          form.bindFromRequest().fold(
            formWithErrors =>
              CountryWithValidationDetails.euCountriesWithVRNValidationRules.filter(_.country.code == country.code).head match {
                case countryWithValidationDetails =>
                  Future.successful(BadRequest(view(formWithErrors, mode, index, countryWithValidationDetails)))
              },

            value =>

              if (appConfig.otherCountryRegistrationValidationEnabled) {
                coreRegistrationValidationService.searchEuVrn(value, country.code).flatMap {

                  case Some(activeMatch) if coreRegistrationValidationService.isActiveTrader(activeMatch) =>
                    Future.successful(Redirect(controllers.routes.FixedEstablishmentVRNAlreadyRegisteredController.onPageLoad()))

                  case Some(activeMatch) if coreRegistrationValidationService.isQuarantinedTrader(activeMatch) =>
                    Future.successful(Redirect(controllers.routes.ExcludedVRNController.onPageLoad()))

                  case _ => for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(EuVatNumberPage(index), value))
                    _ <- cc.sessionRepository.set(updatedAnswers)
                  } yield Redirect(EuVatNumberPage(index).navigate(mode, updatedAnswers))
                }
              } else {
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(EuVatNumberPage(index), value))
                  _ <- cc.sessionRepository.set(updatedAnswers)
                } yield Redirect(EuVatNumberPage(index).navigate(mode, updatedAnswers))
              }
          )
      }
  }

}

