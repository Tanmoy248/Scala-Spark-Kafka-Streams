package models

import play.api.libs.json.JsValue

case class IntermediateSparkOutput(
                                    timeBucket : JsValue,
                                    result : (Integer,Integer)
                                  )
