package com.example.ydbinsertion.logic.entity

import jakarta.persistence.*

@Entity
@Table(name = "account")
class AccountEntity(
    var number: String? = null,
    var name: String? = null,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cur", referencedColumnName = "code")
    var cur: CurrencyEntity? = null,
) : BaseEntity()
