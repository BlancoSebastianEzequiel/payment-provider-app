package controllers

import play.api.libs.json.{JsDefined, JsValue, Json}

import javax.inject._
import play.api.mvc._
import play.api.libs.ws.WSClient
import repositories.payment_provider_repository.{FileBasedPaymentProviderRepository, PaymentProvider, PaymentProviderRepository}
import services.PaymentProviderService
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

  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def redirect(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val code = Try((request.body \ "code").as[String])
    code match {
      case Success(code) =>
        ws
          .url("https://www.tiendanube.com/apps/authorize/token")
          .addHttpHeaders("Accept" -> "application/json")
          .post(
            Json.obj(
              "client_id" -> "158",
              "client_secret" -> "sD6KcaJu1Jsta9dL3eSPojj1KwJNCZ7M9QA09yKZqZHm1Vo3",
              "grant_type" -> "authorization_code",
              "code" -> code
            ).toString()
          )
          .flatMap(response => {
            val body = Json.parse(response.body)
            (body \ "error") match {
              case _  : JsDefined =>
                Future(BadRequest(Json.parse(response.body)))
              case _ =>
                val paymentProviderResponse = for {
                  appToken <- Try((body \ "access_token").as[String])
                  storeId <- Try((body \ "user_id").as[String])
                } yield createPaymentProvider(storeId, appToken)
                paymentProviderResponse match {
                  case Success(value) =>
                    value
                  case Failure(error) =>
                    Future(BadRequest(error.getMessage))
                }
            }
          })(ec)
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
