/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.actions._
import forms.euDetails.EuTaxReferenceFormProvider
import models.{Country, Index, Mode}
import models.requests.AuthenticatedDataRequest
import pages.euDetails.{EuCountryPage, EuTaxReferencePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.CoreRegistrationValidationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.euDetails.EuTaxReferenceView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EuTaxReferenceController @Inject()(
                                          override val messagesApi: MessagesApi,
                                          cc: AuthenticatedControllerComponents,
                                          formProvider: EuTaxReferenceFormProvider,
                                          coreRegistrationValidationService: CoreRegistrationValidationService,
                                          appConfig: FrontendAppConfig,
                                          view: EuTaxReferenceView
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getCountry(index) {
        country =>

          val form = formProvider(country)

          val preparedForm = request.userAnswers.get(EuTaxReferencePage(index)) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Future.successful(Ok(view(preparedForm, mode, index, country)))
      }
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getCountry(index) {
        country =>

          val form = formProvider(country)

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, index, country))),

            value =>
              if (appConfig.otherCountryRegistrationValidationEnabled) {
                coreRegistrationValidationService.searchEuTaxId(value, country.code).flatMap {

                  case Some(activeMatch) if coreRegistrationValidationService.isActiveTrader(activeMatch) =>
                    Future.successful(Redirect(controllers.routes.FixedEstablishmentVRNAlreadyRegisteredController.onPageLoad()))

                  case Some(activeMatch) if coreRegistrationValidationService.isQuarantinedTrader(activeMatch) =>
                    Future.successful(Redirect(controllers.routes.ExcludedVRNController.onPageLoad()))

                  case _ => for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(EuTaxReferencePage(index), value))
                    _ <- cc.sessionRepository.set(updatedAnswers)
                  } yield Redirect(EuTaxReferencePage(index).navigate(mode, updatedAnswers))
                }
              } else {
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(EuTaxReferencePage(index), value))
                  _ <- cc.sessionRepository.set(updatedAnswers)
                } yield Redirect(EuTaxReferencePage(index).navigate(mode, updatedAnswers))
              }
          )
      }
  }

  private def getCountry(index: Index)
                        (block: Country => Future[Result])
                        (implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(EuCountryPage(index)).map {
      country =>
        block(country)
    }.getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
}
