package controllers

import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Results}
import repositories.payment_provider_repository.{FileBasedPaymentProviderRepository, PaymentProviderRepository}
import services.TransactionsService

import java.nio.file.Paths
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class CheckoutController @Inject()(val ws: WSClient, val controllerComponents: ControllerComponents)(implicit ec: ExecutionContext) extends BaseController {
  val paymentProviderRepository: PaymentProviderRepository = new FileBasedPaymentProviderRepository(Paths.get("tmp"))
  val transactionsService = new TransactionsService(ws, paymentProviderRepository)

  def redirect(): Action[AnyContent] = Action.async { implicit request =>
    val storeId = request.queryString("storeId").head.toInt
    val orderId = request.queryString("orderId").head.toInt
    val currency = request.queryString("currency").head
    val total = request.queryString("total").head.toFloat
    val callbackUrls = Json.parse(request.queryString("callbackUrls").head)
    val success = (callbackUrls \ "success").as[String]
    val failure = (callbackUrls \ "failure").as[String]
    transactionsService.execute(storeId, orderId, total, currency).map {
      case Failure(exception) =>
        BadRequest(exception.getMessage)
        Results.Redirect(failure)
      case Success(transactionId) =>
        Created(Json.obj("id" -> transactionId))
        Results.Redirect(success)
    }
  }
}
