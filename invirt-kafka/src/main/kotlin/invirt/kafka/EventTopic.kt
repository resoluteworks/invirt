package invirt.kafka

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.admin.Admin
import org.apache.kafka.clients.admin.NewTopic
import kotlin.reflect.KClass

private val log = KotlinLogging.logger {}

data class EventTopic<E : KafkaEvent>(
    val name: String,
    val eventClass: KClass<E>,
    val partitions: Int = -1
) {

    fun create(config: KafkaConfig): EventTopic<E> {
        createTopics(config, this)
        return this
    }

    fun delete(config: KafkaConfig) {
        Admin.create(config.clientProperties()).deleteTopics(listOf(name)).all().get()
    }

    companion object {

        fun createTopics(config: KafkaConfig, vararg topics: EventTopic<*>) {
            val admin = Admin.create(config.clientProperties())
            val existingTopics = admin.listTopics().names().get()
            if (existingTopics.isNotEmpty()) {
                log.atInfo {
                    message = "Kafka topics already exist (${existingTopics.size})"
                    payload = mapOf("topics" to existingTopics)
                }
            }
            admin.createTopics(
                topics
                    .filter { topic -> topic.name !in existingTopics }
                    .map { topic ->
                        log.atInfo {
                            message = "Creating topic"
                            payload = mapOf("topic" to topic.name)
                        }
                        val partitions = if (topic.partitions > 0) topic.partitions else config.defaultTopicPartitions
                        val replicationFactor = config.topicReplicationFactor
                        NewTopic(topic.name, partitions, replicationFactor.toShort())
                    }
            ).values().values.forEach { it.get() }
        }
    }
}
