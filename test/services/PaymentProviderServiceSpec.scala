package services

import akka.Done
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
import play.api.mvc.Results.{BadRequest, Ok}
import play.api.test.Helpers.POST
import repositories.payment_provider_repository.{PaymentProvider, PaymentProviderRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

class PaymentProviderServiceSpec extends PlaySpec with ScalaFutures with MockitoSugar {
  val storeId = "storeId"
  val appToken = "appToken"
  val paymentProvider: PaymentProvider = PaymentProvider("paymentProviderId", storeId, appToken)
  val paymentProviderRepository: PaymentProviderRepository = mock[PaymentProviderRepository]

  def createService(result: Result): PaymentProviderService = {
    val ws: WSClient = MockWS {
      case (POST, "https://api.localnube.com/v1/storeId/payment_providers") => Action { result }
    }
    new PaymentProviderService(ws, paymentProviderRepository)
  }

  "create" should {
    "returns the new payment provider" in {
      val service = createService(Ok(Json.obj("id" -> paymentProvider.id)))
      when(paymentProviderRepository.save(paymentProvider)).thenReturn(Done)
      val result = service.create(storeId, appToken)
      result.futureValue shouldEqual Success(paymentProvider)
    }

    "returns the error if the request fails" in {
      val service = createService(BadRequest("ERROR"))
      when(paymentProviderRepository.find(storeId)).thenReturn(paymentProvider)
      val result = service.create(storeId, appToken)
      result.futureValue.isFailure shouldBe true
    }
  }
}
