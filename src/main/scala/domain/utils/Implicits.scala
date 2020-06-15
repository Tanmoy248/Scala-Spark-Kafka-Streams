package domain.utils

import play.api.libs.json.{JsObject, Json, Writes}
import models.Info

class Implicits {
  implicit val InfoWrites = new Writes[Info]{
    def writes(i : Info): JsObject = Json.obj(
      "key1" -> i.key1,
      "key2" -> i.key2,
      "key3" -> i.key3
    )
  }

}

object Implicits extends  Implicits
