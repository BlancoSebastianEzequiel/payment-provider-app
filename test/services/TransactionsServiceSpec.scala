package services

import mockws.MockWS
import mockws.MockWSHelpers.Action
import org.mockito.Mockito.when
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, Created}
import play.api.test.Helpers.POST
import repositories.payment_provider_repository.{PaymentProvider, PaymentProviderRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

class TransactionsServiceSpec extends PlaySpec with ScalaFutures with MockitoSugar {
  val storeId = 33
  val orderId = 900
  val transactionId = "transactionId"
  val paymentProvider: PaymentProvider = PaymentProvider("id", storeId, "token")
  val paymentProviderRepository: PaymentProviderRepository = mock[PaymentProviderRepository]

  def createService(result: Result): TransactionsService = {
    val ws: WSClient = MockWS {
      case (POST, "https://api.localnube.com/v1/33/orders/900/transactions") =>
        Action { result }
    }
    new TransactionsService(ws, paymentProviderRepository)
  }

  "execute" should {
    "returns the transaction id if the request succeeds" in {
      val service = createService(Created(Json.obj("id" -> transactionId)))
      when(paymentProviderRepository.find(storeId)).thenReturn(paymentProvider)
      val result = service.execute(storeId, orderId, 2, "ARS")
      result.futureValue shouldBe Success(transactionId)
    }

    "returns the error if the request fails" in {
      val service = createService(BadRequest("ERROR"))
      when(paymentProviderRepository.find(storeId)).thenReturn(paymentProvider)
      val result = service.execute(storeId, orderId, 2, "ARS")
      result.futureValue.isFailure shouldBe true
    }
  }
}
