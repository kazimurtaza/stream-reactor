package com.datamountaineer.streamreactor.connect.mongodb.config


import com.datamountaineer.streamreactor.connect.errors.ThrowErrorPolicy
import com.datamountaineer.streamreactor.connect.rowkeys.{StringGenericRowKeyBuilder, StringStructFieldsStringKeyBuilder}
import org.apache.kafka.common.config.ConfigException
import org.scalatest.{Matchers, WordSpec}

import scala.collection.JavaConversions._

class MongoSinkSettingsTest extends WordSpec with Matchers {
  "MongoSinkSettings" should {
    "default the host if the hosts settings not provided" in {
      val map = Map(
        MongoConfig.DATABASE_CONFIG -> "database1",
        MongoConfig.KCQL_CONFIG -> "INSERT INTO collection1 SELECT * FROM topic1"
      )

      val config = MongoConfig(map)
      val settings = MongoSinkSettings(config)
      settings.database shouldBe "database1"
      settings.hosts shouldBe Seq(MongoConfig.HOSTS_DEFAULT)
      settings.batchSize shouldBe MongoConfig.BATCH_SIZE_CONFIG_DEFAULT
      settings.keyBuilderMap.size shouldBe 0
      settings.routes.size shouldBe 1
      settings.errorPolicy shouldBe ThrowErrorPolicy()
      settings.ignoredField shouldBe Map("topic1" -> Set.empty)
    }

    "handle two topics" in {
      val map = Map(
        MongoConfig.DATABASE_CONFIG -> "database1",
        MongoConfig.KCQL_CONFIG -> "INSERT INTO collection1 SELECT * FROM topic1;INSERT INTO coll2 SELECT a as F1, b as F2 FROM topic2"
      )

      val config = MongoConfig(map)
      val settings = MongoSinkSettings(config)
      settings.database shouldBe "database1"
      settings.hosts shouldBe Seq(MongoConfig.HOSTS_DEFAULT)
      settings.batchSize shouldBe MongoConfig.BATCH_SIZE_CONFIG_DEFAULT
      settings.keyBuilderMap.size shouldBe 0
      settings.routes.size shouldBe 2
      settings.errorPolicy shouldBe ThrowErrorPolicy()
      settings.ignoredField shouldBe Map("topic1" -> Set.empty, "topic2" -> Set.empty)
    }

    "handle ingore fields" in {
      val map = Map(
        MongoConfig.DATABASE_CONFIG -> "database1",
        MongoConfig.KCQL_CONFIG -> "INSERT INTO collection1 SELECT * FROM topic1 IGNORE a,b,c"
      )

      val config = MongoConfig(map)
      val settings = MongoSinkSettings(config)
      settings.database shouldBe "database1"
      settings.hosts shouldBe Seq(MongoConfig.HOSTS_DEFAULT)
      settings.batchSize shouldBe MongoConfig.BATCH_SIZE_CONFIG_DEFAULT
      settings.keyBuilderMap.size shouldBe 0
      settings.routes.size shouldBe 1
      settings.keyBuilderMap.size shouldBe 0
      settings.errorPolicy shouldBe ThrowErrorPolicy()
      settings.ignoredField shouldBe Map("topic1" -> Set("a", "b", "c"))
    }

    "handle primary key fields" in {
      val map = Map(
        MongoConfig.DATABASE_CONFIG -> "database1",
        MongoConfig.KCQL_CONFIG -> "INSERT INTO collection1 SELECT * FROM topic1 PK a,b"
      )

      val config = MongoConfig(map)
      val settings = MongoSinkSettings(config)
      settings.database shouldBe "database1"
      settings.hosts shouldBe Seq(MongoConfig.HOSTS_DEFAULT)
      settings.batchSize shouldBe MongoConfig.BATCH_SIZE_CONFIG_DEFAULT
      settings.keyBuilderMap.size shouldBe 0
      settings.routes.size shouldBe 1
      settings.keyBuilderMap.size shouldBe 0
      settings.errorPolicy shouldBe ThrowErrorPolicy()
      settings.ignoredField shouldBe Map("topic1" -> Set.empty)
    }

    "throw an exception if the kqcl is not valid" in {
      val map = Map(
        MongoConfig.DATABASE_CONFIG -> "database1",
        MongoConfig.KCQL_CONFIG -> "INSERT INTO  SELECT * FROM topic1"
      )

      val config = MongoConfig(map)
      intercept[ConfigException] {
        MongoSinkSettings(config)
      }
    }

    "throw a ConfigException if the host is missing the port" in {
      val map = Map(
        MongoConfig.HOSTS_CONFIG -> "hostA:29511,host1,host2:12536",
        MongoConfig.DATABASE_CONFIG -> "database1",
        MongoConfig.KCQL_CONFIG -> "INSERT INTO collection1 SELECT * FROM topic1"
      )

      val config = MongoConfig(map)
      intercept[ConfigException] {
        MongoSinkSettings(config)
      }
    }

    "throw an exception if the database is an empty string" in {
      val map = Map(
        MongoConfig.DATABASE_CONFIG -> "",
        MongoConfig.KCQL_CONFIG -> "INSERT INTO collection1 SELECT * FROM topic1"
      )

      val config = MongoConfig(map)
      intercept[ConfigException] {
        MongoSinkSettings(config)
      }
    }
  }
}
