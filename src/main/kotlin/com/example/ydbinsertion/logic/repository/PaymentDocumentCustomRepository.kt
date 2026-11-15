package com.example.ydbinsertion.logic.repository

import com.example.ydbinsertion.batchinsertion.impl.repository.InsertByEntitySaverBatchRepository
import com.example.ydbinsertion.logic.entity.PaymentDocumentEntity
import org.springframework.stereotype.Component

@Component
class PaymentDocumentCustomRepository(
    val repo: PaymentDocumentRepository,
) : PaymentDocumentRepository by repo, InsertByEntitySaverBatchRepository<PaymentDocumentEntity>(PaymentDocumentEntity::class)