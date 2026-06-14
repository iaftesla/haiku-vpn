package io.nekohasekai.libbox

/**
 * Stub object mimicking the sing-box-core entry point Libbox.
 */
object Libbox {
    fun newService(configJson: String, platformInterface: PlatformInterface): BoxService {
        return BoxService(configJson, platformInterface)
    }
}
