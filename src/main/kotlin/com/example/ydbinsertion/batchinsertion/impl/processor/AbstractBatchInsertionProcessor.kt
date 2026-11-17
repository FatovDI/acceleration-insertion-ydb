package com.example.ydbinsertion.batchinsertion.impl.processor

import com.example.ydbinsertion.batchinsertion.*
import com.example.ydbinsertion.batchinsertion.api.processor.DataForUpdate
import com.example.ydbinsertion.logic.entity.BaseEntity
import java.io.DataOutputStream
import java.lang.reflect.Field
import java.math.BigDecimal
import java.sql.Connection
import java.sql.Statement
import java.sql.Types
import java.time.LocalDate
import java.time.LocalDateTime
import jakarta.persistence.Column
import tech.ydb.table.values.DecimalType
import tech.ydb.table.values.PrimitiveType
import tech.ydb.table.values.Value
import kotlin.reflect.KClass

/**
 * Abstract class for save and update data to DB
 */
abstract class AbstractBatchInsertionProcessor {



    /**
     * save list data with basic insert method
     * @param clazz - entity class
     * @param data - list of string
     * @param conn - DB connection
     */
    fun insertDataToDataBase(clazz: KClass<out BaseEntity>, data: List<String>, conn: Connection) {
        insertDataToDataBase(getTableName(clazz), getInsertColumnsStringByClass(clazz), data, conn)
    }

    /**
     * save list data with insert method
     * @param clazz - entity class
     * @param data - list of string
     * @param conn - DB connection
     */
    fun insertDataToDataBaseMultiRow(clazz: KClass<out BaseEntity>, data: List<String>, conn: Connection) {
        insertDataToDataBaseMultiRow(getTableName(clazz), getInsertColumnsStringByClass(clazz), data, conn)
    }

    /**
     * save list data with update method
     * @param clazz - entity class
     * @param data - list of string
     * @param conn - DB connection
     */
    fun updateDataToDataBase(clazz: KClass<out BaseEntity>, data: List<String>, conn: Connection) {
        updateDataToDataBase(getTableName(clazz), getColumnsStringByClass(clazz), data, conn)
    }

    /**
     * get string for CSV file and copy method
     * @param data - list of string
     * @param delimiter - delimiter for separate data
     * @param nullValue - string to define null value
     * @return String - string for insert
     */
    fun getStringForWrite(data: Collection<String?>, delimiter: String, nullValue: String): String {

        return data.joinToString(delimiter) {
            it
                ?.replace("\\", "\\\\")
                ?.replace("\n", "\\n")
                ?.replace("\r", "\\r")
                ?.replace(delimiter, "\\$delimiter")
                ?: nullValue
        }
    }

    /**
     * get string for insert by entity
     * @param data - list of data from entity
     * @param nullValue - string to define null value
     * @return String - string for insert
     */
    fun getStringForInsert(data: Collection<String?>, nullValue: String): String {
        return data
            .joinToString(",") { js ->
                js
                    ?.replace("'", "''")
                    ?.let { "'$it'" }
                    ?: nullValue
            }
    }

    /**
     * Convert value to a YQL literal with explicit type where YDB requires it.
     */
    fun toSqlLiteral(value: Any?, field: Field? = null): String = when (value) {
        null -> "NULL"
        is Boolean -> value.toString()
        is BigDecimal -> {
            val column = field?.getAnnotation(Column::class.java)
            val precision = column?.precision?.takeIf { it > 0 } ?: value.precision()
            val scale = column?.scale?.takeIf { it >= 0 } ?: value.scale()
            "Decimal(\"${value.toPlainString()}\", $precision, $scale)"
        }
        is Number -> value.toString()
        is LocalDate -> "Date(\"$value\")"
        is LocalDateTime -> "DateTime(\"$value\")"
        is String -> when (field?.type) {
            LocalDate::class.java -> "Date(\"$value\")"
            LocalDateTime::class.java -> "DateTime(\"$value\")"
            BigDecimal::class.java -> {
                val column = field.getAnnotation(Column::class.java)
                val precision = column?.precision?.takeIf { it > 0 } ?: 22
                val scale = column?.scale?.takeIf { it >= 0 } ?: 9
                "Decimal(\"$value\", $precision, $scale)"
            }
            java.lang.Boolean::class.java, java.lang.Boolean.TYPE -> value
            java.lang.Integer::class.java, java.lang.Integer.TYPE,
            java.lang.Long::class.java, java.lang.Long.TYPE,
            java.lang.Double::class.java, java.lang.Double.TYPE,
            java.lang.Float::class.java, java.lang.Float.TYPE -> value
            else -> "\"${escapeString(value)}\""
        }
        else -> "\"${escapeString(value.toString())}\""
    }

