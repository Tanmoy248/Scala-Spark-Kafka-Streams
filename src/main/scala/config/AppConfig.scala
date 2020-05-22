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

}

object AppConfig extends AppConfig
