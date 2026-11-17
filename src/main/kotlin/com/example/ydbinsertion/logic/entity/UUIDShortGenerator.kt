package com.example.ydbinsertion.logic.entity

import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.generator.BeforeExecutionGenerator
import org.hibernate.generator.EventType
import org.hibernate.generator.EventTypeSets
import java.nio.ByteBuffer
import java.time.LocalDate
import java.util.Base64
import java.util.EnumSet
import java.util.UUID

class UUIDShortGenerator: BeforeExecutionGenerator {

    override fun getEventTypes(): EnumSet<EventType> {
        return EventTypeSets.INSERT_ONLY
    }

    override fun generate(
        session: SharedSessionContractImplementor?,
        owner: Any?,
        currentValue: Any?,
        eventType: EventType?
    ): Any {
        return currentValue ?: UUID.randomUUID().short()
    }

}

fun UUID.toBase64url(): ByteArray {
    val byteArray = ByteBuffer.allocate(16)
        .putLong(this.mostSignificantBits)
        .putLong(this.leastSignificantBits)
        .array()

    return Base64.getUrlEncoder().encode(byteArray)
}


fun UUID.short(): String {
    val v = String(this.toBase64url()).take(2)
    return v + shortDate() + String(this.toBase64url()).take(22)
}

fun shortDate(): String {
    return shortDate(LocalDate.now())
}

fun shortDate(date: LocalDate): String {
    val x = 366 * (date.year % 11) + date.dayOfYear - 1
    val output = charArrayOf(base64URLTable[x / 64], base64URLTable[x % 64])
    return String(output)
}

val base64URLTable = charArrayOf(
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
    'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
    'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c',
    'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
    'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w',
    'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6',
    '7', '8', '9', '-', '_'
)
