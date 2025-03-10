package connectors

import config.Service
import connectors.CurrentReturnHttpParser.*
import play.api.Configuration
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReturnStatusConnector @Inject()(config: Configuration, httpClientV2: HttpClientV2)
                                     (implicit ec: ExecutionContext) extends HttpErrorFunctions {

  private val baseUrl = config.get[Service]("microservice.services.one-stop-shop-registration")

  def getCurrentReturns(vrn: Vrn)(implicit hc: HeaderCarrier): Future[CurrentReturnsResponse] =
    httpClientV2.get(url"$baseUrl/vat-returns/current-returns/$vrn").execute[CurrentReturnsResponse]
}
