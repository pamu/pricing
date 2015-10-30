import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse

import scala.concurrent.Future
import scala.util.Try

import scala.util.Success
import scala.util.Failure


/**
 * Created by pnagarjuna on 30/10/15.
 */

case class Atomic(Value: Int, Text: String)

object Utils {
  def evaluate(cityId: Int, carVersion: Int, year: Int, month: Int, kms: Int, vt: Int = 2): Future[WSResponse] = {
    //http://www.carwale.com/used/CarValuation/default.aspx?city=198&car=998&year=1998&month=2&kms=1111&vt=2
    val baseUrl = "http://www.carwale.com/used/CarValuation/default.aspx"
    val params = List(s"city=${cityId.toString}", s"car=${carVersion.toString}", s"year=${year.toString}", s"month=${month.toString}", s"kms=${kms.toString}", s"vt=$vt")

    WS.client.url(baseUrl + params.mkString("?", "&", "")).withFollowRedirects(true)
      .get()
  }

  val carWale = "http://www.carwale.com/ajaxpro/CarwaleAjax.AjaxValuation,Carwale.ashx"

  def getMakes(carYear: Int): Future[WSResponse] = {
    val req = WS.client.url(carWale).withHeaders(
      "X-AjaxPro-Method" -> "GetValuationMakes"
    )
    val payload = Json.obj(
      "carYear" -> carYear.toString
    )
    req.post(payload)
  }

  def getModels(carYear: Int, makeId: Int): Future[WSResponse] = {
    val req = WS.client.url(carWale).withHeaders(
      "X-AjaxPro-Method" -> "GetValuationModels"
    )
    val payload = Json.obj(
      "carYear" -> carYear.toString,
      "makeId" -> makeId.toString
    )
    req.post(payload)
  }

  def getVersions(carYear: Int, modelId: Int): Future[WSResponse] = {
    val req = WS.client.url(carWale).withHeaders(
      "X-AjaxPro-Method" -> "GetValuationVersions"
    )
    val payload = Json.obj(
      "carYear" -> carYear.toString,
      "modelId" -> modelId.toString
    )
    req.post(payload)
  }

  def parse(str: String): Option[List[Atomic]] = {
    Try {
      val parsedStr = Json.parse(str)
      val textValue = (parsedStr \ "value").as[String]
      val parsedTextValue = Json.parse(textValue)
      val table = (parsedTextValue \ "Table").asOpt[List[JsValue]]
      for(someList <- table) yield {
        for(item <- someList) yield Atomic((item \ "Value").as[String].trim.toInt, (item \ "Text").as[String])
      }
    } match {
      case Success(value) => value
      case Failure(th) =>
        th.printStackTrace()
        None
    }
  }

}
