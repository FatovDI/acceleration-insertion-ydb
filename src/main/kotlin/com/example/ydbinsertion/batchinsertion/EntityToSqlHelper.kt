package com.example.ydbinsertion.batchinsertion

import com.example.ydbinsertion.batchinsertion.exception.BatchInsertionException
import com.example.ydbinsertion.batchinsertion.utils.toSnakeCase
import com.example.ydbinsertion.logic.entity.BaseEntity
import com.example.ydbinsertion.logic.entity.short
import java.lang.reflect.Field
import java.util.UUID
import jakarta.persistence.Column
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.jvmName

fun ensureId(entity: BaseEntity): String =
    entity.id ?: UUID.randomUUID().short().also { entity.id = it }

fun getStringDataFromEntity(entity: BaseEntity) =
    getDataFromEntity(entity).map { it?.toString() }

fun getDataFromEntity(entity: BaseEntity) =
    getFieldsWithoutId(entity::class).map { field ->
        field.trySetAccessible()
        getDataFromEntityByField(entity, field)
    }

fun getDataFromEntityForInsert(entity: BaseEntity) =
    getDataFromEntity(entity) + ensureId(entity)

fun getDataFromEntityByField(entity: BaseEntity, field: Field) =
    when (val obj = field.get(entity)) {
        null -> null
        is BaseEntity -> {
            field.annotations
                .filterIsInstance<JoinColumn>()
                .firstOrNull()
                ?.referencedColumnName
                ?.takeIf { it.isNotEmpty() }
                ?.let { obj.javaClass.getDeclaredField(it) }
                ?.apply { trySetAccessible() }
                ?.get(obj)
                ?: obj.id
        }
        else -> obj
    }

fun getInsertValuesFromEntity(entity: BaseEntity): List<Pair<Field, Any?>> =
    getFieldsForInsert(entity::class).map { field ->
        field.trySetAccessible()
        val value = if (field.name == "id") ensureId(entity) else getDataFromEntityByField(entity, field)
        field to value
    }

fun getFieldsForInsert(clazz: KClass<out BaseEntity>): List<Field> =
    getFieldsWithoutId(clazz) + getFields(clazz).first { it.name == "id" }

fun getTableName(clazz: KClass<*>): String {
    val columnAnnotation = clazz.annotations.find { it.annotationClass == Table::class } as Table?
    return columnAnnotation?.name
        ?: clazz.simpleName?.toSnakeCase()
        ?: throw BatchInsertionException(
            "Can not define table name by class: ${clazz.jvmName}"
        )
}

fun getColumnsStringByClass(clazz: KClass<out BaseEntity>) =
    getFieldsWithoutId(clazz).joinToString(",") { getColumnName(it) }

fun getColumnsByClass(clazz: KClass<out BaseEntity>) =
    getFieldsWithoutId(clazz).map { getColumnName(it) }

fun getInsertColumnsStringByClass(clazz: KClass<out BaseEntity>) =
    getFieldsForInsert(clazz).joinToString(",") { getColumnName(it) }

fun getInsertColumnsByClass(clazz: KClass<out BaseEntity>) =
    getFieldsForInsert(clazz).map { getColumnName(it) }

fun getInsertColumnTypesByClass(clazz: KClass<out BaseEntity>) =
    getFieldsForInsert(clazz).map { getColumnYqlType(it) }

fun getColumnYqlType(field: Field?): String = when (field?.type) {
    java.lang.String::class.java -> "Utf8"
    java.math.BigDecimal::class.java -> {
        val column = field.getAnnotation(Column::class.java)
        val precision = column?.precision?.takeIf { it > 0 } ?: 22
        val scale = column?.scale?.takeIf { it >= 0 } ?: 9
        "Decimal($precision, $scale)"
    }
    java.lang.Boolean::class.java, java.lang.Boolean.TYPE -> "Bool"
    java.time.LocalDate::class.java -> "Date"
    java.time.LocalDateTime::class.java -> "Timestamp"
    java.lang.Integer::class.java, java.lang.Integer.TYPE -> "Int32"
    java.lang.Long::class.java, java.lang.Long.TYPE -> "Int64"
    java.lang.Double::class.java, java.lang.Double.TYPE -> "Double"
    java.lang.Float::class.java, java.lang.Float.TYPE -> "Float"
    else -> "Utf8"
}

fun getFieldsWithoutId(clazz: KClass<out BaseEntity>) =
    getFields(clazz).filter { it.name != "id" }

fun getFields(clazz: KClass<*>): List<Field> {
    val fields = mutableListOf<Field>()
    var currentClass: Class<*>? = clazz.java
    while (currentClass != null && currentClass != Any::class.java) {
        fields += currentClass.declaredFields
        currentClass = currentClass.superclass
    }
    return fields
}

fun getColumns(columns: Set<KProperty1<*, *>>) =
    columns.map { getColumnName(it.javaField) }

fun getColumnsString(columns: Set<KProperty1<*, *>>) =
    columns.joinToString(",") { getColumnName(it.javaField) }

fun getColumnName(field: Field?) =
    field?.annotations.let { annotations ->
        annotations
            ?.find { it.annotationClass == Column::class }
            ?.let { it as Column }
            ?.name
            ?.takeIf { it.isNotEmpty() }
            ?: annotations
                ?.find { it.annotationClass == JoinColumn::class }
                ?.let { it as JoinColumn }?.name
    }
        ?: field?.name?.toSnakeCase()
        ?: throw BatchInsertionException("Can not define column name by field")

