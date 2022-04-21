package services

import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import repositories.payment_provider_repository.{PaymentProvider, PaymentProviderRepository}

import java.io.FileInputStream
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class PaymentProviderService(restClient: WSClient, repository: PaymentProviderRepository)(implicit ec: ExecutionContext) {
  def create(storeId: String, appToken: String): Future[Try[PaymentProvider]] = {
    restClient
      .url(s"https://api.localnube.com/v1/$storeId/payment_providers")
      .addHttpHeaders(
        "Accept" -> "application/json",
        "Authentication" -> s"bearer $appToken",
        "User-Agent" -> "MyApp test@tiendanube.com.com"
      )
      .post(Json.parse(new FileInputStream("app/resources/payment_providers_data.json")).toString())
      .map(response =>
        response.status match {
          case OK =>
            val id = (Json.parse(response.body) \ "id").as[String]
            val paymentProvider = PaymentProvider(id, storeId, appToken)
            repository.save(paymentProvider)
            Success(paymentProvider)
          case _ => Failure(new Exception(response.body))
        }
      )
  }
}
