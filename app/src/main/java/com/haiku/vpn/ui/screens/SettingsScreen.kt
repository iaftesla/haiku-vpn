package com.haiku.vpn.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.haiku.vpn.core.RealityConfig
import com.haiku.vpn.ui.VpnViewModel
import com.haiku.vpn.ui.theme.CreamBg
import com.haiku.vpn.ui.theme.DividerGray
import com.haiku.vpn.ui.theme.MossGreen
import com.haiku.vpn.ui.theme.MutedText
import com.haiku.vpn.ui.theme.SakuraPink
import com.haiku.vpn.ui.theme.SumizomeCharcoal
import com.haiku.vpn.ui.theme.PoeticFontFamily
import com.haiku.vpn.ui.theme.TechnicalFontFamily
import java.util.concurrent.Executors

@Composable
fun SettingsScreen(
    viewModel: VpnViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val killSwitchEnabled by viewModel.killSwitchEnabled.collectAsState()

    var configText by remember { mutableStateOf("") }
    var isScanningQr by remember { mutableStateOf(false) }

    val quotes = remember {
        listOf(
            "Простота — это высшая ступень безопасности.",
            "Какими бы ни были наши раны, мы должны продолжать жить...",
            "Если ты не можешь победить, беги. Если бежишь — выживай...",
            "Время течет, смывая имена, но не память о тепле рук.",
            "Не убегай от боли, прими её как капли летнего дождя."
        )
    }
    var quoteIndex by remember { mutableStateOf(0) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                isScanningQr = true
            } else {
                Toast.makeText(context, "Требуется разрешение на камеру для сканирования QR-кода", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
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
                    text = "Настройки",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 2.sp
                )

                // Balanced empty placeholder to align title
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. Security Option: Kill Switch ---
            Text(
                text = "БЕЗОПАСНОСТЬ",
                style = MaterialTheme.typography.labelMedium,
                color = MossGreen,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Экстренное отключение (Kill Switch)",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Блокировать весь интернет-трафик в случае неожиданного отключения VPN.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedText
                    )
                }

                Switch(
                    checked = killSwitchEnabled,
                    onCheckedChange = { viewModel.toggleKillSwitch(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.background,
                        checkedTrackColor = MossGreen,
                        uncheckedThumbColor = MutedText,
                        uncheckedTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. UI Theme Option ---
            Text(
                text = "ОФОРМЛЕНИЕ",
                style = MaterialTheme.typography.labelMedium,
                color = MossGreen,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val themeMode by viewModel.themeMode.collectAsState()
            var showThemeDialog by remember { mutableStateOf(false) }
            val activeThemeLabel = when (themeMode) {
                "light" -> "Светлая"
                "dark" -> "Темная"
                else -> "Системная"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { showThemeDialog = true }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Тема интерфейса",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Выбор цветовой схемы приложения",
                        style = MaterialTheme.typography.labelMedium,
                        color = MutedText
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = activeThemeLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MossGreen
                    )
                    Text(
                        text = "→",
                        fontSize = 16.sp,
                        color = MossGreen
                    )
                }

                if (showThemeDialog) {
                    AlertDialog(
                        onDismissRequest = { showThemeDialog = false },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { showThemeDialog = false }) {
                                Text("ОТМЕНА", color = MossGreen, fontFamily = TechnicalFontFamily, fontWeight = FontWeight.Bold)
                            }
                        },
                        title = {
                            Text(
                                "Стиль оформления",
                                fontFamily = PoeticFontFamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                listOf(
                                    Triple("system", "Системная гармония", "Следовать ритму вашей операционной системы"),
                                    Triple("light", "Светлый Широнэри", "Мягкие светлые тона бумаги"),
                                    Triple("dark", "Темный Сумидзоме", "Глубокие медитативные тона угля")
                                ).forEach { (mode, label, desc) ->
                                    val isSelected = themeMode == mode
                                    
                                    val paletteColors = when (mode) {
                                        "light" -> listOf(CreamBg, SakuraPink, MossGreen)
                                        "dark" -> listOf(SumizomeCharcoal, SakuraPink, MossGreen)
                                        else -> listOf(CreamBg, SumizomeCharcoal, MossGreen)
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (isSelected) MossGreen.copy(alpha = 0.08f)
                                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) MossGreen.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.06f),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable {
                                                viewModel.setThemeMode(mode)
                                                showThemeDialog = false
                                            }
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                                
                                                // Palette bubble indicators
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    paletteColors.forEach { color ->
                                                        Box(
                                                            modifier = Modifier
                                                                .size(10.dp)
                                                                .clip(CircleShape)
                                                                .background(color)
                                                                .border(0.5.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f), CircleShape)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = desc,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MutedText,
                                                lineHeight = 15.sp
                                            )
                                        }
                                        
                                        // Selected state radio indicator
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .border(1.5.dp, if (isSelected) MossGreen else MutedText.copy(alpha = 0.3f), CircleShape)
                                                .padding(4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(CircleShape)
                                                        .background(MossGreen)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. Import Configuration Options ---
            Text(
                text = "ИМПОРТ КОНФИГУРАЦИИ",
                style = MaterialTheme.typography.labelMedium,
                color = MossGreen,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // A. QR Code Scanner Activation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, MossGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable {
                            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                isScanningQr = true
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Сканировать QR-код",
                        style = MaterialTheme.typography.bodyLarge.copy(color = MossGreen),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Divider text "or"
                Text(
                    text = "— ИЛИ ВСТАВИТЬ ССЫЛКУ —",
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedText.copy(alpha = 0.8f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                // B. URI Input Box
                OutlinedTextField(
                    value = configText,
                    onValueChange = { input ->
                        configText = input
                        val trimmed = input.trim()
                        if (trimmed.startsWith("vless://", ignoreCase = true) && trimmed.length > 20) {
                            try {
                                val parsed = RealityConfig.parseFromUri(trimmed)
                                val added = viewModel.addNode(parsed)
                                if (added) {
                                    Toast.makeText(context, "Узел '${parsed.name}' автоматически сохранен", Toast.LENGTH_SHORT).show()
                                    configText = ""
                                    onNavigateBack()
                                }
                            } catch (e: Exception) {
                                // Do nothing, user might be editing the key
                            }
                        }
                    },
                    label = { Text("Вставьте ссылку vless://", color = MossGreen) },
                    placeholder = { Text("vless://...", color = MutedText) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // C. Action buttons for TextInput
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Paste Button
                    Button(
                        onClick = {
                            val pasteText = clipboardManager.getText()?.text ?: ""
                            if (pasteText.isNotEmpty()) {
                                configText = pasteText
                                val trimmed = pasteText.trim()
                                try {
                                    val parsed = RealityConfig.parseFromUri(trimmed)
                                    val added = viewModel.addNode(parsed)
                                    if (added) {
                                        Toast.makeText(context, "Узел '${parsed.name}' автоматически сохранен", Toast.LENGTH_SHORT).show()
                                        configText = ""
                                        onNavigateBack()
                                    } else {
                                        Toast.makeText(context, "Узел уже существует", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Вставлен неполный ключ. Исправьте его вручную.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Вставить", fontFamily = TechnicalFontFamily, fontWeight = FontWeight.Bold)
                    }

                    // Save Button
                    Button(
                        onClick = {
                            if (configText.trim().isEmpty()) {
                                Toast.makeText(context, "Конфигурация пуста", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            try {
                                val parsed = RealityConfig.parseFromUri(configText.trim())
                                val added = viewModel.addNode(parsed)
                                if (added) {
                                    Toast.makeText(context, "Узел '${parsed.name}' импортирован", Toast.LENGTH_SHORT).show()
                                    configText = ""
                                    onNavigateBack() // Go back to main
                                } else {
                                    Toast.makeText(context, "Узел уже существует", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Ошибка импорта: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Импорт", fontFamily = TechnicalFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- 5. App Info Section ---
            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        quoteIndex = (quoteIndex + 1) % quotes.size
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Haiku VPN v1.0.0",
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = quotes[quoteIndex],
                    style = MaterialTheme.typography.labelMedium,
                    color = MossGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }

        // --- 5. QR Code Camera Overlay ---
        if (isScanningQr) {
            QrCodeScannerOverlay(
                onQrScanned = { rawUri ->
                    try {
                        val parsed = RealityConfig.parseFromUri(rawUri)
                        val added = viewModel.addNode(parsed)
                        if (added) {
                            Toast.makeText(context, "Узел '${parsed.name}' импортирован", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Узел уже существует", Toast.LENGTH_SHORT).show()
                        }
                        isScanningQr = false
                        onNavigateBack()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Неверный QR-код конфигурации: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        isScanningQr = false
                    }
                },
                onClose = { isScanningQr = false }
            )
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun QrCodeScannerOverlay(
    onQrScanned: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().apply {
                        setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    val barcodeScanner = BarcodeScanning.getClient()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            barcodeScanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        val rawValue = barcode.rawValue
                                        if (rawValue != null && rawValue.startsWith("vless://", ignoreCase = true)) {
                                            onQrScanned(rawValue)
                                            break
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    Log.e("QrScanner", "MLKit analysis failure", it)
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("QrScanner", "Failed to bind camera use cases", e)
                    }

                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Scanner Frame Overlay UI
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Scanner Title Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Сканирование QR-кода Reality",
                    color = Color.White,
                    fontSize = 18.sp
                )
                Text(
                    text = "Закрыть",
                    color = SakuraPink,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .clickable { onClose() }
                        .padding(8.dp)
                )
            }

            // Central Scanning Frame Box
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .border(2.dp, SakuraPink, RoundedCornerShape(16.dp))
            )

            // Guidance text
            Text(
                text = "Поместите QR-код в рамку для импорта",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 60.dp)
            )
        }
    }
}
