package com.example.ydbinsertion.batchinsertion.impl.processor

import com.example.ydbinsertion.batchinsertion.*
import com.example.ydbinsertion.batchinsertion.api.processor.BatchInsertionByEntityProcessor
import com.example.ydbinsertion.batchinsertion.api.processor.DataForUpdate
import com.example.ydbinsertion.logic.entity.BaseEntity
import com.example.ydbinsertion.logic.entity.short
import org.springframework.stereotype.Component
import java.io.BufferedWriter
import java.sql.Connection
import java.util.UUID
import kotlin.reflect.KClass

@Component
class YDBBatchInsertionByEntityProcessor(
): AbstractBatchInsertionProcessor(), BatchInsertionByEntityProcessor {

    private val delimiter = "|"
    private val nullValue = "NULL"

    override fun addDataForCreate(data: BaseEntity, writer: BufferedWriter) {
        val values = getStringDataFromEntity(data)
        writer.write(getStringForWrite(values, delimiter, nullValue))
        writer.newLine()
    }

    override fun getStringForUpdate(data: BaseEntity): String =
        getFieldsWithoutId(data::class)
            .joinToString(", ") { field ->
                field.trySetAccessible()
                "${getColumnName(field)} = ${toSqlLiteral(getDataFromEntityByField(data, field), field)}"
            }
            .let { "$it where id = ${toSqlLiteral(ensureId(data))}" }

    override fun getStringForInsert(data: BaseEntity) =
        getInsertValuesString(data)

    /**
     * save list data with insert method and prepared statement
     * @param clazz - entity class
     * @param data - list of string by columns
     * @param conn - DB connection
     */
    override fun insertDataToDataBasePreparedStatement(
        clazz: KClass<out BaseEntity>,
        data: List<List<Any?>>,
        conn: Connection
    ) {
        insertDataToDataBasePreparedStatementMultiRow(
            getTableName(clazz),
            getInsertColumnsByClass(clazz),
            withIds(clazz, data),
            conn,
            getInsertColumnTypesByClass(clazz)
        )
    }

    /**
     * save list data with insert method and prepared statement
     * @param clazz - entity class
     * @param data - list of string by columns
     * @param conn - DB connection
     */
    override fun insertDataToDataBasePreparedStatementBasic(
        clazz: KClass<out BaseEntity>,
        data: List<List<Any?>>,
        conn: Connection
    ) {
        insertDataToDataBasePreparedStatementBasic(
            getTableName(clazz),
            getInsertColumnsByClass(clazz),
            withIds(clazz, data),
            conn,
            getInsertColumnTypesByClass(clazz)
        )
    }


    override fun insertDataToDataBasePreparedStatementAndUnnest(
        clazz: KClass<out BaseEntity>,
        data: List<List<Any?>>,
        pgTypes: List<String>,
        conn: Connection
    ) {
        insertDataToDataBasePreparedStatementMultiRow(
            getTableName(clazz),
            getInsertColumnsByClass(clazz),
            withIds(clazz, data.map { it.map { value -> value as Any? } }),
            conn,
            getInsertColumnTypesByClass(clazz)
        )
    }

    private fun withIds(clazz: KClass<out BaseEntity>, data: List<List<Any?>>): List<List<Any?>> {
        val columnsSize = getInsertColumnsByClass(clazz).size
        return data.map { row ->
            if (row.size == columnsSize) row else row + UUID.randomUUID().short()
        }
    }

    /**
     * get pg types by columns
     * @param clazz - entity class
     * @param conn - DB connection
     * @return List<String> - list with pg type name
     */
    override fun getPgTypes(clazz: KClass<out BaseEntity>, conn: Connection): List<String> =
        List(getInsertColumnsByClass(clazz).size) { "String" }

    override fun updateDataToDataBasePreparedStatement(
        clazz: KClass<out BaseEntity>,
        data: List<List<Any?>>,
        conditionParams: List<String>,
        conn: Connection
    ) {
        updateDataToDataBasePreparedStatement(
            DataForUpdate(getTableName(clazz), getColumnsByClass(clazz), conditionParams, data),
            conn
        )
    }

}
