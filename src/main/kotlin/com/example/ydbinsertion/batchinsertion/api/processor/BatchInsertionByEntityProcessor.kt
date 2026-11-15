package com.example.ydbinsertion.batchinsertion.api.processor

import com.example.ydbinsertion.logic.entity.BaseEntity
import java.io.*
import java.sql.Connection
import kotlin.reflect.KClass

/**
 * For save or update entity or Map<out KProperty1<out BaseEntity, *>, String?>
 */
interface BatchInsertionByEntityProcessor{
    /**
     * add data for create by entity via file
     * @param data - entity
     * @param writer - BufferedWriter for write entity to file
     */
    fun addDataForCreate(data: BaseEntity, writer: BufferedWriter)

    /**
     * get string for update by entity
     * @param data - entity
     * @return String - string for update
     */
    fun getStringForUpdate(data: BaseEntity): String

    /**
     * get string for insert by entity
     * @param data - entity
     * @return String - string for insert
     */
    fun getStringForInsert(data: BaseEntity): String

    /**
     * save list data with insert method
     * @param clazz - entity class
     * @param data - list of string
     * @param conn - DB connection
     */
    fun insertDataToDataBase(clazz: KClass<out BaseEntity>, data: List<String>, conn: Connection)

    /**
     * save list data with insert method multi row
     * @param clazz - entity class
     * @param data - list of string
     * @param conn - DB connection
     */
    fun insertDataToDataBaseMultiRow(clazz: KClass<out BaseEntity>, data: List<String>, conn: Connection)

    /**
     * save list data with insert method and prepared statement
     * @param clazz - entity class
     * @param data - list of string by columns
     * @param conn - DB connection
     */
    fun insertDataToDataBasePreparedStatement(clazz: KClass<out BaseEntity>, data: List<List<Any?>>, conn: Connection)

    /**
     * save list data with insert method and prepared statement basic
     * @param clazz - entity class
     * @param data - list of string by columns
     * @param conn - DB connection
     */
    fun insertDataToDataBasePreparedStatementBasic(clazz: KClass<out BaseEntity>, data: List<List<Any?>>, conn: Connection)

    /**
     * save list data with insert method and prepared statement and select data by unnest
     * @param clazz - entity class
     * @param data - list of string by columns
     * @param pgTypes - list of pg types
     * @param conn - DB connection
     */
    fun insertDataToDataBasePreparedStatementAndUnnest(
        clazz: KClass<out BaseEntity>,
        data: List<List<*>>,
        pgTypes: List<String>,
        conn: Connection
    )

    /**
     * get pg types by columns
     * @param clazz - entity class
     * @param conn - DB connection
     * @return List<String> - list with pg type name
     */
    fun getPgTypes(clazz: KClass<out BaseEntity>, conn: Connection): List<String>

    /**
     * save list data with update method
     * @param clazz - entity class
     * @param data - list of string
     * @param conn - DB connection
     */
    fun updateDataToDataBase(clazz: KClass<out BaseEntity>, data: List<String>, conn: Connection)

    /**
     * save list data with update method and prepared statement
     * @param clazz - entity class
     * @param data - list with batched data
     * @param conditionParams - list with names of parameter
     * @param conn - DB connection
     */
    fun updateDataToDataBasePreparedStatement(
        clazz: KClass<out BaseEntity>,
        data: List<List<Any?>>,
        conditionParams: List<String>,
        conn: Connection
    )

}
