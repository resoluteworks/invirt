package invirt.kafka

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import kotlin.reflect.KClass

internal val defaultKafkaJsonMapper = jacksonObjectMapper()
    .registerModule(JavaTimeModule())
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

internal fun <E : KafkaEvent> streamJsonSerializer(): Serializer<E> = Serializer<E> { _, value ->
    defaultKafkaJsonMapper.writeValueAsString(value).toByteArray()
}

internal fun <E : KafkaEvent> streamJsonDeserializer(valueClass: KClass<E>): Deserializer<E> = Deserializer<E> { _, value ->
    defaultKafkaJsonMapper.readValue(String(value), valueClass.java)
}
