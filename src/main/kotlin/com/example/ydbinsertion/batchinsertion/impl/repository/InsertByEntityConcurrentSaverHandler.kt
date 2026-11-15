package com.example.ydbinsertion.batchinsertion.impl.repository

import com.example.ydbinsertion.batchinsertion.api.processor.BatchInsertionByEntityProcessor
import com.example.ydbinsertion.batchinsertion.api.saver.BatchInsertionByEntitySaver
import com.example.ydbinsertion.batchinsertion.impl.saver.InsertByEntityConcurrentSaver
import com.example.ydbinsertion.logic.entity.BaseEntity
import java.util.concurrent.ExecutorService
import javax.sql.DataSource
import kotlin.reflect.KClass

class InsertByEntityConcurrentSaverHandler<E : BaseEntity>(
    private val processor: BatchInsertionByEntityProcessor,
    private val entityClass: KClass<E>,
    private val dataSource: DataSource,
    private val batchSize: Int,
    private val numberOfSavers: Int = 4,
    executorService: ExecutorService
): BatchInsertionByEntitySaver<E> {
    private var counterEntity = 0
    private var counterSaver = 0
    private val savers = (1..numberOfSavers)
        .map { InsertByEntityConcurrentSaver(processor, entityClass, dataSource.connection, batchSize, executorService) }

    override fun addDataForSave(entity: E) {

        val currSaver = savers[counterSaver % numberOfSavers]

        currSaver.addDataForSave(entity)

        counterEntity++
        counterEntity.takeIf { it % batchSize == 0 }?.let { counterSaver++ }
    }

    override fun saveData() {
        savers.forEach {
            it.saveData()
        }
    }

    override fun commit() {
        savers.forEach {
            it.commit()
        }
    }

    override fun rollback() {
        savers.forEach {
            it.rollback()
        }
    }

    override fun close() {
        savers.forEach {
            it.close()
        }
    }
}
