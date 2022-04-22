package controllers

import mockws.{MockWS, MockWSHelpers}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.http.Status.{BAD_REQUEST, CREATED}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContent, Result}
import play.api.mvc.Results.{BadRequest, Created}
import play.api.test.Helpers.{GET, POST, contentAsString, contentType, defaultAwaitTimeout, status, stubControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import repositories.payment_provider_repository.{FileBasedPaymentProviderRepository, PaymentProvider, PaymentProviderRepository}

import java.nio.file.Paths
import scala.concurrent.ExecutionContext.Implicits.global

class CheckoutControllerSpec extends PlaySpec with GuiceOneAppPerTest with Injecting with MockWSHelpers {
  val storeId = 2718
  val orderId = 3000
  val transactionId = "transactionId"
  val paymentProvider: PaymentProvider = PaymentProvider("id", storeId, "token")
  val paymentProviderRepository: PaymentProviderRepository = new FileBasedPaymentProviderRepository(Paths.get("tmp"))

  "POST redirect" should {
    def createController(result: Result): CheckoutController = {
      val ws = MockWS {
        case (POST, "https://api.localnube.com/v1/2718/orders/3000/transactions") => Action { result }
      }
      new CheckoutController(ws, stubControllerComponents())
    }

    def createPayload(storeId: Int, orderId: Int, total: Float, currency: String): FakeRequest[AnyContent] = {
      val params = s"storeId=$storeId&orderId=$orderId&currency=$currency&total=$total"
      FakeRequest(GET, s"/payment_redirect/?$params")
    }

    val payload = createPayload(storeId, orderId, 2, "ARS")

    "returns the created status response" in {
      paymentProviderRepository.save(paymentProvider)
      val controller = createController(Created(Json.obj("id" -> transactionId)))
      val response = controller.redirect().apply(payload)

      status(response) mustBe CREATED
      contentType(response) mustBe Some("application/json")
      contentAsString(response) mustEqual Json.obj("id" -> transactionId).toString()
    }

    "returns the badRequest status response" in {
      paymentProviderRepository.save(paymentProvider)
      val controller = createController(BadRequest("ERROR"))
      val response = controller.redirect().apply(payload)

      status(response) mustBe BAD_REQUEST
      contentType(response) mustBe Some("text/plain")
      contentAsString(response) mustEqual "ERROR"
    }
  }
}
