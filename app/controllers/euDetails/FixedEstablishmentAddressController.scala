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

package controllers.euDetails

import controllers.actions._
import controllers.routes
import forms.euDetails.FixedEstablishmentAddressFormProvider
import models.requests.DataRequest
import models.{Country, Index, Mode}
import navigation.Navigator
import pages.euDetails.FixedEstablishmentAddressPage
import pages.euDetails.{EuCountryPage, FixedEstablishmentAddressPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.euDetails.FixedEstablishmentAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FixedEstablishmentAddressController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      cc: AuthenticatedControllerComponents,
                                      navigator: Navigator,
                                      formProvider: FixedEstablishmentAddressFormProvider,
                                      view: FixedEstablishmentAddressView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()
  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getCountry(index) {
        country =>

          val preparedForm = request.userAnswers.get(FixedEstablishmentAddressPage(index)) match {
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

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, index, country))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(FixedEstablishmentAddressPage(index), value))
                _              <- cc.sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(FixedEstablishmentAddressPage(index), mode, updatedAnswers))
          )

      }
  }

  private def getCountry(index: Index)
                        (block: Country => Future[Result])
                        (implicit request: DataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(EuCountryPage(index)).map {
      country =>
        block(country)
    }.getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
}