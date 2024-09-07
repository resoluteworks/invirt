package invirt.kafka

import invirt.utils.threads.ThreadPool
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.Serdes
import org.http4k.lens.StringBiDiMappings.uuid
import java.time.Duration

private val log = KotlinLogging.logger {}

fun <E : KafkaEvent> kafkaConsumer(
    config: KafkaConfig,
    topic: EventTopic<E>,
    threads: Int,
    handler: (E) -> Unit
) = Consumer(config, topic, threads, false, handler)

fun <E : KafkaEvent> kafkaPubSubConsumer(
    config: KafkaConfig,
    topic: EventTopic<E>,
    threads: Int,
    handler: (E) -> Unit
) = Consumer(config, topic, threads, true, handler)

class Consumer<E : KafkaEvent>(
    private val config: KafkaConfig,
    private val topic: EventTopic<E>,
    private val threads: Int,
    private val pubSub: Boolean = false,
    private val handler: (E) -> Unit
) {

    @Volatile
    private var stopped: Boolean = false

    init {
        val threadPool = ThreadPool<Unit>(threads)
        val groupId = if (pubSub) {
            "${topic.name}.pubsub.${uuid()}"
        } else {
            "${topic.name}.competing-consumer"
        }

        val properties = config.clientProperties()
            .plus(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest")
            .plus(ConsumerConfig.GROUP_ID_CONFIG to groupId)

        repeat(threads) {
            val kafkaConsumer = KafkaConsumer(properties, Serdes.String().deserializer(), streamJsonDeserializer(topic.eventClass))
            kafkaConsumer.subscribe(listOf(topic.name))
            threadPool.submit {
                runConsumer(kafkaConsumer, groupId)
            }
        }
    }

    private fun runConsumer(kafkaConsumer: KafkaConsumer<String, E>, groupId: String) {
        log.atInfo {
            message = "Running Kafka consumer"
            payload = mapOf(
                "topic" to topic.name,
                "threads" to threads,
                "pubSub" to pubSub,
                "groupId" to groupId
            )
        }
        while (!stopped) {
            val records = kafkaConsumer.poll(Duration.ofMillis(config.consumerPollTimeoutMs))
            log.atTrace {
                message = "Received Kafka records"
                payload = mapOf(
                    "topic" to topic.name,
                    "groupId" to groupId,
                    "records" to records.count()
                )
            }
            records.forEach { record ->
                handler(record.value())
            }
        }

        kafkaConsumer.unsubscribe()
        kafkaConsumer.close()
        log.info { "Stopped consumer for topic $topic and group $groupId" }
    }
}
