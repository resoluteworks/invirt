package invirt.kafka

import invirt.kafka.test.testRedPandaConfig
import invirt.utils.uuid7
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import org.apache.kafka.clients.admin.Admin

class EventTopicTest : StringSpec() {

    private val kafkaConfig = testRedPandaConfig()

    init {
        "create/delete topic" {
            data class TestEvent(override val key: String = "test") : KafkaEvent

            val topicName = "test-${uuid7()}"
            val admin = Admin.create(kafkaConfig.clientProperties())

            val initialTopicCount = admin.listTopics().names().get().size

            val topic = EventTopic(topicName, TestEvent::class).create(kafkaConfig)

            // Number of topics increases by 1 after creating a new topic
            admin.listTopics().names().get().size shouldBe initialTopicCount + 1

            // Topic exists after being created
            admin.listTopics().names().get() shouldContain topicName

            // Creating a topic again doesn't do anything
            topic.create(kafkaConfig)
            admin.listTopics().names().get().size shouldBe initialTopicCount + 1

            topic.delete(kafkaConfig)

            // Topic does not exist after being deleted
            admin.listTopics().names().get() shouldNotContain topicName
        }
    }
}
