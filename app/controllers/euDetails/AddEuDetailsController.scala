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

import controllers.actions._
import forms.euDetails.AddEuDetailsFormProvider
import models.euDetails.EuOptionalDetails
import models.requests.AuthenticatedDataRequest
import models.{CheckMode, Country, Mode}
import pages.euDetails.AddEuDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.DeriveNumberOfEuRegistrations
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CompletionChecks
import utils.EuDetailsCompletionChecks.{getAllIncompleteEuDetails, incompleteCheckEuDetailsRedirect}
import utils.FutureSyntax.FutureOps
import viewmodels.checkAnswers.euDetails.EuDetailsSummary
import views.html.euDetails.{AddEuDetailsView, PartOfVatGroupAddEuDetailsView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddEuDetailsController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        cc: AuthenticatedControllerComponents,
                                        formProvider: AddEuDetailsFormProvider,
                                        view: AddEuDetailsView,
                                        viewPartOfVatGroup: PartOfVatGroupAddEuDetailsView
)(implicit ec: ExecutionContext) extends FrontendBaseController with CompletionChecks with I18nSupport {

  protected val controllerComponents: MessagesControllerComponents = cc
  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      val vatOnly = request.userAnswers.vatInfo.exists(_.partOfVatGroup)
      getNumberOfEuCountries {
        number =>

          val canAddCountries = number < Country.euCountries.size

          withCompleteDataAsync[EuOptionalDetails](
            data = getAllIncompleteEuDetails,
            onFailure = (incomplete: Seq[EuOptionalDetails]) => {
              if(vatOnly){
                val list = EuDetailsSummary.countryAndVatNumberList(request.userAnswers, mode)
                Future.successful(Ok(viewPartOfVatGroup(form, mode, list, canAddCountries, incomplete)))
              } else {
                val list = EuDetailsSummary.addToListRows(request.userAnswers, mode)
                Future.successful(Ok(view(form, mode, list, canAddCountries, incomplete)))
              }
            }) {
            if(vatOnly) {
              val list = EuDetailsSummary.countryAndVatNumberList(request.userAnswers, mode)
              Future.successful(Ok(viewPartOfVatGroup(form, mode, list, canAddCountries)))
            } else {
              val list = EuDetailsSummary.addToListRows(request.userAnswers, mode)
              Future.successful(Ok(view(form, mode, list, canAddCountries)))
            }
          }
      }
  }

  def onSubmit(mode: Mode, incompletePromptShown: Boolean): Action[AnyContent] = cc.authAndGetData(Some(mode)).async {
    implicit request =>
      val vatOnly = request.userAnswers.vatInfo.exists(_.partOfVatGroup)
      withCompleteDataAsync[EuOptionalDetails](
        data = getAllIncompleteEuDetails,
        onFailure = (incomplete: Seq[EuOptionalDetails]) => {
          if (incompletePromptShown) {
            incompleteCheckEuDetailsRedirect(CheckMode).map(
              redirectIncompletePage => redirectIncompletePage.toFuture
            ).getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
          } else {
            Future.successful(Redirect(routes.AddEuDetailsController.onPageLoad(mode)))
          }
        }) {
        getNumberOfEuCountries {
          number =>
            val canAddCountries = number < Country.euCountries.size
            form.bindFromRequest().fold(
              formWithErrors => {
                if(vatOnly) {
                  val list = EuDetailsSummary.countryAndVatNumberList(request.userAnswers, mode)
                  Future.successful(BadRequest(viewPartOfVatGroup(formWithErrors, mode, list, canAddCountries)))
                } else {
                  val list = EuDetailsSummary.addToListRows(request.userAnswers, mode)
                  Future.successful(BadRequest(view(formWithErrors, mode, list, canAddCountries)))
                }

              },
              value =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(AddEuDetailsPage, value))
                  _              <- cc.sessionRepository.set(updatedAnswers)
                } yield Redirect(AddEuDetailsPage.navigate(mode, updatedAnswers))
            )
        }
      }

  }

  private def getNumberOfEuCountries(block: Int => Future[Result])
                                    (implicit request: AuthenticatedDataRequest[AnyContent]): Future[Result] =
    request.userAnswers.get(DeriveNumberOfEuRegistrations).map {
      number =>
        block(number)
    }.getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
}
