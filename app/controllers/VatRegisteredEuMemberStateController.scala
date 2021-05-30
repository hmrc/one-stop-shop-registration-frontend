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

package controllers

import controllers.actions._
import forms.VatRegisteredEuMemberStateFormProvider

import javax.inject.Inject
import models.{Index, Mode}
import navigation.Navigator
import pages.VatRegisteredEuMemberStatePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{AllEuVatDetailsQuery, AllWebsites}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.VatRegisteredEuMemberStateView

import scala.concurrent.{ExecutionContext, Future}

class VatRegisteredEuMemberStateController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        cc: AuthenticatedControllerComponents,
                                        navigator: Navigator,
                                        formProvider: VatRegisteredEuMemberStateFormProvider,
                                        view: VatRegisteredEuMemberStateView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData() {
    implicit request =>

      val form = formProvider(index, request.userAnswers.get(AllEuVatDetailsQuery).getOrElse(Seq.empty).map(_.vatRegisteredEuMemberState))

      val preparedForm = request.userAnswers.get(VatRegisteredEuMemberStatePage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, index))
  }

  def onSubmit(mode: Mode, index: Index): Action[AnyContent] = cc.authAndGetData().async {
    implicit request =>

      val form = formProvider(index, request.userAnswers.get(AllEuVatDetailsQuery).getOrElse(Seq.empty).map(_.vatRegisteredEuMemberState))

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, index))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(VatRegisteredEuMemberStatePage(index), value))
            _              <- cc.sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(VatRegisteredEuMemberStatePage(index), mode, updatedAnswers))
      )
  }
}
