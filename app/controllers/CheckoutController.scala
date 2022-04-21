package controllers

import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, BaseController, ControllerComponents}
import repositories.payment_provider_repository.{FileBasedPaymentProviderRepository, PaymentProviderRepository}
import services.TransactionsService

import java.nio.file.Paths
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class CheckoutController @Inject()(val ws: WSClient, val controllerComponents: ControllerComponents)(implicit ec: ExecutionContext) extends BaseController {
  val paymentProviderRepository: PaymentProviderRepository = new FileBasedPaymentProviderRepository(Paths.get("tmp"))
  val transactionsService = new TransactionsService(ws, paymentProviderRepository)

  def redirect(storeId: String, orderId: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val currency = (request.body \ "currency").as[String]
    val total = (request.body \ "total").as[Float]
    transactionsService.execute(storeId, orderId, total, currency).map {
      case Failure(exception) => BadRequest(exception.getMessage)
      case Success(transactionId) => Created(Json.obj("id" -> transactionId))
    }
  }
}
