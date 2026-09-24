package invirt.kafka.test

import invirt.kafka.KafkaConfig
import io.kotest.core.extensions.install
import io.kotest.core.spec.Spec
import io.kotest.extensions.testcontainers.TestContainerSpecExtension
import org.testcontainers.redpanda.RedpandaContainer

fun Spec.testRedPandaConfig(): KafkaConfig {
    val container = install(TestContainerSpecExtension(RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v24.1.16")))
    return KafkaConfig(
        servers = container.bootstrapServers,
        applicationId = "test",
        topicReplicationFactor = 1,
        defaultTopicPartitions = 1,
        consumerPollTimeoutMs = 500,
        security = null
    )
}
