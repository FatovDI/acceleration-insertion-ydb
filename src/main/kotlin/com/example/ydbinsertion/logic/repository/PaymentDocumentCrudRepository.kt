package com.example.ydbinsertion.logic.repository

import com.example.ydbinsertion.logic.entity.PaymentDocumentEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentDocumentCrudRepository: CrudRepository<PaymentDocumentEntity, Long>