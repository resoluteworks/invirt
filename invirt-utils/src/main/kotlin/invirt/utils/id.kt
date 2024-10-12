package invirt.utils

import com.github.f4b6a3.uuid.UuidCreator
import org.apache.commons.codec.binary.Base32
import java.nio.ByteBuffer
import java.util.*

fun uuid7(): String = UuidCreator.getTimeOrderedEpoch()
    .toString()
    .replace("-", "")

fun UUID.toByteArray(): ByteArray {
    val bb: ByteBuffer = ByteBuffer.allocate(16)
    bb.putLong(mostSignificantBits)
    bb.putLong(leastSignificantBits)
    return bb.array()
}

fun UUID.toBase32(): String = Base32().encodeAsString(toByteArray()).lowercase().replace("=", "")

fun uuidBase32(): String = UUID.randomUUID().toBase32()
