package io.nekohasekai.libbox

import java.util.Timer
import java.util.TimerTask
import kotlin.random.Random

/**
 * Stub class mimicking the sing-box-core BoxService.
 * Simulates active connection logs and data transfer speeds for previewing.
 */
class BoxService(
    private val configJson: String,
    private val platformInterface: PlatformInterface
) {
    private var timer: Timer? = null

    fun start() {
        platformInterface.writeLog("sing-box-core stub: initialization success.")
        platformInterface.writeLog("sing-box-core stub: tunnel established on tun0.")
        
        // Start background speed simulation to show traffic updating in the UI
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    // Simulate speed counts between 10 KiB/s and 4.2 MiB/s
                    val dlVal = String.format("%.1f", Random.nextDouble(10.0, 1500.0))
                    val ulVal = String.format("%.1f", Random.nextDouble(5.0, 300.0))
                    
                    platformInterface.writeLog("outbound/proxy: $dlVal KiB/s download, $ulVal KiB/s upload")
                }
            }, 1000, 1500)
        }
    }

    fun close() {
        timer?.cancel()
        timer = null
        platformInterface.writeLog("sing-box-core stub: service connection closed.")
    }
}
