package com.haiku.vpn.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.haiku.vpn.core.HaikuVpnService
import com.haiku.vpn.core.RealityConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.random.Random

/**
 * ViewModel governing State management for Haiku VPN UI.
 */
class VpnViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("haiku_prefs", Context.MODE_PRIVATE)

    // VPN core service states
    val vpnState = HaikuVpnService.vpnState
    val rxBytes = HaikuVpnService.rxBytes
    val txBytes = HaikuVpnService.txBytes

    // Connection Info States
    private val _connectionDuration = MutableStateFlow(0L)
    val connectionDuration = _connectionDuration.asStateFlow()

    private val _connectionIp = MutableStateFlow("—")
    val connectionIp = _connectionIp.asStateFlow()

    private val _activePing = MutableStateFlow<Long?>(null)
    val activePing = _activePing.asStateFlow()

    private var timerJob: Job? = null

    // Theme state
    private val _themeMode = MutableStateFlow("system")
    val themeMode = _themeMode.asStateFlow()

    // Node configuration states
    private val _nodes = MutableStateFlow<List<RealityConfig>>(emptyList())
    val nodes = _nodes.asStateFlow()

    private val _selectedNode = MutableStateFlow<RealityConfig?>(null)
    val selectedNode = _selectedNode.asStateFlow()

    private val _pings = MutableStateFlow<Map<String, Long>>(emptyMap())
    val pings = _pings.asStateFlow()

    // Preferences states
    private val _killSwitchEnabled = MutableStateFlow(false)
    val killSwitchEnabled = _killSwitchEnabled.asStateFlow()

    // Haiku poem state
    private val _activeHaiku = MutableStateFlow(getRandomHaiku(HaikuVpnService.VpnState.Disconnected))
    val activeHaiku = _activeHaiku.asStateFlow()

    private val _zenPhrase = MutableStateFlow("")
    val zenPhrase = _zenPhrase.asStateFlow()

    // Authorization States
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail = _userEmail.asStateFlow()

    private val _userPlan = MutableStateFlow("Haiku Free")
    val userPlan = _userPlan.asStateFlow()

    private val _userExpiry = MutableStateFlow("Бессрочно")
    val userExpiry = _userExpiry.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    init {
        loadSettings()
        loadNodes()
        loadProfile()

        // Observe VPN State changes to refresh the Haiku poem, start timer, and mock IP/Ping
        viewModelScope.launch {
            vpnState.collect { state ->
                _activeHaiku.value = getRandomHaiku(state)
                _zenPhrase.value = getRandomZenPhrase(state)
                handleStateChange(state)
            }
        }
    }

    private fun handleStateChange(state: HaikuVpnService.VpnState) {
        timerJob?.cancel()
        if (state is HaikuVpnService.VpnState.Connected) {
            // Start Connection Timer
            _connectionDuration.value = 0L
            timerJob = viewModelScope.launch {
                var seconds = 0L
                while (true) {
                    delay(1000)
                    seconds++
                    _connectionDuration.value = seconds
                }
            }
            // Mock connection details
            val node = _selectedNode.value
            _connectionIp.value = if (node != null) {
                val hash = Math.abs(node.address.hashCode()) % 254
                "185.220.101.$hash"
            } else {
                "185.220.101.42"
            }
            // Measure or mock ping
            viewModelScope.launch {
                val pingVal = node?.let { measurePing(it.address, it.port) } ?: -1L
                _activePing.value = if (pingVal > 0) pingVal else Random.nextLong(30, 75)
            }
        } else {
            _connectionDuration.value = 0L
            _connectionIp.value = "—"
            _activePing.value = null
        }
    }

    private fun loadSettings() {
        _killSwitchEnabled.value = sharedPrefs.getBoolean("kill_switch", false)
        _themeMode.value = sharedPrefs.getString("theme_mode", "system") ?: "system"
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        sharedPrefs.edit().putString("theme_mode", mode).apply()
    }

    fun toggleKillSwitch(enabled: Boolean) {
        _killSwitchEnabled.value = enabled
        sharedPrefs.edit().putBoolean("kill_switch", enabled).apply()
    }

    private fun loadNodes() {
        viewModelScope.launch(Dispatchers.IO) {
            val jsonStr = sharedPrefs.getString("saved_nodes", null)
            if (jsonStr != null) {
                try {
                    val parsed = Json.decodeFromString<List<RealityConfig>>(jsonStr)
                    _nodes.value = parsed
                    val activeName = sharedPrefs.getString("selected_node_name", null)
                    _selectedNode.value = parsed.find { it.name == activeName } ?: parsed.firstOrNull()
                } catch (e: Exception) {
                    loadFallbackNodes()
                }
            } else {
                loadFallbackNodes()
            }
        }
    }

    private fun loadFallbackNodes() {
        val fallback = listOf(
            RealityConfig(
                name = "Kyoto Zen",
                countryCode = "jp",
                address = "kyoto.haikuvpn.org",
                port = 443,
                publicKey = "p1Lh_O6cK_hE8-t6H0Q25ZvxD_dE7qf9Z1S2T3U4V5W",
                sni = "yahoo.co.jp",
                uuid = "b7a2d8e9-5f21-4f11-8280-928919013c72"
            )
        )
        _nodes.value = fallback
        _selectedNode.value = fallback.first()
        saveNodesList(fallback)
    }

    private fun saveNodesList(list: List<RealityConfig>) {
        sharedPrefs.edit().putString("saved_nodes", Json.encodeToString(list)).apply()
    }

    fun selectNode(config: RealityConfig) {
        _selectedNode.value = config
        sharedPrefs.edit().putString("selected_node_name", config.name).apply()
    }

    fun addNode(config: RealityConfig): Boolean {
        val current = _nodes.value.toMutableList()
        if (current.any { it.address == config.address && it.port == config.port }) {
            return false
        }
        current.add(config)
        _nodes.value = current
        saveNodesList(current)
        if (_selectedNode.value == null) {
            selectNode(config)
        }
        return true
    }

    fun deleteNode(config: RealityConfig) {
        val current = _nodes.value.toMutableList()
        current.remove(config)
        _nodes.value = current
        saveNodesList(current)
        if (_selectedNode.value?.name == config.name) {
            _selectedNode.value = current.firstOrNull()
        }
    }

    fun toggleConnection() {
        val context = getApplication<Application>()
        when (vpnState.value) {
            is HaikuVpnService.VpnState.Connected, is HaikuVpnService.VpnState.Connecting -> {
                val intent = Intent(context, HaikuVpnService::class.java).apply {
                    action = HaikuVpnService.ACTION_STOP
                }
                context.startService(intent)
            }
            is HaikuVpnService.VpnState.Disconnected, is HaikuVpnService.VpnState.Error -> {
                val node = _selectedNode.value ?: return
                val intent = Intent(context, HaikuVpnService::class.java).apply {
                    action = HaikuVpnService.ACTION_START
                    putExtra(HaikuVpnService.EXTRA_CONFIG_URI, generateUri(node))
                    putExtra(HaikuVpnService.EXTRA_KILL_SWITCH, _killSwitchEnabled.value)
                }
                context.startService(intent)
            }
        }
    }

    private fun generateUri(node: RealityConfig): String {
        val flowStr = if (node.flow.isNotEmpty()) "&flow=${node.flow}" else ""
        val sidStr = if (node.shortId.isNotEmpty()) "&sid=${node.shortId}" else ""
        return "vless://${node.uuid}@${node.address}:${node.port}?security=reality&pbk=${node.publicKey}${sidStr}&sni=${node.sni}&fp=${node.fingerprint}&type=${node.network}${flowStr}#${node.name}|${node.countryCode}"
    }

    fun testAllPings() {
        viewModelScope.launch {
            _nodes.value.forEach { node ->
                val delay = measurePing(node.address, node.port)
                val currentPings = _pings.value.toMutableMap()
                currentPings[node.name] = delay
                _pings.value = currentPings
            }
        }
    }

    private suspend fun measurePing(host: String, port: Int): Long = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), 1200)
            }
            System.currentTimeMillis() - start
        } catch (e: Exception) {
            -1L
        }
    }

    // Profile &     // Local persistent DB helpers for accounts
    private fun getRegisteredUsers(): MutableMap<String, String> {
        val jsonStr = sharedPrefs.getString("registered_users", null)
        if (jsonStr.isNullOrEmpty()) return mutableMapOf()
        return try {
            Json.decodeFromString<Map<String, String>>(jsonStr).toMutableMap()
        } catch (e: Exception) {
            mutableMapOf()
        }
    }

    private fun saveRegisteredUsers(users: Map<String, String>) {
        sharedPrefs.edit().putString("registered_users", Json.encodeToString(users)).apply()
    }

    private fun getUserPlans(): MutableMap<String, String> {
        val jsonStr = sharedPrefs.getString("user_plans", null)
        if (jsonStr.isNullOrEmpty()) return mutableMapOf()
        return try {
            Json.decodeFromString<Map<String, String>>(jsonStr).toMutableMap()
        } catch (e: Exception) {
            mutableMapOf()
        }
    }

    private fun saveUserPlans(plans: Map<String, String>) {
        sharedPrefs.edit().putString("user_plans", Json.encodeToString(plans)).apply()
    }

    private fun getUserExpiries(): MutableMap<String, String> {
        val jsonStr = sharedPrefs.getString("user_expiries", null)
        if (jsonStr.isNullOrEmpty()) return mutableMapOf()
        return try {
            Json.decodeFromString<Map<String, String>>(jsonStr).toMutableMap()
        } catch (e: Exception) {
            mutableMapOf()
        }
    }

    private fun saveUserExpiries(expiries: Map<String, String>) {
        sharedPrefs.edit().putString("user_expiries", Json.encodeToString(expiries)).apply()
    }

    private fun loadProfile() {
        _isLoggedIn.value = sharedPrefs.getBoolean("user_logged_in", false)
        _userEmail.value = sharedPrefs.getString("user_email", "") ?: ""
        _userPlan.value = sharedPrefs.getString("user_plan", "Haiku Free") ?: "Haiku Free"
        _userExpiry.value = sharedPrefs.getString("user_expiry", "Бессрочно") ?: "Бессрочно"
    }

    fun login(emailInput: String, passwordInput: String) {
        _authError.value = null
        val email = emailInput.trim().lowercase()
        val password = passwordInput.trim()
        
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
        if (email.isBlank() || !email.matches(emailRegex)) {
            _authError.value = "Неверный формат почты"
            return
        }
        if (password.length < 6) {
            _authError.value = "Пароль должен содержать от 6 символов"
            return
        }
        
        val users = getRegisteredUsers()
        // Auto-register demo account if it doesn't exist
        if (email == "demo@haikuvpn.org" && !users.containsKey(email)) {
            users[email] = "123456"
            saveRegisteredUsers(users)
        }
        
        if (!users.containsKey(email) || users[email] != password) {
            _authError.value = "Неверная почта или пароль"
            return
        }
        
        viewModelScope.launch {
            delay(1000) // Имитация задержки сети
            val plan = getUserPlans()[email] ?: "Haiku Free"
            val expiry = getUserExpiries()[email] ?: "Бессрочно"
            
            _isLoggedIn.value = true
            _userEmail.value = email
            _userPlan.value = plan
            _userExpiry.value = expiry
            saveProfileState(true, email, plan, expiry)
        }
    }

    fun register(emailInput: String, passwordInput: String) {
        _authError.value = null
        val email = emailInput.trim().lowercase()
        val password = passwordInput.trim()
        
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
        if (email.isBlank() || !email.matches(emailRegex)) {
            _authError.value = "Неверный формат почты"
            return
        }
        if (password.length < 6) {
            _authError.value = "Пароль должен содержать от 6 символов"
            return
        }
        
        val users = getRegisteredUsers()
        if (users.containsKey(email)) {
            _authError.value = "Пользователь с такой почтой уже зарегистрирован"
            return
        }
        
        viewModelScope.launch {
            delay(1000) // Имитация задержки сети
            users[email] = password
            saveRegisteredUsers(users)
            
            val plans = getUserPlans()
            plans[email] = "Haiku Free"
            saveUserPlans(plans)
            
            val expiries = getUserExpiries()
            expiries[email] = "Бессрочно"
            saveUserExpiries(expiries)
            
            _isLoggedIn.value = true
            _userEmail.value = email
            _userPlan.value = "Haiku Free"
            _userExpiry.value = "Бессрочно"
            saveProfileState(true, email, "Haiku Free", "Бессрочно")
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _userEmail.value = ""
        _userPlan.value = "Haiku Free"
        _userExpiry.value = "Бессрочно"
        saveProfileState(false, "", "Haiku Free", "Бессрочно")
    }

    fun upgradeToPremium() {
        val email = _userEmail.value.lowercase()
        if (email.isEmpty()) return
        
        val plans = getUserPlans()
        plans[email] = "Haiku Premium"
        saveUserPlans(plans)
        
        val expiries = getUserExpiries()
        expiries[email] = "14.06.2027"
        saveUserExpiries(expiries)
        
        _userPlan.value = "Haiku Premium"
        _userExpiry.value = "14.06.2027"
        saveProfileState(true, _userEmail.value, "Haiku Premium", "14.06.2027")
    }

    fun cancelPremium() {
        val email = _userEmail.value.lowercase()
        if (email.isEmpty()) return
        
        val plans = getUserPlans()
        plans[email] = "Haiku Free"
        saveUserPlans(plans)
        
        val expiries = getUserExpiries()
        expiries[email] = "Бессрочно"
        saveUserExpiries(expiries)
        
        _userPlan.value = "Haiku Free"
        _userExpiry.value = "Бессрочно"
        saveProfileState(true, _userEmail.value, "Haiku Free", "Бессрочно")
    }

    private fun saveProfileState(loggedIn: Boolean, email: String, plan: String, expiry: String) {
        sharedPrefs.edit().apply {
            putBoolean("user_logged_in", loggedIn)
            putString("user_email", email)
            putString("user_plan", plan)
            putString("user_expiry", expiry)
        }.apply()
    }

    // Japanese Wabi-Sabi Haiku Poems
    private fun getRandomHaiku(state: HaikuVpnService.VpnState): List<String> {
        val disconnectedHaikus = listOf(
            listOf("Тихий осенний ветер,", "Тропа сквозь пустой лес,", "Ожидание первого шага."),
            listOf("Тихая вода отражает,", "Облако проходит в молчании,", "Не оставляя следа."),
            listOf("Отдыхая на камне,", "Бабочка складывает крылья,", "Дорога безмолвна."),
            listOf("Чистый белый холст,", "Кисть замерла над ним,", "Ждет порыва ветра."),
            // Anime Easter Eggs (Evangelion & Naruto)
            listOf("В стальном доспехе", "Плачет дитя среди руин —", "Не убегай прочь."),
            listOf("Оранжевый лист", "Вьется в вихре урагана,", "Путь ниндзя предрешен.")
        )

        val connectingHaikus = listOf(
            listOf("Туман над холмом,", "Ветер начинает шептать,", "Ища открытые небеса."),
            listOf("Первый вдох бриза,", "Собирает опавшие листья,", "Чтобы начать путешествие."),
            listOf("Капли чернил в воде,", "Рисуют новые тропы,", "Путь открывается.")
        )

        val connectedHaikus = listOf(
            listOf("Свободный вольный ветер,", "Над вершинами гор,", "Безграничный путь впереди."),
            listOf("Туман рассеивается,", "Золотой свет льется сквозь сосны,", "У неба нет врат."),
            listOf("Свободный и чистый,", "Как поток, бегущий по камням,", "Тени больше нет."),
            listOf("Птица летит ввысь,", "Оставляя клетку позади,", "Простор бесконечен."),
            // Anime Easter Eggs (Spirited Away, Frieren, Fullmetal Alchemist)
            listOf("Забыто имя,", "Но в небесах летит дракон —", "Река помнит все."),
            listOf("Свет далеких звезд,", "Эльф считает капли лет,", "Память греет путь."),
            listOf("Красный плащ в пыли,", "Ищет брата сквозь века,", "Алхимии закон.")
        )

        val errorHaikus = listOf(
            listOf("Буря закрыла луну,", "Ветви гнутся в темноте,", "Замри и дыши снова.")
        )

        return when (state) {
            is HaikuVpnService.VpnState.Disconnected -> disconnectedHaikus[Random.nextInt(disconnectedHaikus.size)]
            is HaikuVpnService.VpnState.Connecting -> connectingHaikus[Random.nextInt(connectingHaikus.size)]
            is HaikuVpnService.VpnState.Connected -> connectedHaikus[Random.nextInt(connectedHaikus.size)]
            is HaikuVpnService.VpnState.Error -> errorHaikus[0]
        }
    }

    private fun getRandomZenPhrase(state: HaikuVpnService.VpnState): String {
        return when (state) {
            is HaikuVpnService.VpnState.Disconnected -> listOf(
                "Время созерцания",
                "Внутренний покой",
                "Путь начинается здесь",
                "Не убегай",
                "Мой путь ниндзя"
            ).random()
            is HaikuVpnService.VpnState.Connecting -> listOf(
                "Сбор ветра...",
                "Поиск тропы...",
                "Рассеивание тумана..."
            ).random()
            is HaikuVpnService.VpnState.Connected -> listOf(
                "Небо открыто",
                "Поток чист",
                "Бескрайний простор",
                "Ветер уносит имя",
                "Провожая в путь"
            ).random()
            is HaikuVpnService.VpnState.Error -> "Преграда на пути"
        }
    }
}

