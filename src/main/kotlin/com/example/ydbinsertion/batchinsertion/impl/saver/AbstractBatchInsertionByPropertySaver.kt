package com.example.ydbinsertion.batchinsertion.impl.saver

import com.example.ydbinsertion.batchinsertion.api.saver.BatchInsertionByPropertySaver
import com.example.ydbinsertion.batchinsertion.exception.BatchInsertionException
import com.example.ydbinsertion.logic.entity.BaseEntity
import com.example.ydbinsertion.logic.entity.short
import java.sql.Connection
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

abstract class AbstractBatchInsertionByPropertySaver<E : BaseEntity>(
    conn: Connection,
    private val batchSize: Int,
    private val entityClass: KClass<E>,
) : AbstractBatchInsertionSaver(conn), BatchInsertionByPropertySaver<E> {

    private var counter = 0
    var columns: Set<KProperty1<E, *>>? = null

    @Suppress("UNCHECKED_CAST")
    protected fun addIdIfAbsent(data: Map<out KProperty1<E, *>, Any?>): Map<out KProperty1<E, *>, Any?> {
        if (data.keys.any { it.name == "id" }) return data

        val idProperty = entityClass.memberProperties
            .find { it.name == "id" }
            ?.let { it as KProperty1<E, Any?> }
            ?: throw BatchInsertionException("Id should be defined in entity for insert")

        return data + (idProperty to UUID.randomUUID().short())
    }

    override fun addDataForSave(data: Map<out KProperty1<E, *>, Any?>) {
        columns?.let { it.takeIf { data.keys == it } ?: throw BatchInsertionException("Keys should be equal in one saver") }
        columns ?: let { columns = data.keys }

        counter++
        if (counter % batchSize == 0) {
            log.info("save batch insertion $batchSize")
            saveData()
        }
    }

    override fun saveData() {
        columns ?: throw BatchInsertionException("Columns should be defined in the saver")
    }

    override fun commit() {
        if (counter % batchSize != 0) {
            saveData()
        }
        log.info("start commit $counter data")
        counter = 0
        super.commit()
    }
}
