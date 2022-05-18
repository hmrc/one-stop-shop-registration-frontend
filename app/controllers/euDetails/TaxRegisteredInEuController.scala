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
import forms.euDetails.TaxRegisteredInEuFormProvider
import models.Mode
import pages.PartOfVatGroupPage
import pages.euDetails.TaxRegisteredInEuPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.euDetails.{TaxRegisteredInEuView, VatRegisteredInEuView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxRegisteredInEuController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         cc: AuthenticatedControllerComponents,
                                         formProvider: TaxRegisteredInEuFormProvider,
                                         view: TaxRegisteredInEuView,
                                         vatOnlyView: VatRegisteredInEuView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData() {
    implicit request =>
      val vatOnly = request.userAnswers.vatInfo.flatMap(_.partOfVatGroup).getOrElse(request.userAnswers.get(PartOfVatGroupPage).contains(true))
      val form = formProvider(vatOnly)
      val preparedForm = request.userAnswers.get(TaxRegisteredInEuPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      if(vatOnly){
        Ok(vatOnlyView(preparedForm, mode))
      } else {
        Ok(view(preparedForm, mode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>
      val vatOnly = request.userAnswers.vatInfo.flatMap(_.partOfVatGroup).getOrElse(request.userAnswers.get(PartOfVatGroupPage).contains(true))
      val form = formProvider(vatOnly)
      form.bindFromRequest().fold(
        formWithErrors =>
          if(vatOnly){
            Future.successful(BadRequest(vatOnlyView(formWithErrors, mode)))
          } else {
            Future.successful(BadRequest(view(formWithErrors, mode)))
          },

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(TaxRegisteredInEuPage, value))
            _              <- cc.sessionRepository.set(updatedAnswers)
          } yield Redirect(TaxRegisteredInEuPage.navigate(mode, updatedAnswers))
      )
  }
}
