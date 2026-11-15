package com.example.ydbinsertion.batchinsertion.api.saver

/**
 * For save or update data
 */
interface BatchInsertionSaver: AutoCloseable {

    /**
     * commit data to DB
     */
    fun commit()

    /**
     * rollback
     */
    fun rollback()

}
