package at.bitfire.nophonespam.model

import android.content.ContentValues

data class Number(
    val number: String = "",
    val name: String? = null,
    val lastCall: Long? = null,
    val timesCalled: Int = 0,
    val blockFrom: Long? = null,
    val blockUntil: Long? = null
) {
    companion object {
        const val _TABLE = "numbers"
        const val NUMBER = "number"
        const val NAME = "name"
        const val LAST_CALL = "lastCall"
        const val TIMES_CALLED = "timesCalled"
        const val BLOCK_FROM = "block_from"
        const val BLOCK_UNTIL = "block_until"

        fun fromValues(values: ContentValues): Number = Number(
            number = values.getAsString(NUMBER) ?: "",
            name = values.getAsString(NAME),
            lastCall = values.getAsLong(LAST_CALL),
            timesCalled = values.getAsInteger(TIMES_CALLED) ?: 0,
            blockFrom = values.getAsLong(BLOCK_FROM),
            blockUntil = values.getAsLong(BLOCK_UNTIL)
        )

        fun wildcardsDbToView(number: String): String =
            number.replace('%', '*').replace('_', '#')

        fun wildcardsViewToDb(number: String): String =
            number.replace(Regex("[^+#*0-9]"), "").replace('*', '%').replace('#', '_')
    }
}
