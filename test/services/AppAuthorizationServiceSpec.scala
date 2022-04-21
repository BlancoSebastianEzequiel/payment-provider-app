package services

import mockws.MockWS
import mockws.MockWSHelpers.Action
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import play.api.test.Helpers.POST

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

class AppAuthorizationServiceSpec extends PlaySpec with ScalaFutures with MockitoSugar {
  val accessToken = "accessToken"
  val storeId = "storeId"
  val clientId = "clientId"
  val clientSecret = "clientSecret"

  def createService(result: Result): AppAuthorizationService = {
    val ws: WSClient = MockWS {
      case (POST, "https://www.tiendanube.com/apps/authorize/token") => Action { result }
    }
    new AppAuthorizationService(ws)
  }

  "authorize" should {
    "returns the appToken and the storeId" in {
      val service = createService(Ok(Json.obj("access_token" -> accessToken, "user_id" -> storeId)))
      val result = service.authorize("code", clientId, clientSecret)
      result.futureValue shouldEqual Success(storeId, accessToken)
    }

    "returns an error if the request fails" in {
      val service = createService(Ok(Json.obj("error" -> "Some Error")))
      val result = service.authorize("code", clientId, clientSecret)
      result.futureValue.isFailure shouldBe true
    }
  }
}
