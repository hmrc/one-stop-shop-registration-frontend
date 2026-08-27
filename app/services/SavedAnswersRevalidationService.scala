package services

import controllers.{SetActiveTraderResult, routes}
import models.core.Match
import models.domain.VatCustomerInfo
import models.requests.AuthenticatedDataRequest
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import repositories.AuthenticatedUserAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureSyntax.FutureOps

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SavedAnswersRevalidationService @Inject()(
                                                 coreRegistrationValidationService: CoreRegistrationValidationService,
                                                 authenticatedUserAnswersRepository: AuthenticatedUserAnswersRepository,
                                                 clock: Clock
                                               )(implicit ec: ExecutionContext) extends SetActiveTraderResult {

  def revalidateSavedUserAnswers() = {
    true
  }

  private def activeMatchRedirectUrl(maybeMatch: Option[Match])(implicit request: AuthenticatedDataRequest[_]): Future[Option[Result]] = {
    maybeMatch match {
      case Some(activeMatch) if activeMatch.isActiveTrader =>
        setActiveTraderResultAndRedirect(
          activeMatch = activeMatch,
          sessionRepository = authenticatedUserAnswersRepository,
          redirect = routes.AlreadyRegisteredController.onPageLoad()
        ).flatMap { result =>
          Some(result).toFuture
        }

      case Some(activeMatch) if activeMatch.isQuarantinedTrader(clock) =>
        Some(Redirect(routes.OtherCountryExcludedAndQuarantinedController.onPageLoad(activeMatch.memberState, activeMatch.getEffectiveDate).url)).toFuture

      case _ => None.toFuture
    }
  }


  private def revalidateUKVrn()(implicit hc: HeaderCarrier, request: AuthenticatedDataRequest[_]): Future[Option[Result]] = {
    if (checkVrnExpired(request.userAnswers.vatInfo)) {
      // TODO -> Redirect to new page? Expired VRN
      Some(Redirect(routes.JourneyRecoveryController.onPageLoad().url)).toFuture
    } else {
      coreRegistrationValidationService.searchUkVrn(request.vrn).flatMap { maybeMatch =>
        activeMatchRedirectUrl(maybeMatch)
      }
    }
  }

  private def checkVrnExpired(vatCustomerInfo: Option[VatCustomerInfo]): Boolean = {
    vatCustomerInfo match {
      case Some(vatInfo) =>
        vatInfo.deregistrationDecisionDate.exists(!_.isAfter(LocalDate.now(clock)))

      case _ => false
    }
  }
}
