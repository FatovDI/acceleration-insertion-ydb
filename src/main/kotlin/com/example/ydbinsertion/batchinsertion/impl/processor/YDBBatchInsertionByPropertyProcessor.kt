package com.example.ydbinsertion.batchinsertion.impl.processor

import com.example.ydbinsertion.batchinsertion.*
import com.example.ydbinsertion.batchinsertion.api.processor.BatchInsertionByPropertyProcessor
import com.example.ydbinsertion.batchinsertion.api.processor.DataForUpdate
import com.example.ydbinsertion.logic.entity.BaseEntity
import org.springframework.stereotype.Component
import java.io.BufferedWriter
import java.sql.Connection
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

@Component
class YDBBatchInsertionByPropertyProcessor(
) : AbstractBatchInsertionProcessor(), BatchInsertionByPropertyProcessor {

    override fun addDataForCreate(
        data: Map<out KProperty1<out BaseEntity, *>, Any?>,
        writer: BufferedWriter,
        delimiter: String,
        nullValue: String
    ) {
        writer.write(getStringForWrite(data.values.map { it?.toString() }, delimiter, nullValue))
        writer.newLine()
    }

    override fun getStringForUpdate(
        data: Map<out KProperty1<out BaseEntity, *>, Any?>,
        id: Long,
        nullValue: String
    ) = getStringForInsert(data, nullValue).let { "($it) where id = '$id'" }

    override fun insertDataToDataBase(
        clazz: KClass<out BaseEntity>,
        columns: Set<KProperty1<out BaseEntity, *>>,
        data: List<String>,
        conn: Connection
    ) {
        insertDataToDataBase(getTableName(clazz), getColumnsString(columns), data, conn)
    }

    override fun insertDataToDataBaseMultiRow(
        clazz: KClass<out BaseEntity>,
        columns: Set<KProperty1<out BaseEntity, *>>,
        data: List<String>,
        conn: Connection
    ) {
        insertDataToDataBaseMultiRow(getTableName(clazz), getColumnsString(columns), data, conn)
    }

    override fun insertDataToDataBasePreparedStatement(
        clazz: KClass<out BaseEntity>,
        columns: Set<KProperty1<out BaseEntity, *>>,
        data: List<List<Any?>>,
        conn: Connection
    ) {
        insertDataToDataBasePreparedStatementMultiRow(getTableName(clazz), getColumns(columns), data, conn)
    }

    override fun insertDataToDataBasePreparedStatementBasic(
        clazz: KClass<out BaseEntity>,
        columns: Set<KProperty1<out BaseEntity, *>>,
        data: List<List<Any?>>,
        conn: Connection
    ) {
        insertDataToDataBasePreparedStatementBasic(getTableName(clazz), getColumns(columns), data, conn)
    }

    override fun insertDataToDataBasePreparedStatementAndUnnest(
        clazz: KClass<out BaseEntity>,
        columns: Set<KProperty1<out BaseEntity, *>>,
        data: List<List<*>>,
        pgTypes: List<String>,
        conn: Connection
    ) {
        return insertDataToDataBasePreparedStatementAndUnnest(getTableName(clazz), getColumns(columns), data, pgTypes, conn)
    }

    override fun getPgTypes(
        clazz: KClass<out BaseEntity>,
        columns: Set<KProperty1<out BaseEntity, *>>,
        conn: Connection
    ): List<String> {
        return getPgTypes(getTableName(clazz), getColumns(columns), conn)
    }

    override fun updateDataToDataBase(
        clazz: KClass<out BaseEntity>,
        columns: Set<KProperty1<out BaseEntity, *>>,
        data: List<String>,
        conn: Connection
    ) {
        updateDataToDataBase(getTableName(clazz), getColumnsString(columns), data, conn)
    }

    override fun updateDataToDataBasePreparedStatement(
        clazz: KClass<out BaseEntity>,
        columns: Set<KProperty1<out BaseEntity, *>>,
        data: Collection<Collection<Any?>>,
        conditionParams: Collection<String>,
        conn: Connection
    ): Int {
        return updateDataToDataBasePreparedStatement(
            DataForUpdate(getTableName(clazz), columns.map { getColumnName(it.javaField) }, conditionParams, data),
            conn
        )
    }

    override fun getStringForInsert(data: Map<out KProperty1<out BaseEntity, *>, Any?>, nullValue: String) =
        getStringForInsert(data.values.map { it?.toString() }, nullValue)

}
