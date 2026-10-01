package com.example.omninoteconnect.data.Note

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Notes::class], version = 2, exportSchema = false)
abstract class NoteDatabase : RoomDatabase(){

    abstract fun noteDao() : NoteDao

    companion object {
        private var instance: NoteDatabase? = null

        // Migrasi dari versi 1 ke 2: tambah kolom placeName dan isDone.
        // Dengan migrasi, catatan lama tidak ikut terhapus saat aplikasi di-update.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Notes ADD COLUMN placeName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE Notes ADD COLUMN isDone INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): NoteDatabase {
            return synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
