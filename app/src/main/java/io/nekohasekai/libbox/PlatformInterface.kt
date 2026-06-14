package io.nekohasekai.libbox

/**
 * Stub interface mimicking the sing-box-core library PlatformInterface.
 */
interface PlatformInterface {
    fun writeLog(message: String?)
    fun useService(): Boolean
    fun openTun(mtu: Int): Int
    fun protect(socket: Int): Boolean
    fun onServiceStopped()
}
