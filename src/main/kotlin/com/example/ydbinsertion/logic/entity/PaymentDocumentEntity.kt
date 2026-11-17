package com.example.ydbinsertion.logic.entity

import java.math.BigDecimal
import java.time.LocalDate
import jakarta.persistence.*

@Entity
@Table(name = "payment_document")
class PaymentDocumentEntity(
    var orderDate: LocalDate? = null,
    var orderNumber: String? = null,
    @Column(precision = 22, scale = 9)
    var amount: BigDecimal? = null,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cur", referencedColumnName = "code")
    var cur: CurrencyEntity? = null,
    var expense: Boolean? = null,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    var account: AccountEntity? = null,
    var paymentPurpose: String? = null,
    @Column(name = "prop_10")
    var prop10: String? = null,
    @Column(name = "prop_15")
    var prop15: String? = null,
    @Column(name = "prop_20")
    var prop20: String? = null,
) : BaseEntity()
