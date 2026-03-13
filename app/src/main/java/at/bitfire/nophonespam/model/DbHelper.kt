package at.bitfire.nophonespam.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) : SQLiteOpenHelper(context, "database", null, DB_VERSION) {

    companion object {
        private const val DB_VERSION = 3
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE ${Number._TABLE}(" +
                "${Number.NUMBER} TEXT NOT NULL PRIMARY KEY," +
                "${Number.NAME} TEXT NULL," +
                "${Number.LAST_CALL} INTEGER NULL," +
                "${Number.TIMES_CALLED} INTEGER NOT NULL DEFAULT 0," +
                "${Number.BLOCK_FROM} INTEGER NULL," +
                "${Number.BLOCK_UNTIL} INTEGER NULL" +
            ")"
        )
        db.execSQL(
            "CREATE TABLE ${BlockedCall._TABLE}(" +
                "${BlockedCall.ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "${BlockedCall.MATCHED_PATTERN} TEXT NOT NULL," +
                "${BlockedCall.INCOMING_NUMBER} TEXT," +
                "${BlockedCall.BLOCKED_AT} INTEGER NOT NULL" +
            ")"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, from: Int, to: Int) {
        if (from < 2) {
            db.execSQL(
                "CREATE TABLE ${BlockedCall._TABLE}(" +
                    "${BlockedCall.ID} INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "${BlockedCall.MATCHED_PATTERN} TEXT NOT NULL," +
                    "${BlockedCall.INCOMING_NUMBER} TEXT," +
                    "${BlockedCall.BLOCKED_AT} INTEGER NOT NULL" +
                ")"
            )
        }
        if (from < 3) {
            db.execSQL("ALTER TABLE ${Number._TABLE} ADD COLUMN ${Number.BLOCK_FROM} INTEGER NULL")
            db.execSQL("ALTER TABLE ${Number._TABLE} ADD COLUMN ${Number.BLOCK_UNTIL} INTEGER NULL")
        }
    }
}
