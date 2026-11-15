package com.example.ydbinsertion.batchinsertion.impl.saver

import com.example.ydbinsertion.batchinsertion.api.processor.BatchInsertionByEntityProcessor
import com.example.ydbinsertion.logic.entity.BaseEntity
import java.sql.Connection
import kotlin.reflect.KClass

open class InsertByEntitySaver<E: BaseEntity>(
    private val processor: BatchInsertionByEntityProcessor,
    private val entityClass: KClass<E>,
    conn: Connection,
    batchSize: Int
) : AbstractBatchInsertionByEntitySaver<E>(conn, batchSize) {

    private val dataForInsert = mutableListOf<String>()

    override fun addDataForSave(entity: E) {
        dataForInsert.add(processor.getStringForInsert(entity))
        super.addDataForSave(entity)
    }

    override fun saveData() {
        processor.insertDataToDataBase(entityClass, dataForInsert, conn)
        dataForInsert.clear()
    }
}