    private fun escapeString(value: String) = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")

    /**
     * YDB JDBC can not infer a type for null values, so pass a typed empty optional.
     */
    fun typedNullValue(type: String): Value<*> {
        val ydbType = when {
            type == "Utf8" || type == "Text" -> PrimitiveType.Text
            type == "Bytes" -> PrimitiveType.Bytes
            type == "Bool" -> PrimitiveType.Bool
            type == "Date" -> PrimitiveType.Date
            type == "Datetime" -> PrimitiveType.Datetime
            type == "Timestamp" -> PrimitiveType.Timestamp
            type == "Int32" -> PrimitiveType.Int32
            type == "Int64" -> PrimitiveType.Int64
            type == "Double" -> PrimitiveType.Double
            type == "Float" -> PrimitiveType.Float
            type.startsWith("Decimal(") -> {
                val args = type.removePrefix("Decimal(").removeSuffix(")").split(",").map { it.trim() }
                DecimalType.of(args[0].toInt(), args[1].toInt())
            }
            else -> PrimitiveType.Text
        }
        return ydbType.makeOptional().emptyValue()
    }

    /**
     * get values string for insert by entity with generated id
     */
    fun getInsertValuesString(entity: BaseEntity): String =
        getInsertValuesFromEntity(entity).joinToString(",") { (field, value) ->
            toSqlLiteral(value, field)
        }


    /**
     * start save binary data for copy method
     * @param outputStream - data output stream with data for save
     */
    fun startSaveBinaryDataForCopyMethod(outputStream: DataOutputStream) {

        // 11 byte of start PGCOPY\n\377\r\n\0
        outputStream.writeBytes("PGCOPY\n")
        outputStream.write(0xFF)
        outputStream.writeBytes("\r\n")
        outputStream.write(byteArrayOf(0))

        // disable OID
        outputStream.writeInt(0)
        // length of addition header
        outputStream.writeInt(0)

    }

    /**
     * end save binary data for copy method
     * @param outputStream - data output stream with data for save
     */
    fun endSaveBinaryDataForCopyMethod(outputStream: DataOutputStream) {

        outputStream.writeShort(-1)
        outputStream.close()

    }


    /**
     * save list data with insert method
     * @param tableName - table name in DB
     * @param columns - string of column separated by comma delimiter
     * @param data - list of string
     * @param conn - DB connection
     */
    fun insertDataToDataBase(tableName: String, columns: String, data: List<String>, conn: Connection) {
        if (data.isEmpty()) return

        conn.createStatement().use { stmt ->
            data.map { s ->
                stmt.addBatch( "INSERT INTO $tableName ($columns) VALUES ($s);")
            }
            stmt.executeBatch()
        }
    }

