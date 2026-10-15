package at.bitfire.nophonespam

/** Shared decision order for screening and read-only blocking checks. */
internal object CallBlockingPolicy {
    fun shouldBlock(
        incomingNumber: String?,
        blockingEnabled: Boolean,
        blockHiddenNumbers: Boolean,
        blockNonContacts: Boolean,
        isContact: (String) -> Boolean,
        matchesBlacklist: (String) -> Boolean
    ): Boolean {
        if (!blockingEnabled) return false
        if (incomingNumber.isNullOrEmpty()) return blockHiddenNumbers

        // Country-wide patterns can be created by previous non-contact calls.
        // In contacts-only mode, a saved contact must bypass those rules too.
        if (blockNonContacts && isContact(incomingNumber)) return false
        return matchesBlacklist(incomingNumber) || blockNonContacts
    }
}
