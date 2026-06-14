package io.nekohasekai.libbox

import libv2ray.Libv2ray
import libv2ray.V2RayPoint

/**
 * Service wrapper that interacts with the real sing-box compiled library via Go Bindings.
 */
class BoxService(
    private val configJson: String,
    private val platformInterface: PlatformInterface
) {
    private var v2rayPoint: V2RayPoint? = null

    fun start() {
        try {
            platformInterface.writeLog("sing-box-core: starting tunnel...")
            
            // Instantiating the real JNI callback interface
            val supportsSet = V2RayVPNServiceSupportsSetImpl(platformInterface)
            
            // Initialize native Go core runner
            val point = Libv2ray.newV2RayPoint(supportsSet, false)
            point.configureFileContent = configJson
            
            v2rayPoint = point
            
            // Run core loop in background thread since runLoop blocks execution
            Thread {
                try {
                    point.runLoop(true)
                } catch (e: Exception) {
                    platformInterface.writeLog("sing-box-core loop terminated: ${e.message}")
                }
            }.start()
            
            platformInterface.writeLog("sing-box-core: tunnel initialized and loop started.")
        } catch (e: Exception) {
            platformInterface.writeLog("sing-box-core failed to start: ${e.message}")
            throw e
        }
    }

    fun close() {
        try {
            v2rayPoint?.stopLoop()
        } catch (e: Exception) {
            platformInterface.writeLog("sing-box-core error stopping service: ${e.message}")
        } finally {
            v2rayPoint = null
            platformInterface.writeLog("sing-box-core: service stopped.")
        }
    }
}
