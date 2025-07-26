package invirt.kafka

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.SaslConfigs
import org.http4k.config.Environment
import org.http4k.config.EnvironmentKey
import org.http4k.lens.boolean
import org.http4k.lens.int
import org.http4k.lens.long

data class KafkaConfig(
    val servers: String,
    val applicationId: String,
    val topicReplicationFactor: Int,
    val defaultTopicPartitions: Int,
    val consumerPollTimeoutMs: Long,
    val security: KafkaSecurityConfig?
) {

    companion object {
        operator fun invoke(env: Environment): KafkaConfig = KafkaConfig(
            servers = EnvironmentKey.required("KAFKA_SERVERS")(env),
            applicationId = EnvironmentKey.defaulted("KAFKA_APPLICATION_ID", "application")(env),
            topicReplicationFactor = EnvironmentKey.int().required("KAFKA_TOPIC_REPLICATION_FACTOR")(env),
            defaultTopicPartitions = EnvironmentKey.int().defaulted("KAFKA_TOPIC_DEFAULT_PARTITION_COUNT", 16)(env),
            consumerPollTimeoutMs = EnvironmentKey.long().defaulted("KAFKA_CONSUMER_POLL_TIMEOUT", 5000)(env),
            security = KafkaSecurityConfig(env)
        )
    }

    fun clientProperties(): Map<String, String> {
        val properties = mutableMapOf(
            CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to servers
        )
        security?.let {
            properties[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = it.securityProtocol
            properties[SaslConfigs.SASL_MECHANISM] = it.mechanism
            properties[SaslConfigs.SASL_JAAS_CONFIG] =
                "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"${it.username}\" password=\"${it.password}\";"
        }
        return properties
    }
}

data class KafkaSecurityConfig(
    val username: String,
    val password: String,
    val mechanism: String = "SCRAM-SHA-512",
    val securityProtocol: String = "SASL_SSL"
) {
    companion object {
        operator fun invoke(env: Environment): KafkaSecurityConfig? =
            if (EnvironmentKey.boolean().defaulted("KAFKA_SECURITY_ENABLED", false)(env)) {
                KafkaSecurityConfig(
                    username = EnvironmentKey.required("KAFKA_SECURITY_USERNAME")(env),
                    password = EnvironmentKey.required("KAFKA_SECURITY_PASSWORD")(env),
                    mechanism = EnvironmentKey.defaulted("KAFKA_SECURITY_MECHANISM", "SCRAM-SHA-512")(env),
                    securityProtocol = EnvironmentKey.defaulted("KAFKA_SECURITY_PROTOCOL", "SASL_SSL")(env)
                )
            } else {
                null
            }
    }
}
