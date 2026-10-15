package at.bitfire.nophonespam

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallBlockingPolicyTest {
    private val incomingNumber = "+436641234567"

    @Test
    fun savedContactBypassesCountryRuleWithoutUpdatingBlockHistory() {
        assertFalse(CallBlockingPolicy.shouldBlock(
            incomingNumber, true, false, true,
            isContact = { it == incomingNumber },
            matchesBlacklist = { error("Saved contacts must not reach rule matching or logging") }
        ))
    }

    @Test
    fun unknownCallerIsBlockedWithoutMatchingRule() {
        assertTrue(CallBlockingPolicy.shouldBlock(
            incomingNumber, true, false, true, { false }, { false }
        ))
    }

    @Test
    fun unknownCallerStillMatchesExistingRule() {
        var checkedRule = false
        assertTrue(CallBlockingPolicy.shouldBlock(
            incomingNumber, true, false, true, { false }, {
                checkedRule = true
                true
            }
        ))
        assertTrue(checkedRule)
    }

    @Test
    fun blacklistAppliesWhenContactsOnlyModeIsDisabled() {
        assertTrue(CallBlockingPolicy.shouldBlock(
            incomingNumber, true, false, false,
            isContact = { error("Contacts lookup is unnecessary when the option is off") },
            matchesBlacklist = { it == incomingNumber }
        ))
    }

    @Test
    fun unmatchedCallerIsAllowedWhenContactsOnlyModeIsDisabled() {
        assertFalse(CallBlockingPolicy.shouldBlock(
            incomingNumber, true, false, false, { false }, { false }
        ))
    }

    @Test
    fun hiddenCallsFollowHiddenNumberSettingWithoutLookups() {
        for (number in listOf(null, "")) {
            for (blockHidden in listOf(false, true)) {
                val blocked = CallBlockingPolicy.shouldBlock(
                    number, true, blockHidden, true,
                    { error("Hidden caller has no contact number") },
                    { error("Hidden caller has no number to match") }
                )
                if (blockHidden) assertTrue(blocked) else assertFalse(blocked)
            }
        }
    }

    @Test
    fun disablingBlockingAllowsAllCallsWithoutLookups() {
        for (number in listOf(null, "", incomingNumber)) {
            assertFalse(CallBlockingPolicy.shouldBlock(
                number, false, true, true,
                { error("Blocking is disabled") },
                { error("Blocking is disabled") }
            ))
        }
    }
}
