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

package controllers

import controllers.actions._
import forms.HasTradingNameFormProvider
import logging.Logging
import models.Mode
import models.requests.AuthenticatedDataRequest
import pages.HasTradingNamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckJourneyRecovery.determineJourneyRecovery
import utils.FutureSyntax.FutureOps
import views.html.HasTradingNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HasTradingNameController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         cc: AuthenticatedControllerComponents,
                                         formProvider: HasTradingNameFormProvider,
                                         view: HasTradingNameView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      getCompanyName(mode) {
        companyName =>

          val form = formProvider()

          val preparedForm = request.userAnswers.get(HasTradingNamePage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Future.successful(Ok(view(preparedForm, mode, companyName)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      getCompanyName(mode) {
        companyName =>

          val form = formProvider()

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, companyName))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(HasTradingNamePage, value))
                _              <- cc.sessionRepository.set(updatedAnswers)
              } yield Redirect(HasTradingNamePage.navigate(mode, updatedAnswers))
        )
      }
  }

  private def getCompanyName(mode: Mode)(block: String => Future[Result])
                            (implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] = {
    request.userAnswers.vatInfo match {
      case Some(vatInfo) if(vatInfo.organisationName.isDefined) =>
        val name = vatInfo.organisationName.getOrElse{
          val exception = new IllegalStateException("No organisation name when expecting one")
          logger.error(exception.getMessage, exception)
          throw exception
        }
        block(name)
      case Some(vatInfo) if(vatInfo.individualName.isDefined) =>
        val name = vatInfo.individualName.getOrElse {
          val exception = new IllegalStateException("No individual name when expecting one")
          logger.error(exception.getMessage, exception)
          throw exception
        }
        block(name)
      case _ => Redirect(determineJourneyRecovery(Some(mode))).toFuture
    }
  }
}
