package controllers

import play.api.libs.json.{JsValue, Json}

import javax.inject._
import play.api.mvc._
import play.api.libs.ws.WSClient
import repositories.payment_provider_repository.{FileBasedPaymentProviderRepository, PaymentProvider, PaymentProviderRepository}
import services.{AppAuthorizationService, PaymentProviderService}

import java.nio.file.Paths
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val ws: WSClient, val controllerComponents: ControllerComponents)(implicit ec: ExecutionContext) extends BaseController {
  val paymentProviderRepository: PaymentProviderRepository = new FileBasedPaymentProviderRepository(Paths.get("tmp"))
  val paymentProviderService = new PaymentProviderService(ws, paymentProviderRepository)
  val authorizationService = new AppAuthorizationService(ws)

  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def checkoutUrl(): Action[JsValue] = Action(parse.json) { implicit request =>
    val storeId = (request.body \ "storeId").as[Int]
    val orderId = (request.body \ "orderId").as[Int]
    val currency = (request.body \ "currency").as[String]
    val total = (request.body \ "total").as[Float]
    val callbackUrls = (request.body \ "callbackUrls").as[JsValue]
    val baseUrl = "https://localhost:9443"
    val params = s"storeId=$storeId&orderId=$orderId&currency=$currency&total=$total&callbackUrls=$callbackUrls"
    val redirectUrl = s"$baseUrl/payment_redirect?$params"
    Ok(Json.obj("success" -> true, "redirect_url" -> redirectUrl))
  }

  def redirect(): Action[AnyContent] = Action.async { implicit request =>
    val code = request.queryString.get("code")
    val clientId = "159"
    val clientSecret = "qDawDH34hvNLBZO9aOnTMpbcn2L394ovwdha8Hw87i3UlBaE"

    code match {
      case Some(code) =>
        authorizationService.authorize(code.head, clientId, clientSecret)
          .flatMap {
              case Failure(error) =>
                Future(BadRequest(Json.obj("authorization_failed" -> Json.parse(error.getMessage))))
              case Success((storeId, appToken)) =>
                createPaymentProvider(storeId, appToken)
            }
      case None =>
        Future(BadRequest(Json.obj("code" -> "Missing parameter")))
    }
  }

  private def createPaymentProvider(storeId: Int, appToken: String) = {
    paymentProviderService.create(storeId, appToken).map {
      case Success(paymentProvider: PaymentProvider) =>
        Ok(Json.obj("id" -> paymentProvider.id))
      case Failure(error) => BadRequest(Json.parse(error.getMessage))
    }
  }
}
