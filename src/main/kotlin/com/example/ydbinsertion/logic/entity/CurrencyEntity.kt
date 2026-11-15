package com.example.ydbinsertion.logic.entity

import org.hibernate.annotations.NaturalId
import org.hibernate.annotations.NaturalIdCache
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "currency")
@NaturalIdCache
class CurrencyEntity(
    @NaturalId(mutable = true)
    var code: String? = null,
    var name: String? = null,
) : BaseEntity()
