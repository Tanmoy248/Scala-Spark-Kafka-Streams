package models

case class TopicOffset(
                      topic : String,
                      partition : Integer,
                      offset : Long
                      )
