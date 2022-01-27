package controllers.test

import connectors.test.TestOnlyConnector
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TestOnlyController @Inject()(testConnector: TestOnlyConnector,
                                   mcc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends FrontendController(mcc) {

  def deleteAccounts(): Action[AnyContent] = Action.async { implicit request =>
    testConnector.dropAccounts()
      .map(_ => Ok("Perf Tests Accounts MongoDB deleted"))
      .recover {
        case _: NotFoundException => Ok("Perf Tests Accounts did not exist")
      }
  }

}
