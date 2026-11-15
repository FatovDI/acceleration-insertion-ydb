package com.example.ydbinsertion.logic.service

import com.example.ydbinsertion.batchinsertion.api.processor.BatchInsertionByPropertyProcessor
import com.example.ydbinsertion.batchinsertion.impl.factory.BatchInsertionByPropertyFactory
import com.example.ydbinsertion.logic.entity.PaymentDocumentEntity
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
 class BatchInsertionPaymentDocumentByPropertyFactory(
    processor: BatchInsertionByPropertyProcessor,
    dataSource: DataSource,
 ) : BatchInsertionByPropertyFactory<PaymentDocumentEntity>(PaymentDocumentEntity::class, processor, dataSource)