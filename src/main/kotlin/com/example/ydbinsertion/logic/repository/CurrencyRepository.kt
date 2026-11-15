package com.example.ydbinsertion.logic.repository

import com.example.ydbinsertion.logic.entity.CurrencyEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CurrencyRepository : JpaRepository<CurrencyEntity, Long> {
}
