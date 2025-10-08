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

import config.FrontendAppConfig
import connectors.RegistrationConnector
import controllers.actions.*
import formats.Format.dateFormatter
import logging.Logging
import models.UserAnswers
import models.iossRegistration.IossEtmpDisplayRegistration
import models.requests.AuthenticatedDataRequest
import pages.{BankDetailsPage, BusinessContactDetailsPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import queries.AllTradingNames
import services.{DateService, PeriodService}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.{BankDetailsSummary, BusinessContactDetailsSummary, HasTradingNameSummary, TradingNameSummary}
import viewmodels.govuk.all.SummaryListViewModel
import views.html.ApplicationCompleteView

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class ApplicationCompleteController @Inject()(
                                               override val messagesApi: MessagesApi,
                                               cc: AuthenticatedControllerComponents,
                                               view: ApplicationCompleteView,
                                               frontendAppConfig: FrontendAppConfig,
                                               dateService: DateService,
                                               periodService: PeriodService,
                                               registrationConnector: RegistrationConnector
                                             )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  protected val controllerComponents: MessagesControllerComponents = cc

  def onPageLoad(): Action[AnyContent] = (cc.actionBuilder andThen cc.identify andThen cc.getData andThen cc.requireData(None)).async {
    implicit request => {

      val userResearchUrl = frontendAppConfig.userResearchUrl1

      for {
        externalEntryUrl <- registrationConnector.getSavedExternalEntry()
        maybeCalculatedCommencementDate <- dateService.calculateCommencementDate(request.userAnswers)
      } yield {
        val calculatedCommencementDate = maybeCalculatedCommencementDate.getOrElse {
          val exception = new IllegalStateException("A calculated commencement date is expected, otherwise it wasn't submitted")
          logger.error(exception.getMessage, exception)
          throw exception
        }
        (for {
          organisationName <- getOrganisationName(request.userAnswers)
          contactDetails <- request.userAnswers.get(BusinessContactDetailsPage)
        } yield {
          val savedUrl = externalEntryUrl.fold(_ => None, _.url)
          val periodOfFirstReturn = periodService.getFirstReturnPeriod(calculatedCommencementDate)
          val nextPeriod = periodService.getNextPeriod(periodOfFirstReturn)
          val firstDayOfNextPeriod = nextPeriod.firstDay
          Ok(
            view(
              HtmlFormat.escape(contactDetails.emailAddress).toString,
              request.vrn,
              frontendAppConfig.feedbackUrl,
              calculatedCommencementDate.format(dateFormatter),
              savedUrl,
              organisationName,
              periodOfFirstReturn.displayShortText,
              firstDayOfNextPeriod.format(dateFormatter),
              request.latestIossRegistration,
              detailList(request.latestIossRegistration, request.userAnswers).rows.nonEmpty,
              request.numberOfIossRegistrations,
              userResearchUrl
            )
          )
        }).getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
    }
  }

  private def getOrganisationName(answers: UserAnswers): Option[String] = {
    answers.vatInfo match {
      case Some(vatInfo) if (vatInfo.organisationName.isDefined) => vatInfo.organisationName
      case Some(vatInfo) if (vatInfo.individualName.isDefined) => vatInfo.individualName
      case _ => None
    }
  }

  private def detailList(originalRegistration: Option[IossEtmpDisplayRegistration], userAnswers: UserAnswers)
                        (implicit request: AuthenticatedDataRequest[AnyContent]): SummaryList = {

    originalRegistration match {
      case Some(iossEtmpDisplayRegistration) =>

        SummaryListViewModel(
          rows = (
            getHasTradingNameRows(iossEtmpDisplayRegistration, userAnswers) ++
              getTradingNameRows(iossEtmpDisplayRegistration, userAnswers) ++
              getBusinessContactDetailsRows(iossEtmpDisplayRegistration, userAnswers) ++
              getBankDetailsRows(iossEtmpDisplayRegistration, userAnswers)
            ).flatten
        )
      case _ =>
        SummaryListViewModel(
          rows = Seq.empty
        )
    }
  }

  private def getHasTradingNameRows(
                                     originalRegistration: IossEtmpDisplayRegistration,
                                     userAnswers: UserAnswers
                                   )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val originalTradingNames = originalRegistration.tradingNames.map(_.tradingName).toList
    val amendedTradingNames = userAnswers.get(AllTradingNames).getOrElse(List.empty)
    val hasChangedToNo = amendedTradingNames.isEmpty && originalTradingNames.nonEmpty
    val hasChangedToYes = amendedTradingNames.nonEmpty && originalTradingNames.nonEmpty || originalTradingNames.isEmpty
    val notAmended = amendedTradingNames.nonEmpty && originalTradingNames.nonEmpty || amendedTradingNames.isEmpty && originalTradingNames.isEmpty

    if (notAmended) {
      Seq.empty
    } else if (hasChangedToNo || hasChangedToYes) {
      Seq(
        new HasTradingNameSummary().amendedAnswersRow(request.userAnswers),
      )
    } else {
      Seq.empty
    }
  }

  private def getTradingNameRows(
                                  originalRegistration: IossEtmpDisplayRegistration,
                                  userAnswers: UserAnswers
                                )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val originalTradingNames = originalRegistration.tradingNames.map(_.tradingName).toList
    val amendedTradingNames = userAnswers.get(AllTradingNames).getOrElse(List.empty)
    val addedTradingNames = amendedTradingNames.diff(originalTradingNames)
    val removedTradingNames = originalTradingNames.diff(amendedTradingNames)

    val changedTradingNames = amendedTradingNames.zip(originalTradingNames).collect {
      case (amended, original) if amended != original => amended
    } ++ amendedTradingNames.drop(originalTradingNames.size)

    val addedTradingNamesRow = if (addedTradingNames.nonEmpty) {
      userAnswers.set(AllTradingNames, changedTradingNames) match {
        case Success(amendedUserAnswer) =>
          Some(TradingNameSummary.amendedAnswersRow(amendedUserAnswer))

        case Failure(_) =>
          None
      }
    } else {
      None
    }

    val removedTradingNamesRow = Some(TradingNameSummary.removedAnswersRow(removedTradingNames))

    Seq(addedTradingNamesRow, removedTradingNamesRow).flatten
  }

  private def getBusinessContactDetailsRows(
                                             originalRegistration: IossEtmpDisplayRegistration,
                                             userAnswers: UserAnswers
                                           )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val originalDetails = originalRegistration.schemeDetails
    val amendedDetails = userAnswers.get(BusinessContactDetailsPage)

    Seq(
      if (!amendedDetails.map(_.fullName).contains(originalDetails.contactName)) {
        BusinessContactDetailsSummary.amendedContactNameRow(userAnswers)
      } else {
        None
      },

      if (!amendedDetails.map(_.telephoneNumber).contains(originalDetails.businessTelephoneNumber)) {
        BusinessContactDetailsSummary.amendedTelephoneNumberRow(userAnswers)
      } else {
        None
      },

      if (!amendedDetails.map(_.emailAddress).contains(originalDetails.businessEmailId)) {
        BusinessContactDetailsSummary.amendedEmailAddressRow(userAnswers)
      } else {
        None
      }
    )
  }

  private def getBankDetailsRows(
                                  originalRegistration: IossEtmpDisplayRegistration,
                                  userAnswers: UserAnswers
                                )(implicit request: AuthenticatedDataRequest[_]): Seq[Option[SummaryListRow]] = {

    val originalDetails = originalRegistration.bankDetails
    val amendedDetails = userAnswers.get(BankDetailsPage)

    Seq(
      if (!amendedDetails.map(_.accountName).contains(originalDetails.accountName)) {
        BankDetailsSummary.amendedAccountNameRow(userAnswers)
      } else {
        None
      },

      if (!amendedDetails.map(_.bic).contains(originalDetails.bic)) {
        BankDetailsSummary.amendedBICRow(userAnswers)
      } else {
        None
      },

      if (!amendedDetails.map(_.iban).contains(originalDetails.iban)) {
        BankDetailsSummary.amendedIBANRow(userAnswers)
      } else {
        None
      }
    )
  }
}
