package com.haiku.vpn.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import com.haiku.vpn.core.HaikuVpnService
import com.haiku.vpn.ui.VpnViewModel
import com.haiku.vpn.ui.components.EnsoConnectionButton
import com.haiku.vpn.ui.theme.MossGreen
import com.haiku.vpn.ui.theme.MutedText
import com.haiku.vpn.ui.theme.SakuraPink
import com.haiku.vpn.ui.theme.SumizomeCharcoal
import com.haiku.vpn.ui.theme.PoeticFontFamily
import com.haiku.vpn.ui.theme.TechnicalFontFamily
import java.util.Locale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    viewModel: VpnViewModel,
    onToggleConnection: () -> Unit,
    onNavigateToServers: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val vpnState by viewModel.vpnState.collectAsState()
    val activeHaiku by viewModel.activeHaiku.collectAsState()
    val selectedNode by viewModel.selectedNode.collectAsState()
    val rxBytes by viewModel.rxBytes.collectAsState()
    val txBytes by viewModel.txBytes.collectAsState()
    
    val duration by viewModel.connectionDuration.collectAsState()
    val ipAddress by viewModel.connectionIp.collectAsState()
    val activePing by viewModel.activePing.collectAsState()
    val zenPhrase by viewModel.zenPhrase.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scrollState = rememberScrollState()

    val lifecycleOwner = LocalLifecycleOwner.current
    var isLifecycleResumed by remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isLifecycleResumed = event == Lifecycle.Event.ON_RESUME
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val stateText = when (vpnState) {
        is HaikuVpnService.VpnState.Disconnected -> "Отключено"
        is HaikuVpnService.VpnState.Connecting -> "Связывание путей..."
        is HaikuVpnService.VpnState.Connected -> "Защищено"
        is HaikuVpnService.VpnState.Error -> "Путь прегражден"
    }
    val stateColor = when (vpnState) {
        is HaikuVpnService.VpnState.Connected -> MossGreen
        is HaikuVpnService.VpnState.Connecting -> SakuraPink
        is HaikuVpnService.VpnState.Error -> Color(0xFFC88A8A)
        else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
    }
    val statusGuidance = when (vpnState) {
        is HaikuVpnService.VpnState.Disconnected -> ""
        is HaikuVpnService.VpnState.Connecting -> "Связывание защищенных узлов..."
        is HaikuVpnService.VpnState.Connected -> "Ваш сетевой поток полностью защищен"
        is HaikuVpnService.VpnState.Error -> "Соединение прервано. Нажмите для повтора"
    }

    if (isLandscape) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. Top Bar Navigation ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Haiku VPN",
                    fontFamily = PoeticFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- 2. Horizontal split content ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Button & Status texts
                Column(
                    modifier = Modifier
                        .weight(0.44f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    EnsoConnectionButton(
                        vpnState = vpnState,
                        onClick = onToggleConnection,
                        buttonSize = 170.dp,
                        isAnimating = isLifecycleResumed
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text(
                        text = stateText.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        color = stateColor
                    )
                    if (statusGuidance.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = statusGuidance,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                        )
                    }
                    if (vpnState is HaikuVpnService.VpnState.Disconnected && zenPhrase.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = zenPhrase,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                // Right Column: Scrollable cards & server selector
                Column(
                    modifier = Modifier
                        .weight(0.56f)
                        .fillMaxHeight()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // A. Haiku Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(
                                targetState = activeHaiku,
                                transitionSpec = {
                                    fadeIn(tween(800)) togetherWith fadeOut(tween(600))
                                },
                                label = "HaikuAnim"
                            ) { lines ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    lines.forEach { line ->
                                        Text(
                                            text = line,
                                            style = MaterialTheme.typography.displayMedium.copy(fontSize = 18.sp, lineHeight = 28.sp),
                                            color = MaterialTheme.colorScheme.onBackground,
                                            textAlign = TextAlign.Center,
                                            fontStyle = FontStyle.Italic,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // B. Stats (when connected)
                    AnimatedVisibility(
                        visible = vpnState is HaikuVpnService.VpnState.Connected,
                        enter = fadeIn(animationSpec = tween(400)) + expandVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                        exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(
                            animationSpec = spring(
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Timing & Latency
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("ВРЕМЯ СЕССИИ", fontSize = 9.sp, letterSpacing = 1.sp, color = MutedText)
                                        Text(formatDuration(duration), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("ЗАДЕРЖКА", fontSize = 9.sp, letterSpacing = 1.sp, color = MutedText)
                                        Text("${activePing ?: "—"} ms", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MossGreen)
                                    }
                                }

                                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f))

                                // IP & Protocol
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("ВИРТУАЛЬНЫЙ IP", fontSize = 9.sp, letterSpacing = 1.sp, color = MutedText)
                                        Text(ipAddress, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("ПРОТОКОЛ", fontSize = 9.sp, letterSpacing = 1.sp, color = MutedText)
                                        Text("VLESS Reality", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                                    }
                                }

                                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f))

                                // Speed Counters
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("↓", fontSize = 16.sp, color = MossGreen, modifier = Modifier.padding(end = 4.dp))
                                        Text(formatBytes(rxBytes), fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("↑", fontSize = 16.sp, color = SakuraPink, modifier = Modifier.padding(end = 4.dp))
                                        Text(formatBytes(txBytes), fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
                                    }
                                }
                            }
                        }
                    }

                    // C. Server Selector Trigger Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(32.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { onNavigateToServers() }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CountryBadge(
                            countryCode = selectedNode?.countryCode ?: "vpn",
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Text(
                            text = selectedNode?.name ?: "Выбрать узел",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (vpnState is HaikuVpnService.VpnState.Connected) MossGreen
                                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
                                )
                        )
                    }
                }
            }
        }
    } else {
        // Portrait mode
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- 1. Redesigned Premium Top Bar Navigation ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Haiku VPN",
                    fontFamily = PoeticFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // --- 2. Central Scrollable Content (Safe from overflow) ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // A. The Haiku Poem (Elegant layout box)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = activeHaiku,
                            transitionSpec = {
                                fadeIn(tween(800)) togetherWith fadeOut(tween(600))
                            },
                            label = "HaikuAnim"
                        ) { lines ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                lines.forEach { line ->
                                    Text(
                                        text = line,
                                        style = MaterialTheme.typography.displayMedium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        textAlign = TextAlign.Center,
                                        fontStyle = FontStyle.Italic,
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // B. Enso Connection Button
                EnsoConnectionButton(
                    vpnState = vpnState,
                    onClick = onToggleConnection,
                    isAnimating = isLifecycleResumed
                )

                Spacer(modifier = Modifier.height(20.dp))

                // C. Connection State Text & Zen Phrase
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stateText.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        color = stateColor
                    )
                    if (statusGuidance.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = statusGuidance,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                        )
                    }
                    if (vpnState is HaikuVpnService.VpnState.Disconnected && zenPhrase.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = zenPhrase,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // --- 3. Bottom Stats & Server Panel ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // A. Expandable Connection Info Card (Visible when connected)
                AnimatedVisibility(
                    visible = vpnState is HaikuVpnService.VpnState.Connected,
                    enter = fadeIn(animationSpec = tween(400)) + expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                    exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(
                        animationSpec = spring(
                            stiffness = Spring.StiffnessLow
                        )
                    )
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Timing & Latency
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("ВРЕМЯ СЕССИИ", fontSize = 10.sp, letterSpacing = 1.sp, color = MutedText)
                                    Text(formatDuration(duration), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("ЗАДЕРЖКА", fontSize = 10.sp, letterSpacing = 1.sp, color = MutedText)
                                    Text("${activePing ?: "—"} ms", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MossGreen)
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f))

                            // IP & Protocol
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("ВИРТУАЛЬНЫЙ IP", fontSize = 10.sp, letterSpacing = 1.sp, color = MutedText)
                                    Text(ipAddress, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("ПРОТОКОЛ", fontSize = 10.sp, letterSpacing = 1.sp, color = MutedText)
                                    Text("VLESS Reality", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f))

                            // Speed Counters
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("↓", fontSize = 18.sp, color = MossGreen, modifier = Modifier.padding(end = 4.dp))
                                    Text(formatBytes(rxBytes), fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("↑", fontSize = 18.sp, color = SakuraPink, modifier = Modifier.padding(end = 4.dp))
                                    Text(formatBytes(txBytes), fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                                }
                            }
                        }
                    }
                }

                // B. Server Selector Trigger Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { onNavigateToServers() }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CountryBadge(
                        countryCode = selectedNode?.countryCode ?: "vpn",
                        modifier = Modifier.padding(end = 10.dp)
                    )
                    Text(
                        text = selectedNode?.name ?: "Выбрать узел",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (vpnState is HaikuVpnService.VpnState.Connected) MossGreen
                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun CountryBadge(
    countryCode: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            )
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = countryCode.uppercase(Locale.ROOT),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = TechnicalFontFamily,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 0.5.sp
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format("%02d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B/s"
    val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
    val unit = arrayOf("KiB/s", "MiB/s", "GiB/s")[exp - 1]
    return String.format(Locale.getDefault(), "%.1f %s", bytes / Math.pow(1024.0, exp.toDouble()), unit)
}
