package invirt.kafka

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.Serdes

private val log = KotlinLogging.logger {}

/**
 * Convenience wrapper for a Kafka producer.
 */
class Producer<E : KafkaEvent>(
    config: KafkaConfig,
    private val topic: EventTopic<E>
) {

    private val kafkaProducer = KafkaProducer<String, E>(config.clientProperties(), Serdes.String().serializer(), streamJsonSerializer())

    init {
        log.atInfo {
            message = "Created Kafka producer"
            payload = mapOf(
                "topic" to topic.name
            )
        }
    }

    fun send(event: E) {
        kafkaProducer.send(ProducerRecord(topic.name, event.key, event))
    }

    fun send(events: Collection<E>) {
        events.forEach { send(it) }
    }
}
