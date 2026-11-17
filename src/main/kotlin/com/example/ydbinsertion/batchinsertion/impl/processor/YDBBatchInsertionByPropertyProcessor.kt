package com.example.ydbinsertion.batchinsertion.impl.processor

import com.example.ydbinsertion.batchinsertion.*
import com.example.ydbinsertion.batchinsertion.api.processor.BatchInsertionByPropertyProcessor
import com.example.ydbinsertion.batchinsertion.api.processor.DataForUpdate
import com.example.ydbinsertion.logic.entity.BaseEntity
import com.example.ydbinsertion.logic.entity.short
import org.springframework.stereotype.Component
import java.io.BufferedWriter
import java.sql.Connection
import java.util.UUID
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
        id: String,
        nullValue: String
    ) = data.entries
        .filter { it.key.name != "id" }
        .joinToString(", ") { (property, value) ->
            "${getColumnName(property.javaField)} = ${toSqlLiteral(value, property.javaField)}"
        }
        .let { "$it where id = ${toSqlLiteral(id)}" }

    override fun insertDataToDataBase(
        clazz: KClass<out BaseEntity>,
        columns: Set<KProperty1<out BaseEntity, *>>,
        data: List<String>,
        conn: Connection
    ) {
        if (columns.containId()) {
            insertDataToDataBase(getTableName(clazz), getColumnsString(columns), data, conn)
        } else {
            insertDataToDataBase(
                getTableName(clazz),
                getColumnsString(columns) + ",id",
                data.map { "$it,${toSqlLiteral(nextId())}" },
                conn
            )
        }
    }

    override fun insertDataToDataBaseMultiRow(
        clazz: KClass<out BaseEntity>,
        columns: Set<KProperty1<out BaseEntity, *>>,
        data: List<String>,
        conn: Connection
    ) {
        if (columns.containId()) {
            insertDataToDataBaseMultiRow(getTableName(clazz), getColumnsString(columns), data, conn)
        } else {
            insertDataToDataBaseMultiRow(
                getTableName(clazz),
                getColumnsString(columns) + ",id",
                data.map { "$it,${toSqlLiteral(nextId())}" },
                conn
            )
        }
    }

    override fun insertDataToDataBasePreparedStatement(
        clazz: KClass<out BaseEntity>,
        columns: Set<KProperty1<out BaseEntity, *>>,
        data: List<List<Any?>>,
        conn: Connection
    ) {
        insertDataToDataBasePreparedStatementMultiRow(
            getTableName(clazz),
            insertColumns(columns),
            insertData(columns, data),
            conn,
            insertColumnTypes(columns)
        )
    }

    override fun insertDataToDataBasePreparedStatementBasic(
        clazz: KClass<out BaseEntity>,
        columns: Set<KProperty1<out BaseEntity, *>>,
        data: List<List<Any?>>,
        conn: Connection
    ) {
        insertDataToDataBasePreparedStatementBasic(
            getTableName(clazz),
            insertColumns(columns),
            insertData(columns, data),
            conn,
            insertColumnTypes(columns)
        )
    }

    override fun insertDataToDataBasePreparedStatementAndUnnest(
        clazz: KClass<out BaseEntity>,
        columns: Set<KProperty1<out BaseEntity, *>>,
        data: List<List<*>>,
        pgTypes: List<String>,
        conn: Connection
    ) {
        insertDataToDataBasePreparedStatementMultiRow(
            getTableName(clazz),
            insertColumns(columns),
            insertData(columns, data.map { row -> row.map { value -> value as Any? } }),
            conn,
            insertColumnTypes(columns)
        )
    }

    private fun Set<KProperty1<out BaseEntity, *>>.containId() = getColumns(this).any { it == "id" }

    private fun insertColumns(columns: Set<KProperty1<out BaseEntity, *>>) =
        if (columns.containId()) getColumns(columns) else getColumns(columns) + "id"

    private fun insertData(
        columns: Set<KProperty1<out BaseEntity, *>>,
        data: List<List<Any?>>
    ) = if (columns.containId()) data else data.map { it + nextId() }

    private fun insertColumnTypes(columns: Set<KProperty1<out BaseEntity, *>>) =
        columns.map { getColumnYqlType(it.javaField) }.let {
            if (columns.containId()) it else it + "Utf8"
        }

    private fun nextId() = UUID.randomUUID().short()

    override fun getPgTypes(
        clazz: KClass<out BaseEntity>,
        columns: Set<KProperty1<out BaseEntity, *>>,
        conn: Connection
    ): List<String> = List(insertColumns(columns).size) { "String" }

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
            DataForUpdate(
                getTableName(clazz),
                columns.map { getColumnName(it.javaField) }.filter { it !in conditionParams },
                conditionParams,
                data
            ),
            conn
        )
    }

    override fun getStringForInsert(data: Map<out KProperty1<out BaseEntity, *>, Any?>, nullValue: String) =
        data.entries.joinToString(",") { (property, value) ->
            toSqlLiteral(value, property.javaField)
        }

}
