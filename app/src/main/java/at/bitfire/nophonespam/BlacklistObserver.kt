package at.bitfire.nophonespam

import kotlinx.coroutines.flow.MutableSharedFlow

object BlacklistObserver {
    val updates = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)

    fun notifyUpdated() {
        updates.tryEmit(Unit)
    }
}
