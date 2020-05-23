package config

import com.typesafe.config.ConfigFactory
import javax.inject.Singleton

@Singleton
class AppConfig {
  val config = ConfigFactory.load()
  val mongoDbName = config.getString("db.mongo.dbname")
  // kafka meta table stores the offset for the consumer group
  val kafkaMetaInfo = config.getString("kafka.metaTable")
  val kafkaTopic = config.getString("kafka.topic")
  val kafkaGroupId = config.getString("kafka.groupId")

  // summary of the timewise report
  val reportSummary = config.getString("db.mongo.collections.reportSummary")

}

object AppConfig extends AppConfig
