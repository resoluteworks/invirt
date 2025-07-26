package invirt.kafka

import invirt.kafka.test.testRedPandaConfig
import invirt.kafka.test.withTestTopic
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ConsumerTest : StringSpec() {

    private val kafkaConfig = testRedPandaConfig()

    init {
        "stop" {
            data class TestEvent(val message: String, override val key: String = "test") : KafkaEvent

            kafkaConfig.withTestTopic<TestEvent> { topic ->
                val consumer = kafkaConsumer(kafkaConfig, topic, 2) { }
                consumer.stop()
                consumer.stopped shouldBe true
            }
        }
    }
}
