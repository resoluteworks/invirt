package invirt.kafka.test

import invirt.kafka.KafkaConfig
import io.kotest.core.extensions.install
import io.kotest.core.spec.Spec
import io.kotest.extensions.testcontainers.ContainerExtension
import io.kotest.extensions.testcontainers.ContainerLifecycleMode
import org.testcontainers.redpanda.RedpandaContainer

private val redpandaExtension = ContainerExtension(
    container = RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v24.1.16"),
    mode = ContainerLifecycleMode.Spec
)

fun Spec.testRedPandaConfig(): KafkaConfig {
    val container = install(redpandaExtension)
    return KafkaConfig(
        servers = container.bootstrapServers,
        applicationId = "test",
        topicReplicationFactor = 1,
        defaultTopicPartitions = 1,
        consumerPollTimeoutMs = 500,
        security = null
    )
}
