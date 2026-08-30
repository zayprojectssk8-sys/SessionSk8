package com.zayprojetcs.weeksk8.core.room


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zayprojetcs.weeksk8.core.room.dao.SessionDao
import com.zayprojetcs.weeksk8.core.room.dao.RoundTrickAppDao
import com.zayprojetcs.weeksk8.core.room.dao.TrickAppDao
import com.zayprojetcs.weeksk8.core.room.model.RoomRoundTrick
import com.zayprojetcs.weeksk8.core.room.model.RoomSession
import com.zayprojetcs.weeksk8.core.room.model.RoomTrick


@Database(
    entities = [
        RoomSession::class,
        RoomRoundTrick::class,
        RoomTrick::class
    ],
    version = 1,
    exportSchema = true,
    /*autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]*/
)
//@TypeConverters(ConvertersGson::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sessionAppDao(): SessionDao
    abstract fun roundTrickAppDao(): RoundTrickAppDao
    abstract fun trickAppDao(): TrickAppDao


    companion object {

        fun Context.roomSessionDb() = appDataBaseInstance.sessionAppDao()
        fun Context.roomRoundTrickDb() = appDataBaseInstance.roundTrickAppDao()
        fun Context.roomTrickDb() = appDataBaseInstance.trickAppDao()

        @Volatile
        private var INSTANCE: AppDatabase? = null

        val Context.appDataBaseInstance get() = getInstance(this)


        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "WeekSk8Db.db"
                )

                val instance = builder.build()

                INSTANCE = instance

                instance
            }
        }


    }


}
