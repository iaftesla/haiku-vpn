package io.nekohasekai.libbox

import libv2ray.V2RayVPNServiceSupportsSet

/**
 * Real JNI callback adapter bridging Go core tunnel requests to Android VpnService.
 */
class V2RayVPNServiceSupportsSetImpl(
    private val platformInterface: PlatformInterface
) : V2RayVPNServiceSupportsSet {

    override fun onEmitStatus(code: Long, message: String?): Long {
        platformInterface.writeLog(message ?: "")
        return 0
    }

    override fun prepare(): Long {
        return 0
    }

    override fun protect(socket: Long): Boolean {
        // Delegate to Android protect(int) JNI to prevent proxy loop routing issues
        return platformInterface.protect(socket.toInt())
    }

    override fun setup(parameters: String?): Long {
        // setup is called by the Go runtime when it is initializing the TUN interface.
        // It provides configuration parameters (like MTU, IP, etc.).
        // We can parse the MTU from parameters or default to 1400.
        var mtu = 1400
        if (!parameters.isNullOrEmpty()) {
            val parts = parameters.split(",")
            if (parts.isNotEmpty()) {
                mtu = parts[0].toIntOrNull() ?: 1400
            }
        }
        
        // Call openTun to establish the Android VpnService TUN interface and return the file descriptor
        val fd = platformInterface.openTun(mtu)
        return fd.toLong()
    }

    override fun shutdown(): Long {
        return 0
    }
}
