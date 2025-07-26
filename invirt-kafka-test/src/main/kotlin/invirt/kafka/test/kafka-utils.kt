package invirt.kafka.test

import invirt.kafka.EventTopic
import invirt.kafka.KafkaConfig
import invirt.kafka.KafkaEvent
import invirt.utils.uuid7

inline fun <reified E : KafkaEvent> KafkaConfig.withTestTopic(partitions: Int = 1, block: (EventTopic<E>) -> Unit) {
    val topic = EventTopic("test-${uuid7()}", E::class, partitions).create(this)
    try {
        block(topic)
    } finally {
        topic.delete(this)
    }
}
