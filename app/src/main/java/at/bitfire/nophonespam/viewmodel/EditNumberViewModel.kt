package at.bitfire.nophonespam.viewmodel

import android.app.Application
import android.content.ContentValues
import android.database.DatabaseUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import at.bitfire.nophonespam.BlacklistObserver
import at.bitfire.nophonespam.BlockScheduler
import at.bitfire.nophonespam.model.BlockedCall
import at.bitfire.nophonespam.model.DbHelper
import at.bitfire.nophonespam.model.Number
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditNumberViewModel(application: Application) : AndroidViewModel(application) {

    private val _number = MutableStateFlow<Number?>(null)
    val number: StateFlow<Number?> = _number.asStateFlow()

    fun load(pattern: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val dbHelper = DbHelper(getApplication())
                try {
                    val db = dbHelper.readableDatabase
                    val c = db.query(
                        Number._TABLE, null,
                        "${Number.NUMBER}=?",
                        arrayOf(pattern),
                        null, null, null
                    )
                    try {
                        if (c.moveToNext()) {
                            val values = ContentValues()
                            DatabaseUtils.cursorRowToContentValues(c, values)
                            _number.value = Number.fromValues(values)
                        }
                    } finally {
                        c.close()
                    }
                } finally {
                    dbHelper.close()
                }
            }
        }
    }

    fun save(
        existingPattern: String?,
        name: String,
        numberDb: String,
        blockFrom: Long?,
        blockUntil: Long?,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val dbHelper = DbHelper(getApplication())
                try {
                    val db = dbHelper.writableDatabase
                    val values = ContentValues().apply {
                        put(Number.NAME, name)
                        put(Number.NUMBER, numberDb)
                        if (blockFrom != null) put(Number.BLOCK_FROM, blockFrom) else putNull(Number.BLOCK_FROM)
                        if (blockUntil != null) put(Number.BLOCK_UNTIL, blockUntil) else putNull(Number.BLOCK_UNTIL)
                    }
                    if (existingPattern != null) {
                        db.update(Number._TABLE, values, "${Number.NUMBER}=?", arrayOf(existingPattern))
                    } else {
                        db.insert(Number._TABLE, null, values)
                    }
                    BlacklistObserver.notifyUpdated()
                } finally {
                    dbHelper.close()
                }

                // Cancel any existing alarm for the old pattern
                val oldPattern = existingPattern ?: numberDb
                BlockScheduler.cancelExpiry(getApplication(), oldPattern)

                // Schedule expiry alarm if blockUntil is in the future
                val now = System.currentTimeMillis()
                if (blockUntil != null && blockUntil > now) {
                    BlockScheduler.scheduleExpiry(getApplication(), numberDb, blockUntil)
                }
            }
            onDone()
        }
    }

    fun delete(pattern: String, onDone: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val dbHelper = DbHelper(getApplication())
                try {
                    val db = dbHelper.writableDatabase
                    db.delete(Number._TABLE, "${Number.NUMBER}=?", arrayOf(pattern))
                    db.delete(BlockedCall._TABLE, "${BlockedCall.MATCHED_PATTERN}=?", arrayOf(pattern))
                    BlacklistObserver.notifyUpdated()
                } finally {
                    dbHelper.close()
                }
                BlockScheduler.cancelExpiry(getApplication(), pattern)
            }
            onDone()
        }
    }
}
