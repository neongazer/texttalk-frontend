package entities

import play.api.libs.json._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsNumber

object Msg {

  implicit val format = new Format[Msg]{
    def writes(msg: Msg): JsValue = {
      JsObject(Seq(
        ("timestamp", JsNumber(msg.timestamp)),
        ("user_id", JsString(msg.uid.uid)),
        ("action", JsString(msg.action)),
        ("body", JsString(msg.body))
      ))
    }

    def reads(json: JsValue): JsResult[Msg] =
      for{
        timeStamp <- Json.fromJson[Long](json \ "timestamp")
        uid <- Json.fromJson[String](json \ "user_id")
        action <- Json.fromJson[String](json \ "action")
        msg <- Json.fromJson[String](json \ "body")
      } yield Msg(timeStamp, UserId(uid), action, msg)
  }

}

case class Msg(timestamp: Long, uid: UserId, action: String, body: String) extends JsonMessage