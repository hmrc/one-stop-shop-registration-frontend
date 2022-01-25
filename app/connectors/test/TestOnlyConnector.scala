package connectors.test

import config.Service
import play.api.Configuration
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TestOnlyConnector @Inject()(
                                   config: Configuration,
                                   httpClient: HttpClient
                                 )(implicit ec: ExecutionContext) {

  private val baseUrl = config.get[Service]("microservice.services.one-stop-shop-registration")
  lazy val url = s"${baseUrl}/test-only/delete-accounts"

  def dropAccounts()(implicit hc: HeaderCarrier): Future[HttpResponse] = httpClient.DELETE(url)
}