    /**
     * save list data with insert method multi row
     * @param tableName - table name in DB
     * @param columns - string of column separated by comma delimiter
     * @param data - list of string
     * @param conn - DB connection
     */
    fun insertDataToDataBaseMultiRow(tableName: String, columns: String, data: List<String>, conn: Connection) {
        if (data.isEmpty()) return

        conn.createStatement().use { stmt ->
            stmt.executeUpdate(
                "INSERT INTO $tableName ($columns) VALUES ${
                    data
                        .mapIndexed { index, s -> "($s)${if (index == data.lastIndex) ";" else ","}" }
                        .joinToString("\n")
                }"
            )
        }
    }

    /**
     * save list data with insert method and prepared statement
     * @param tableName - table name in DB
     * @param columns - list of columns
     * @param data - list of data by columns
     * @param conn - DB connection
     */
    fun insertDataToDataBasePreparedStatementMultiRow(
        tableName: String,
        columns: List<String>,
        data: List<Collection<Any?>>,
        conn: Connection,
        columnTypes: List<String>? = null
    ) {
        if (data.isEmpty()) return

        val params = columns.mapIndexed { idx, _ ->
            columnTypes?.get(idx)?.let { "CAST(? AS $it)" } ?: "?"
        }.joinToString(", ")

        conn.prepareStatement(
            "INSERT INTO $tableName (${columns.joinToString(",")}) VALUES ${
                List(data.size) { index -> "(${params})${if (index == data.lastIndex) ";" else ","}" }.joinToString("\n")
            }"
        ).use { stmt ->
            var idx = 0
            data.forEach { str ->
                str.forEachIndexed { colIdx, col ->
                    idx++
                    val value = if (col == null) columnTypes?.get(colIdx)?.let { typedNullValue(it) } else col
                    stmt.setObject(idx, value, Types.OTHER)
                }
            }
            stmt.executeUpdate()
        }
    }

    /**
     * save list data with insert method and basic prepared statement
     * @param tableName - table name in DB
     * @param columns - list of columns
     * @param data - list of data by columns
     * @param conn - DB connection
     */
    fun insertDataToDataBasePreparedStatementBasic(
        tableName: String,
        columns: List<String>,
        data: List<Collection<Any?>>,
        conn: Connection,
        columnTypes: List<String>? = null
    ) {
        if (data.isEmpty()) return

        val params = columns.mapIndexed { idx, _ ->
            columnTypes?.get(idx)?.let { "CAST(? AS $it)" } ?: "?"
        }.joinToString(", ")

        conn.prepareStatement(
            "INSERT INTO $tableName (${columns.joinToString(",")}) VALUES ($params);"
        ).use { stmt ->
            data.forEach { str ->
                str.forEachIndexed { idx, col ->
                    val value = if (col == null) columnTypes?.get(idx)?.let { typedNullValue(it) } else col
                    stmt.setObject(idx + 1, value, Types.OTHER)
                }
                stmt.addBatch()
            }
            stmt.executeBatch()
        }
    }

    /**
     * save list data with insert method and prepared statement and select data by unnest
     * @param tableName - table name in DB
     * @param columns - list of columns
     * @param data - list of data by columns
     * @param pgTypes - list of pg types
     * @param conn - DB connection
     */
    fun insertDataToDataBasePreparedStatementAndUnnest(
        tableName: String,
        columns: List<String>,
        data: List<List<*>>,
        pgTypes: List<String>,
        conn: Connection
    ) {

        conn.prepareStatement(
            "INSERT INTO $tableName (${columns.joinToString(",")}) SELECT * FROM unnest(${columns.joinToString(", ") { "?" }})"
        ).use { stmt ->

            var idx = 0
            columns.forEachIndexed { idxCol, s ->
                val arr = data.map { str ->
                    str[idxCol]
                }.toTypedArray()
                idx++
                stmt.setArray(idx, stmt.connection.createArrayOf(pgTypes[idxCol], arr))
            }
            stmt.executeUpdate()
        }

    }

    /**
     * get pg types by columns
     * @param tableName - table name in DB
     * @param columns - list of columns
     * @param conn - DB connection
     * @return List<String> - list with pg type name
     */
    fun getPgTypes(tableName: String, columns: List<String>, conn: Connection): List<String>  {

        return conn.prepareStatement(
            "INSERT INTO $tableName (${columns.joinToString(",")}) VALUES (${columns.joinToString(", ") { "?" }})"
        ).use { stmt ->
            List(columns.size) { idx -> stmt.parameterMetaData.getParameterTypeName(idx + 1) }
        }

    }

    /**
     * save list data with update method
     * @param tableName - table name in DB
     * @param columns - string of column separated by comma delimiter
     * @param data - list of string
     * @param conn - DB connection
     */
    fun updateDataToDataBase(tableName: String, columns: String, data: List<String>, conn: Connection) {
        if (data.isEmpty()) return

        conn.createStatement().use { stmnt ->
            stmnt.executeUpdate(
                data.joinToString("\n") { s ->
                    "UPDATE $tableName SET $s;"
                }
            )
        }
    }

    /**
     * save list data with update method
     * @param dataForUpdate - data class with data for update
     * @param conn - DB connection
     */
    fun updateDataToDataBasePreparedStatement(dataForUpdate: DataForUpdate, conn: Connection): Int {
        if (dataForUpdate.data.isEmpty()) return 0

        return conn.prepareStatement(
            "UPDATE ${dataForUpdate.tableName} SET ${dataForUpdate.columns.joinToString(", ") { "$it=?" }} where ${dataForUpdate.conditions.joinToString { "$it=? " }}"
        ).use { stmt ->
            dataForUpdate.data.forEach { str ->
                str.forEachIndexed { idx, col ->
                    stmt.setObject(idx + 1, col, Types.OTHER)
                }
                stmt.addBatch()
            }
            val results = stmt.executeBatch()
            if (results.any { it == Statement.SUCCESS_NO_INFO }) {
                dataForUpdate.data.size
            } else {
                results.sumOf { it }
            }
        }

    }
}
