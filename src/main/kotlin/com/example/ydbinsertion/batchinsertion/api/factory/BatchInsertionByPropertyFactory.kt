package com.example.ydbinsertion.batchinsertion.api.factory

import com.example.ydbinsertion.batchinsertion.api.processor.BatchInsertionByPropertyProcessor
import com.example.ydbinsertion.batchinsertion.api.saver.BatchInsertionByPropertySaver
import com.example.ydbinsertion.logic.entity.BaseEntity

/**
 * Interface should be realised for save data by KProperty via batch insertion.
 *
 * example:
 *      @Component
 *       class BatchInsertionPaymentDocumentByEntityFactory(
 *       processor: BatchInsertionByEntityProcessor,
 *       dataSource: DataSource
 *       ) : BatchInsertionByPropertyFactory<PaymentDocumentEntity>(PaymentDocumentEntity::class, processor, dataSource)
 *
 */
interface BatchInsertionByPropertyFactory<E: BaseEntity> {

    val processor: BatchInsertionByPropertyProcessor

    /**
     * get saver by enum
     * @param type - saver type. Can be COPY, INSERT, UPDATE
     */
    fun getSaver(type: SaverType): BatchInsertionByPropertySaver<E>

}