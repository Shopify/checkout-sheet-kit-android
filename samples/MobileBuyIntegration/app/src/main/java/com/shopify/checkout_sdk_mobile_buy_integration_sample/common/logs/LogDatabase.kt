package com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
     entities = [LogLine::class],
     version = 1,
)
@TypeConverters(Converters::class)
abstract class LogDatabase : RoomDatabase() {
     abstract fun logDao(): LogDao
}
