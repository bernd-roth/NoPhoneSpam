package at.bitfire.nophonespam.model

data class BlockedCall(
    val id: Long = 0,
    val matchedPattern: String = "",
    val incomingNumber: String? = null,
    val blockedAt: Long = 0
) {
    companion object {
        const val _TABLE = "blocked_calls"
        const val ID = "id"
        const val MATCHED_PATTERN = "matched_pattern"
        const val INCOMING_NUMBER = "incoming_number"
        const val BLOCKED_AT = "blocked_at"
    }
}
