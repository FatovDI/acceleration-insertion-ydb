package com.example.ydbinsertion.logic.repository

import com.example.ydbinsertion.logic.entity.AccountEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AccountRepository : JpaRepository<AccountEntity, String> {
}
