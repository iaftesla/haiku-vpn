package com.haiku.vpn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haiku.vpn.core.RealityConfig
import com.haiku.vpn.ui.VpnViewModel
import com.haiku.vpn.ui.theme.MossGreen
import com.haiku.vpn.ui.theme.MutedText
import com.haiku.vpn.ui.theme.SakuraPink
import androidx.compose.foundation.border
import com.haiku.vpn.ui.theme.TechnicalFontFamily
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ServerListScreen(
    viewModel: VpnViewModel,
    onNavigateBack: () -> Unit
) {
    val nodes by viewModel.nodes.collectAsState()
    val selectedNode by viewModel.selectedNode.collectAsState()
    val pings by viewModel.pings.collectAsState()

    var showDeleteConfirmDialog by remember { mutableStateOf<RealityConfig?>(null) }

    // Measure pings automatically on screen entrance
    LaunchedEffect(Unit) {
        viewModel.testAllPings()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- 1. Header ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Назад",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .clickable { onNavigateBack() }
                        .padding(8.dp)
                )

                Text(
                    text = "Узлы",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Тест",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MossGreen,
                    modifier = Modifier
                        .clickable { viewModel.testAllPings() }
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "Выберите путь в тишине. Долгое нажатие удалит узел.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. List of Nodes ---
            if (nodes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Сад пуст.\nИмпортируйте узлы в настройках.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MutedText,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(nodes, key = { it.name + it.address }) { node ->
                        val isSelected = selectedNode?.name == node.name
                        val ping = pings[node.name]

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.surface else Color.Unspecified
                                )
                                .combinedClickable(
                                    onClick = {
                                        viewModel.selectNode(node)
                                        onNavigateBack()
                                    },
                                    onLongClick = {
                                        showDeleteConfirmDialog = node
                                    }
                                )
                                .padding(horizontal = 16.dp, vertical = 18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CountryBadge(
                                        countryCode = node.countryCode,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Column {
                                        Text(
                                            text = node.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${node.address}:${node.port}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MutedText
                                        )
                                    }
                                }

                                // Ping status display
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (ping != null) {
                                        val (pingText, pingColor) = when {
                                            ping < 0 -> "Офлайн" to Color(0xFFC88A8A)
                                            ping < 150 -> "${ping}мс" to MossGreen
                                            ping < 300 -> "${ping}мс" to MutedText
                                            else -> "${ping}мс" to Color(0xFFC8A58A)
                                        }
                                        Text(
                                            text = pingText,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = pingColor
                                        )
                                    }

                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MossGreen)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmDialog?.let { viewModel.deleteNode(it) }
                    showDeleteConfirmDialog = null
                }) {
                    Text("Удалить", color = Color(0xFFC88A8A))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Отмена", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
            },
            title = {
                Text(
                    text = "Освободить путь?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = "Вы уверены, что хотите удалить конфигурацию узла '${showDeleteConfirmDialog?.name}'?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
            textContentColor = MaterialTheme.colorScheme.onBackground,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
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

