package com.example.ydbinsertion.batchinsertion.impl

import com.example.ydbinsertion.batchinsertion.api.SqlHelper
import com.example.ydbinsertion.batchinsertion.getTableName
import com.example.ydbinsertion.logic.entity.BaseEntity
import org.springframework.stereotype.Component
import java.sql.ResultSet
import javax.sql.DataSource
import kotlin.reflect.KClass

@Component
class SqlHelper(
    private val dataSource: DataSource
): SqlHelper {

    override fun getIdListForUpdate(count: Int, clazz: KClass<out BaseEntity>): List<String> {
        return dataSource.connection.use { conn ->
            conn
                .createStatement()
                .use { stmnt ->
                    stmnt.executeQuery("SELECT id FROM ${getTableName(clazz)} LIMIT $count")
                        .toList { it.getString(1) }
                }
        }
    }

    override fun dropIndex(clazz: KClass<out BaseEntity>): String = ""


    override fun executeScript(script: String) {
        script.takeIf { it.isNotEmpty() }?.let {
            dataSource.connection.use { conn ->
                conn.createStatement().use { stmnt ->
                    stmnt.execute(it)
                }
            }
        }
    }

    private fun <K, V> ResultSet.toMap(mapBlock: (rs: ResultSet) -> Pair<K, V>): Map<K, V> {
        val map = mutableMapOf<K, V>()
        while (this.next()) {
            map.plusAssign(mapBlock(this))
        }
        return map
    }

    private fun <T> ResultSet.toList(mapBlock: (rs: ResultSet) -> T): List<T> {
        val list = mutableListOf<T>()
        while (this.next()) {
            list.add(mapBlock(this))
        }
        return list
    }
}