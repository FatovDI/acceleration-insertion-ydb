package com.example.ydbinsertion.batchinsertion.impl.saver

import com.example.ydbinsertion.batchinsertion.api.processor.BatchInsertionByEntityProcessor
import com.example.ydbinsertion.batchinsertion.getDataFromEntity
import com.example.ydbinsertion.logic.entity.BaseEntity
import java.sql.Connection
import kotlin.reflect.KClass

open class InsertByEntityPSUnnestSaver<E: BaseEntity>(
    private val processor: BatchInsertionByEntityProcessor,
    private val entityClass: KClass<E>,
    conn: Connection,
    batchSize: Int
) : AbstractBatchInsertionByEntitySaver<E>(conn, batchSize) {

    private val dataForInsert = mutableListOf<List<Any?>>()
    private val pgTypes = processor.getPgTypes(entityClass, conn)

    override fun addDataForSave(entity: E) {
        dataForInsert.add(getDataFromEntity(entity))
        super.addDataForSave(entity)
    }

    override fun saveData() {
        processor.insertDataToDataBasePreparedStatementAndUnnest(entityClass, dataForInsert, pgTypes, conn)
        dataForInsert.clear()
    }
}
