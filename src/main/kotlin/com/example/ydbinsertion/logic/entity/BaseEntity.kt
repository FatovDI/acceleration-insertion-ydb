package com.example.ydbinsertion.logic.entity

import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import java.io.Serializable

@MappedSuperclass
abstract class BaseEntity : Serializable {

    @Id
    @UuidShortId
    var id: String? = null

}
