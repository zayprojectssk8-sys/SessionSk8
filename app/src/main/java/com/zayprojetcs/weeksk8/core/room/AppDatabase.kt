package com.zayprojetcs.weeksk8.core.room


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zayprojetcs.weeksk8.core.room.dao.AccelMinuteDao
import com.zayprojetcs.weeksk8.core.room.dao.ActivityMinuteDao
import com.zayprojetcs.weeksk8.core.room.dao.BaroMinuteDao
import com.zayprojetcs.weeksk8.core.room.dao.GpsMinuteDao
import com.zayprojetcs.weeksk8.core.room.dao.GyroMinuteDao
import com.zayprojetcs.weeksk8.core.room.dao.MagMinuteDao
import com.zayprojetcs.weeksk8.core.room.dao.SessionDao
import com.zayprojetcs.weeksk8.core.room.dao.RoundTrickAppDao
import com.zayprojetcs.weeksk8.core.room.dao.SensorMetricsDao
import com.zayprojetcs.weeksk8.core.room.dao.StepMinuteDao
import com.zayprojetcs.weeksk8.core.room.dao.TrickAppDao
import com.zayprojetcs.weeksk8.core.room.model.AccelMinuteEntity
import com.zayprojetcs.weeksk8.core.room.model.ActivityMinuteEntity
import com.zayprojetcs.weeksk8.core.room.model.BaroMinuteEntity
import com.zayprojetcs.weeksk8.core.room.model.GpsMinuteEntity
import com.zayprojetcs.weeksk8.core.room.model.GyroMinuteEntity
import com.zayprojetcs.weeksk8.core.room.model.MagMinuteEntity
import com.zayprojetcs.weeksk8.core.room.model.RoomRoundTrick
import com.zayprojetcs.weeksk8.core.room.model.RoomSession
import com.zayprojetcs.weeksk8.core.room.model.RoomTrick
import com.zayprojetcs.weeksk8.core.room.model.StepMinuteEntity


@Database(
    entities = [
        RoomSession::class,
        RoomRoundTrick::class,
        RoomTrick::class,
        AccelMinuteEntity::class,
        ActivityMinuteEntity::class,
        BaroMinuteEntity::class,
        GpsMinuteEntity::class,
        GyroMinuteEntity::class,
        MagMinuteEntity::class,
        StepMinuteEntity::class,
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
    abstract fun accelMinuteDao(): AccelMinuteDao
    abstract fun activityMinuteDao(): ActivityMinuteDao
    abstract fun baroMinuteDao(): BaroMinuteDao
    abstract fun gpsMinuteDao(): GpsMinuteDao
    abstract fun gyroMinuteDao(): GyroMinuteDao
    abstract fun magMinuteDao(): MagMinuteDao
    abstract fun stepMinuteDao(): StepMinuteDao
    abstract fun sensorMetricsDao(): SensorMetricsDao


    companion object {

        fun Context.roomSessionDb() = appDataBaseInstance.sessionAppDao()
        fun Context.roomRoundTrickDb() = appDataBaseInstance.roundTrickAppDao()
        fun Context.roomTrickDb() = appDataBaseInstance.trickAppDao()
        fun Context.roomAccelMinuteDb() = appDataBaseInstance.accelMinuteDao()
        fun Context.roomActivityMinuteDb() = appDataBaseInstance.activityMinuteDao()
        fun Context.roomBaroMinuteDb() = appDataBaseInstance.baroMinuteDao()
        fun Context.roomGpsMinuteDb() = appDataBaseInstance.gpsMinuteDao()
        fun Context.roomGyroMinuteDb() = appDataBaseInstance.gyroMinuteDao()
        fun Context.roomMagMinuteDb() = appDataBaseInstance.magMinuteDao()
        fun Context.roomStepMinuteDb() = appDataBaseInstance.stepMinuteDao()
        fun Context.roomSensorMetricsDb() = appDataBaseInstance.sensorMetricsDao()

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
