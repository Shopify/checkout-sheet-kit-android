package com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LogDao {
    @Query("SELECT * FROM logline ORDER BY createdAt DESC LIMIT :n")
    fun getLast(n: Int): List<LogLine>

    @Insert
    fun insert(line: LogLine)

    @Query("DELETE FROM logLine")
    fun clear()
}
