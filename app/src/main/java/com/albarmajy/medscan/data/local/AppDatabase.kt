package com.albarmajy.medscan.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.albarmajy.medscan.data.local.converters.Converters
import com.albarmajy.medscan.data.local.dao.MedicationDao
import com.albarmajy.medscan.data.local.entities.DoseLogEntity
import com.albarmajy.medscan.data.local.entities.MedicationEntity
import com.albarmajy.medscan.data.local.entities.MedicationPlanEntity
import com.albarmajy.medscan.data.local.entities.MedicineReferenceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Database(entities = [MedicationEntity::class, DoseLogEntity::class, MedicationPlanEntity::class, MedicineReferenceEntity::class], version =1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medscan_db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                fillWithDemoData(context, INSTANCE)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

suspend fun fillWithDemoData(context: Context, database: AppDatabase?) {
    withContext(Dispatchers.IO) {
        val dao = database?.medicationDao() ?: return@withContext
        Log.d("Database", "Starting pre-population on Background Thread...")

        try {
            context.assets.open("drugs.csv").bufferedReader().use { reader ->
                val references = reader.lineSequence()
                    .drop(1)
                    .mapNotNull { line ->
                        val col = line.split(",")
                        if (col.size >= 6) {
                            MedicineReferenceEntity(
                                trade_name_en = col[1].trim(),
                                trade_name_ar = col[2].trim(),
                                active_ingredient = col[3].trim(),
                                form = col[4].trim(),
                                strength = col[5].trim()
                            )
                        } else null
                    }.toList()

                if (references.isNotEmpty()) {
                    dao.insertAllReferences(references)
                    Log.d("Database", "Success: Inserted ${references.size} items")
                }
            }
        } catch (e: Exception) {
            Log.e("Database", "Error: ${e.message}")
        }
    }
}