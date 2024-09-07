package invirt.kafka

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.config.Environment

class KafkaConfigTest : StringSpec({

    "no security" {
        KafkaConfig(
            Environment.from(
                "KAFKA_SERVERS" to "localhost:9092",
                "KAFKA_APPLICATION_ID" to "test",
                "KAFKA_TOPIC_REPLICATION_FACTOR" to "3",
                "KAFKA_TOPIC_DEFAULT_PARTITION_COUNT" to "5",
                "KAFKA_CONSUMER_POLL_TIMEOUT" to "10000"
            )
        ) shouldBe
            KafkaConfig(
                servers = "localhost:9092",
                applicationId = "test",
                topicReplicationFactor = 3,
                defaultTopicPartitions = 5,
                consumerPollTimeoutMs = 10000,
                security = null
            )
    }

    "with security" {
        KafkaConfig(
            Environment.from(
                "KAFKA_SERVERS" to "localhost:9092",
                "KAFKA_APPLICATION_ID" to "test",
                "KAFKA_TOPIC_REPLICATION_FACTOR" to "3",
                "KAFKA_TOPIC_DEFAULT_PARTITION_COUNT" to "5",
                "KAFKA_CONSUMER_POLL_TIMEOUT" to "10000",
                "KAFKA_SECURITY_ENABLED" to "true",
                "KAFKA_SECURITY_MECHANISM" to "SCRAM-SHA-512",
                "KAFKA_SECURITY_USERNAME" to "user",
                "KAFKA_SECURITY_PASSWORD" to "password",
                "KAFKA_SECURITY_PROTOCOL" to "SASL_SSL"
            )
        ) shouldBe
            KafkaConfig(
                servers = "localhost:9092",
                applicationId = "test",
                topicReplicationFactor = 3,
                defaultTopicPartitions = 5,
                consumerPollTimeoutMs = 10000,
                security = KafkaSecurityConfig(
                    username = "user",
                    password = "password",
                    mechanism = "SCRAM-SHA-512",
                    securityProtocol = "SASL_SSL"
                )
            )
    }
})
