package com.example.ydbinsertion.batchinsertion.impl.saver

import com.example.ydbinsertion.batchinsertion.api.processor.BatchInsertionByPropertyProcessor
import com.example.ydbinsertion.logic.entity.BaseEntity
import java.sql.Connection
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

open class InsertByPropertyPSSaver<E: BaseEntity>(
    private val processor: BatchInsertionByPropertyProcessor,
    private val entityClass: KClass<E>,
    conn: Connection,
    batchSize: Int
) : AbstractBatchInsertionByPropertySaver<E>(conn, batchSize) {

    private val dataForInsert = mutableListOf<List<Any?>>()

    override fun addDataForSave(data: Map<out KProperty1<E, *>, Any?>) {
        dataForInsert.add(data.values.toList())
        super.addDataForSave(data)
    }

    override fun saveData() {
        super.saveData()
        processor.insertDataToDataBasePreparedStatementBasic(entityClass, columns!!, dataForInsert, conn)
        dataForInsert.clear()
    }
}
