package controllers

import play.api.libs.json.{JsDefined, JsValue, Json}

import javax.inject._
import play.api.mvc._
import play.api.libs.ws.WSClient
import repositories.payment_provider_repository.{FileBasedPaymentProviderRepository, PaymentProvider, PaymentProviderRepository}
import services.{AppAuthorizationService, PaymentProviderService}

import java.nio.file.Paths
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

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
    val storeId = (request.body \ "storeId").as[String]
    val orderId = (request.body \ "orderId").as[String]
    val currency = (request.body \ "currency").as[String]
    val total = (request.body \ "total").as[Float]
    val baseUrl = "https://localhost:9443"
    val params = s"storeId=$storeId&orderId=$orderId&currency=$currency&total=$total"
    val redirectUrl = s"$baseUrl/payment_redirect/?$params"
    Ok(Json.obj("success" -> true, "redirect_url" -> redirectUrl))
  }

  def redirect(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val code = Try((request.body \ "code").as[String])
    val clientId = "159"
    val clientSecret = "qDawDH34hvNLBZO9aOnTMpbcn2L394ovwdha8Hw87i3UlBaE"

    code match {
      case Success(code) =>
        authorizationService.authorize(code, clientId, clientSecret)
          .flatMap {
              case Failure(error) =>
                Future(BadRequest(error.getMessage))
              case Success((storeId, appToken)) =>
                createPaymentProvider(storeId, appToken)
            }
      case Failure(_) =>
        Future(BadRequest(Json.obj("code" -> "Missing parameter")))
    }
  }

  private def createPaymentProvider(storeId: String, appToken: String) = {
    paymentProviderService.create(storeId, appToken).map {
      case Success(paymentProvider: PaymentProvider) =>
        Ok(Json.obj("id" -> paymentProvider.id))
      case Failure(error) => BadRequest(error.getMessage)
    }
  }
}
