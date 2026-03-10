package at.bitfire.nophonespam.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import at.bitfire.nophonespam.model.BlockedCall
import at.bitfire.nophonespam.model.DbHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BlockedCallsViewModel(application: Application) : AndroidViewModel(application) {

    private val _calls = MutableStateFlow<List<BlockedCall>>(emptyList())
    val calls: StateFlow<List<BlockedCall>> = _calls.asStateFlow()

    fun load(pattern: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val dbHelper = DbHelper(getApplication())
                try {
                    val db = dbHelper.readableDatabase
                    val result = mutableListOf<BlockedCall>()
                    val c = db.query(
                        BlockedCall._TABLE, null,
                        "${BlockedCall.MATCHED_PATTERN}=?",
                        arrayOf(pattern),
                        null, null,
                        "${BlockedCall.BLOCKED_AT} DESC"
                    )
                    while (c.moveToNext()) {
                        val call = BlockedCall(
                            id = c.getLong(c.getColumnIndexOrThrow(BlockedCall.ID)),
                            matchedPattern = c.getString(c.getColumnIndexOrThrow(BlockedCall.MATCHED_PATTERN)),
                            incomingNumber = c.getString(c.getColumnIndexOrThrow(BlockedCall.INCOMING_NUMBER)),
                            blockedAt = c.getLong(c.getColumnIndexOrThrow(BlockedCall.BLOCKED_AT))
                        )
                        result.add(call)
                    }
                    c.close()
                    _calls.value = result
                } finally {
                    dbHelper.close()
                }
            }
        }
    }

    fun clearHistory(pattern: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val dbHelper = DbHelper(getApplication())
                try {
                    val db = dbHelper.writableDatabase
                    db.delete(BlockedCall._TABLE, "${BlockedCall.MATCHED_PATTERN}=?", arrayOf(pattern))
                } finally {
                    dbHelper.close()
                }
            }
            load(pattern)
        }
    }
}
