package com.example.ydbinsertion.batchinsertion.impl.saver

import com.example.ydbinsertion.batchinsertion.api.saver.BatchInsertionSaver
import com.example.ydbinsertion.batchinsertion.utils.logger
import java.sql.Connection

abstract class AbstractBatchInsertionSaver(
    val conn: Connection
): BatchInsertionSaver {

    val log by logger()

    init {
        log.info("start save data")
        conn.autoCommit = false
    }

    override fun commit() {
        conn.commit()
    }

    override fun rollback() {
        conn.rollback()
    }

    override fun close() {
        conn.close()
        log.info("end save data")
    }
}