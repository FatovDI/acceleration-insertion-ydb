package com.example.ydbinsertion.logic.service

import com.example.ydbinsertion.batchinsertion.impl.factory.BatchInsertionByEntityFactory
import com.example.ydbinsertion.logic.entity.PaymentDocumentEntity
import org.springframework.stereotype.Component

@Component
class BatchInsertionPaymentDocumentByEntityFactory :
    BatchInsertionByEntityFactory<PaymentDocumentEntity>(PaymentDocumentEntity::class)