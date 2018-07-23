package com.example.nazenani.blekotlin

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import org.jetbrains.anko.db.*

// インメモリでデータベースを使用する場合はDB名をnullにする（？）
class SqliteDatabaseOpenHelper (context: Context): ManagedSQLiteOpenHelper(context, "ble.db", null, 1) {
    val tableName: String = "bluetooth"

    companion object {
        //val tableName: String = "bluetooth"
        private var instance: SqliteDatabaseOpenHelper? = null;

        fun getInstance(context: Context): SqliteDatabaseOpenHelper {
            if (instance == null) {
                instance = SqliteDatabaseOpenHelper(context.applicationContext)
            }
            return instance!!
        }
    }


    override fun onCreate(db: SQLiteDatabase?) {
        db?.run {
            createTable(
                    tableName = tableName,
                    ifNotExists = true,
                    columns = *arrayOf(
                            "is_ibeacon" to INTEGER,
                            "uuid" to TEXT,
                            "address" to TEXT,
                            "rssi" to INTEGER,
                            "major" to TEXT,
                            "minor" to TEXT,
                            "distance" to REAL,
                            "date" to INTEGER
                    )
            )
        }
    }


    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //db?.execSQL("ALTER TABLE " + tableName + " ADD `test` INTEGER DEFAULT 0;")
        db?.dropTable(tableName, true)
    }

}

val Context.db: SqliteDatabaseOpenHelper get() = SqliteDatabaseOpenHelper.getInstance(applicationContext)
