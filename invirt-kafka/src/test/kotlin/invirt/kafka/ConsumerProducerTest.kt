package invirt.kafka

import invirt.kafka.test.testRedPandaConfig
import invirt.kafka.test.withTestTopic
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration.Companion.seconds

class ConsumerProducerTest : StringSpec() {

    private val kafkaConfig = testRedPandaConfig()

    init {
        "basic message send/receive" {
            data class TestEvent(val message: String, override val key: String = "test") : KafkaEvent

            kafkaConfig.withTestTopic<TestEvent> { topic ->
                val messages = Collections.synchronizedList(mutableListOf<String>())
                kafkaConsumer(kafkaConfig, topic, 2) { event ->
                    messages.add(event.message)
                }

                val producer = Producer(kafkaConfig, topic)
                repeat(100) {
                    producer.send(TestEvent("$it"))
                }

                eventually(10.seconds) {
                    messages.size shouldBe 100
                    messages.map { it.toInt() } shouldContainExactlyInAnyOrder (0..99).toList()
                }
            }
        }

        "pub/sub" {
            data class TestEvent(override val key: String = "test") : KafkaEvent

            kafkaConfig.withTestTopic<TestEvent> { topic ->
                val counter = AtomicLong(0)
                kafkaPubSubConsumer(kafkaConfig, topic, 1) {
                    counter.incrementAndGet()
                }
                kafkaPubSubConsumer(kafkaConfig, topic, 1) {
                    counter.incrementAndGet()
                }

                val producer = Producer(kafkaConfig, topic)
                producer.send(TestEvent())

                eventually(10.seconds) {
                    counter.get() shouldBe 2
                }
            }
        }
    }
}
