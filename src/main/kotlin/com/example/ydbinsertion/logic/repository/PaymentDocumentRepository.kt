package com.example.ydbinsertion.logic.repository

import com.example.ydbinsertion.logic.entity.PaymentDocumentEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface PaymentDocumentRepository: JpaRepository<PaymentDocumentEntity, String> {
    fun findAllByOrderNumberAndOrderDate(orderNumber: String, orderDate: LocalDate): List<PaymentDocumentEntity>
}