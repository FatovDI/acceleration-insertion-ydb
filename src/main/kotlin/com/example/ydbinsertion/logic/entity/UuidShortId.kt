package com.example.ydbinsertion.logic.entity

import org.hibernate.annotations.IdGeneratorType

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@IdGeneratorType(value = UUIDShortGenerator::class)
annotation class UuidShortId()
