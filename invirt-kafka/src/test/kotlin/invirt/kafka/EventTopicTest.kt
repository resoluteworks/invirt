package invirt.kafka

import invirt.kafka.test.testRedPandaConfig
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import org.apache.kafka.clients.admin.Admin

class EventTopicTest : StringSpec() {

    private val kafkaConfig = testRedPandaConfig()

    init {
        "create/delete topic" {
            data class TestEvent(override val key: String = "test") : KafkaEvent

            val topicName = "test-${uuid7()}"
            val admin = Admin.create(kafkaConfig.clientProperties())

            admin.listTopics().names().get() shouldNotContain topicName

            val topic = EventTopic(topicName, TestEvent::class).create(kafkaConfig)

            // Topic exists after being created
            admin.listTopics().names().get() shouldContain topicName

            // Creating a topic again doesn't fail and the topic is still there. Redpanda creates its own
            // internal topics lazily, so the broker's total topic count is not something this test can pin.
            topic.create(kafkaConfig)
            admin.listTopics().names().get() shouldContain topicName

            topic.delete(kafkaConfig)

            // Topic does not exist after being deleted
            admin.listTopics().names().get() shouldNotContain topicName
        }
    }
}
