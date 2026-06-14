package com.haiku.vpn.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.haiku.vpn.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * Service executing the Android VpnService and wrapping the sing-box-core engine.
 */
class HaikuVpnService : VpnService() {

    private val binder = LocalBinder()
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    private var tunInterface: ParcelFileDescriptor? = null
    private var singBoxService: io.nekohasekai.libbox.BoxService? = null

    companion object {
        private const val TAG = "HaikuVpnService"
        private const val NOTIFICATION_ID = 1008
        private const val CHANNEL_ID = "haiku_vpn_channel"

        const val ACTION_START = "com.haiku.vpn.START"
        const val ACTION_STOP = "com.haiku.vpn.STOP"
        const val EXTRA_CONFIG_URI = "com.haiku.vpn.CONFIG_URI"
        const val EXTRA_KILL_SWITCH = "com.haiku.vpn.KILL_SWITCH"

        private val _vpnState = MutableStateFlow<VpnState>(VpnState.Disconnected)
        val vpnState = _vpnState.asStateFlow()

        private val _rxBytes = MutableStateFlow(0L)
        val rxBytes = _rxBytes.asStateFlow()

        private val _txBytes = MutableStateFlow(0L)
        val txBytes = _txBytes.asStateFlow()
    }

    sealed interface VpnState {
        object Disconnected : VpnState
        object Connecting : VpnState
        object Connected : VpnState
        data class Error(val message: String) : VpnState
    }

    inner class LocalBinder : Binder() {
        fun getService(): HaikuVpnService = this@HaikuVpnService
    }

    override fun onBind(intent: Intent?): IBinder? {
        if (intent != null && VpnService.SERVICE_INTERFACE == intent.action) {
            return super.onBind(intent)
        }
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val configUri = intent.getStringExtra(EXTRA_CONFIG_URI) ?: ""
                val killSwitch = intent.getBooleanExtra(EXTRA_KILL_SWITCH, false)
                startVpn(configUri, killSwitch)
            }
            ACTION_STOP -> {
                stopVpn()
            }
        }
        return START_NOT_STICKY
    }

    private fun startVpn(configUri: String, killSwitch: Boolean) {
        if (_vpnState.value == VpnState.Connected || _vpnState.value == VpnState.Connecting) {
            Log.d(TAG, "VPN already active or connecting")
            return
        }

        _vpnState.value = VpnState.Connecting
        startForeground(NOTIFICATION_ID, createNotification("Haiku ищет ветер..."))

        serviceScope.launch {
            try {
                if (configUri.isEmpty()) {
                    throw IllegalArgumentException("Пустая строка конфигурации")
                }

                // Parse configuration
                val config = RealityConfig.parseFromUri(configUri)
                
                // Generate sing-box core configuration JSON
                val singBoxJson = SingBoxConfigGenerator.generate(config)

                // Initialize the libbox platform interface
                val platformInterface = object : io.nekohasekai.libbox.PlatformInterface {
                    override fun writeLog(message: String?) {
                        Log.i("SingBoxCore", message ?: "")
                        // Optional: Parse bandwidth bytes from log if sing-box logs it,
                        // or query the BoxService network interfaces.
                        parseBandwidthFromLog(message)
                    }

                    override fun useService(): Boolean {
                        return true
                    }

                    override fun openTun(mtu: Int): Int {
                        return establishTun(mtu, config, killSwitch)
                    }

                    override fun protect(socket: Int): Boolean {
                        return this@HaikuVpnService.protect(socket)
                    }

                    override fun onServiceStopped() {
                        Log.i(TAG, "SingBox core stopped. Stopping VPN service.")
                        if (_vpnState.value == VpnState.Connected || _vpnState.value == VpnState.Connecting) {
                            _vpnState.value = VpnState.Error("Соединение прервано или заблокировано")
                        }
                        stopSelf()
                    }
                }

                // Instantiate and start sing-box service
                singBoxService = io.nekohasekai.libbox.Libbox.newService(singBoxJson, platformInterface)
                singBoxService?.start()

                _vpnState.value = VpnState.Connected
                updateNotification("Haiku: Поток течет спокойно.")
                Log.i(TAG, "VPN Connected and Tunnel established successfully.")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to start VPN", e)
                _vpnState.value = VpnState.Error(e.localizedMessage ?: "Неизвестная ошибка подключения")
                stopSelf()
            }
        }
    }

    private fun establishTun(mtu: Int, config: RealityConfig, killSwitch: Boolean): Int {
        val builder = Builder()
            .setMtu(mtu)
            .addAddress("172.19.0.1", 30) // Matching SingBox config Address
            .addRoute("0.0.0.0", 0)       // Route all IPv4
            .addDnsServer("8.8.8.8")
            .setSession("Haiku VPN: ${config.name}")
            .setConfigureIntent(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )

        if (killSwitch) {
            // Under Android's VpnService, setting a blocking route or blocking bypass locks the device
            // to only route packets via this TUN interface.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setMetered(false)
            }
        }

        val localTun = builder.establish() ?: throw IllegalStateException("VpnService builder returned null fd")
        tunInterface = localTun
        return localTun.fd
    }

    private fun stopVpn() {
        Log.i(TAG, "Stopping VPN...")
        serviceScope.launch {
            try {
                singBoxService?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing sing-box service", e)
            } finally {
                singBoxService = null
            }

            try {
                tunInterface?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing Tun interface", e)
            } finally {
                tunInterface = null
            }

            _vpnState.value = VpnState.Disconnected
            _rxBytes.value = 0L
            _txBytes.value = 0L
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun parseBandwidthFromLog(message: String?) {
        if (message == null) return
        // Optional parsing of core statistics. Sing-box logs outbound stats periodically, 
        // e.g. "outbound/proxy: 256.0 KiB/s download, 12.0 KiB/s upload"
        // Let's implement regex to fetch these for displaying speed counters in real time.
        if (message.contains("download") && message.contains("upload")) {
            // Regex parse
            val dlMatch = Regex("([0-9.]+)\\s+(KiB|MiB|B)/s\\s+download").find(message)
            val ulMatch = Regex("([0-9.]+)\\s+(KiB|MiB|B)/s\\s+upload").find(message)
            
            dlMatch?.let {
                val valStr = it.groupValues[1]
                val unit = it.groupValues[2]
                var bytes = valStr.toDoubleOrNull() ?: 0.0
                if (unit == "KiB") bytes *= 1024
                if (unit == "MiB") bytes *= 1024 * 1024
                _rxBytes.value = bytes.toLong()
            }
            ulMatch?.let {
                val valStr = it.groupValues[1]
                val unit = it.groupValues[2]
                var bytes = valStr.toDoubleOrNull() ?: 0.0
                if (unit == "KiB") bytes *= 1024
                if (unit == "MiB") bytes *= 1024 * 1024
                _txBytes.value = bytes.toLong()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Статус подключения Haiku",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Отображает состояние защищенного туннеля Haiku."
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        val disconnectIntent = Intent(this, HaikuVpnService::class.java).apply {
            action = ACTION_STOP
        }
        val disconnectPendingIntent = PendingIntent.getService(
            this, 1, disconnectIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this, 2, openIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Haiku VPN")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_share) // Simple default icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(openPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Выдохнуть (Отключить)",
                disconnectPendingIntent
            )
            .setColor(0x1C1C1C) // Sumizome Charcoal
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(contentText: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification(contentText))
    }

    override fun onDestroy() {
        stopVpn()
        serviceJob.cancel()
        super.onDestroy()
    }
}
