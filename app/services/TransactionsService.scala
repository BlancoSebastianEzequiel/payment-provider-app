package services

import play.api.http.Status.CREATED
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.libs.ws.WSClient
import repositories.payment_provider_repository.PaymentProviderRepository

import java.io.FileInputStream
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class TransactionsService(restClient: WSClient, paymentProviderRepository: PaymentProviderRepository)(implicit ec: ExecutionContext) {
  def execute(storeId: Int, orderId: Int, total: Float, currency: String): Future[Try[String]] = {
    val paymentProvider = paymentProviderRepository.find(storeId)
    val appToken = paymentProvider.token
    val data = buildData(total, currency, paymentProvider.id)

    restClient
      .url(s"https://api.localnube.com/v1/$storeId/orders/$orderId/transactions")
      .addHttpHeaders(
        "Accept" -> "application/json",
        "User-Agent" -> "MyApp test@tiendanube.com.com",
        "Authentication" -> s"bearer $appToken"
      )
      .post(data.toString())
      .map(response =>
        response.status match {
          case CREATED =>
            val id = (Json.parse(response.body) \ "id").as[String]
            Success(id)
          case _ =>
            Failure(new Exception(response.body))
        }
      )
  }

  private def buildData(total: Float, currency: String, payment_provider_id: String): JsValue = {
    var data = Json.parse(new FileInputStream("app/resources/transaction_data.json")).as[JsObject]
    data = data + ("payment_provider_id", JsString(payment_provider_id))
    val amount = Json.obj("value" -> total, "currency" -> currency)
    data = data + ("first_event", Json.obj("amount" -> amount))
    data
  }
}
