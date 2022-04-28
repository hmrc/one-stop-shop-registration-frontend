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

import controllers.actions._
import forms.euDetails.EuSendGoodsAddressFormProvider
import models.requests.AuthenticatedDataRequest
import models.{Country, Index, Mode}
import pages.euDetails.{EuCountryPage, EuSendGoodsAddressPage, EuSendGoodsTradingNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.euDetails.EuSendGoodsAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EuSendGoodsAddressController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        cc: AuthenticatedControllerComponents,
                                        formProvider: EuSendGoodsAddressFormProvider,
                                        view: EuSendGoodsAddressView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getCountry(index) {
        country =>
        getBusinessName(index) {
          businessName =>
            val form = formProvider(country)

            val preparedForm = request.userAnswers.get(EuSendGoodsAddressPage(index)) match {
              case None => form
              case Some(value) => form.fill(value)
            }
            Future.successful(Ok(view(preparedForm, mode, index, businessName, country)))
        }
      }

  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      getCountry(index) {
        country =>
          getBusinessName(index) {
            businessName =>
              val form = formProvider(country)

              form.bindFromRequest().fold(
                formWithErrors =>
                  Future.successful(BadRequest(view(formWithErrors, mode, index, businessName, country))),

                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(EuSendGoodsAddressPage(index), value))
                    _ <- cc.sessionRepository.set(updatedAnswers)
                  } yield Redirect(EuSendGoodsAddressPage(index).navigate(mode, updatedAnswers))
              )
          }
      }
  }

  private def getBusinessName(index: Index)
                        (block: String => Future[Result])
                        (implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] = {
    request.userAnswers.get(EuSendGoodsTradingNamePage(index)).map {
      businessName => block(businessName)
    }.getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }

  private def getCountry(index: Index)
                        (block: Country => Future[Result])
                        (implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(EuCountryPage(index)).map {
      country => block(country)
    }.getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
}
