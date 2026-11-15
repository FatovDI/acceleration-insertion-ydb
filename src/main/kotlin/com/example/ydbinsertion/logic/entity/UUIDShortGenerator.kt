package com.example.ydbinsertion.logic.entity

import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.generator.BeforeExecutionGenerator
import org.hibernate.generator.EventType
import org.hibernate.generator.EventTypeSets
import java.nio.ByteBuffer
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


fun UUID.short() = String(this.toBase64url()).take(22)

