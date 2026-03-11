package at.bitfire.nophonespam.viewmodel

import android.app.Application
import android.database.DatabaseUtils
import android.content.ContentValues
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import at.bitfire.nophonespam.BlacklistObserver
import at.bitfire.nophonespam.Settings
import at.bitfire.nophonespam.model.DbHelper
import at.bitfire.nophonespam.model.Number
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BlacklistViewModel(application: Application) : AndroidViewModel(application) {

    private val _numbers = MutableStateFlow<List<Number>>(emptyList())
    val numbers: StateFlow<List<Number>> = _numbers.asStateFlow()

    private val settings = Settings(application)

    private val _blockHiddenNumbers = MutableStateFlow(settings.blockHiddenNumbers)
    val blockHiddenNumbers: StateFlow<Boolean> = _blockHiddenNumbers.asStateFlow()

    private val _showNotifications = MutableStateFlow(settings.showNotifications)
    val showNotifications: StateFlow<Boolean> = _showNotifications.asStateFlow()

    private val _blockNonContacts = MutableStateFlow(settings.blockNonContacts)
    val blockNonContacts: StateFlow<Boolean> = _blockNonContacts.asStateFlow()

    init {
        viewModelScope.launch {
            BlacklistObserver.updates
                .onStart { emit(Unit) }
                .collect {
                    loadNumbers()
                }
        }
    }

    private suspend fun loadNumbers() {
        withContext(Dispatchers.IO) {
            val dbHelper = DbHelper(getApplication())
            try {
                val db = dbHelper.readableDatabase
                val result = mutableListOf<Number>()
                val c = db.query(Number._TABLE, null, null, null, null, null, Number.NUMBER)
                while (c.moveToNext()) {
                    val values = ContentValues()
                    DatabaseUtils.cursorRowToContentValues(c, values)
                    result.add(Number.fromValues(values))
                }
                c.close()
                _numbers.value = result
            } finally {
                dbHelper.close()
            }
        }
    }

    fun toggleBlockHiddenNumbers() {
        val newValue = !settings.blockHiddenNumbers
        settings.blockHiddenNumbers = newValue
        _blockHiddenNumbers.value = newValue
    }

    fun toggleShowNotifications() {
        val newValue = !settings.showNotifications
        settings.showNotifications = newValue
        _showNotifications.value = newValue
    }

    fun toggleBlockNonContacts() {
        val newValue = !settings.blockNonContacts
        settings.blockNonContacts = newValue
        _blockNonContacts.value = newValue
    }
}
