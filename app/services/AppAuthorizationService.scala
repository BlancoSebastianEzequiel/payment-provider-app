package services

import play.api.libs.json.{JsDefined, Json}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class AppAuthorizationService(restClient: WSClient)(implicit ec: ExecutionContext) {
  def authorize(code: String, clientId: String, clientSecret: String): Future[Try[(String, String)]] = {
    restClient
      .url("https://www.tiendanube.com/apps/authorize/token")
      .addHttpHeaders("Accept" -> "application/json")
      .post(
        Json.obj(
          "client_id" -> clientId,
          "client_secret" -> clientSecret,
          "grant_type" -> "authorization_code",
          "code" -> code
        ).toString()
      )
      .map(response => {
        val body = Json.parse(response.body)
        body \ "error" match {
          case _: JsDefined =>
            Failure(new Exception(response.body))
          case _ =>
            val appToken = (body \ "access_token").as[String]
            val storeId = (body \ "user_id").as[String]
            Success(storeId, appToken)
        }
      })
  }
}
