package com.example.ydbinsertion.batchinsertion.impl.processor

import com.example.ydbinsertion.batchinsertion.*
import com.example.ydbinsertion.batchinsertion.api.processor.DataForUpdate
import com.example.ydbinsertion.logic.entity.BaseEntity
import java.io.DataOutputStream
import java.sql.Connection
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
        insertDataToDataBase(getTableName(clazz), getColumnsStringByClass(clazz), data, conn)
    }

    /**
     * save list data with insert method
     * @param clazz - entity class
     * @param data - list of string
     * @param conn - DB connection
     */
    fun insertDataToDataBaseMultiRow(clazz: KClass<out BaseEntity>, data: List<String>, conn: Connection) {
        insertDataToDataBaseMultiRow(getTableName(clazz), getColumnsStringByClass(clazz), data, conn)
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

        conn.createStatement().use { stmt ->
            stmt.executeLargeUpdate(
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
        conn: Connection
    ) {

        val params = columns.joinToString(", ") { "?" }

        conn.prepareStatement(
            "INSERT INTO $tableName (${columns.joinToString(",")}) VALUES ${
                List(data.size) { index -> "(${params})${if (index == data.lastIndex) ";" else ","}" }.joinToString("\n")
            }"
        ).use { stmt ->
            var idx = 0
            data.forEach { str ->
                str.forEach { col ->
                    idx++
                    stmt.setObject(idx, col)
                }
            }
            stmt.executeLargeUpdate()
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
        conn: Connection
    ) {

        val params = columns.joinToString(", ") { "?" }

        conn.prepareStatement(
            "INSERT INTO $tableName (${columns.joinToString(",")}) VALUES ($params);"
        ).use { stmt ->
            data.forEach { str ->
                str.forEachIndexed { idx, col ->
                    stmt.setObject(idx + 1, col)
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
            stmt.executeLargeUpdate()
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

        conn.createStatement().use { stmnt ->
            stmnt.executeLargeUpdate(
                data.joinToString("\n") { s ->
                    "UPDATE $tableName SET ($columns) = $s;"
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

        return conn.prepareStatement(
            "UPDATE ${dataForUpdate.tableName} SET ${dataForUpdate.columns.joinToString(", ") { "$it=?" }} where ${dataForUpdate.conditions.joinToString { "$it=? " }}"
        ).use { stmt ->
            dataForUpdate.data.forEach { str ->
                str.forEachIndexed { idx, col ->
                    stmt.setObject(idx + 1, col)
                }
                stmt.addBatch()
            }
            stmt.executeBatch()
        }.sumOf { it }

    }
}
