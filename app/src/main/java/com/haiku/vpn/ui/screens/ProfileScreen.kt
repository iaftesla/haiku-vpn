package com.haiku.vpn.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.haiku.vpn.R
import com.haiku.vpn.ui.VpnViewModel
import com.haiku.vpn.ui.theme.MossGreen
import com.haiku.vpn.ui.theme.MutedText
import com.haiku.vpn.ui.theme.SakuraPink
import com.haiku.vpn.ui.theme.SumizomeCharcoal
import com.haiku.vpn.ui.theme.PoeticFontFamily
import com.haiku.vpn.ui.theme.TechnicalFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: VpnViewModel,
    onNavigateBack: () -> Unit
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userPlan by viewModel.userPlan.collectAsState()
    val userExpiry by viewModel.userExpiry.collectAsState()
    val authError by viewModel.authError.collectAsState()

    val context = LocalContext.current

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var isAuthLoading by remember { mutableStateOf(false) }
    var showUpgradeDialog by remember { mutableStateOf(false) }
    var showCancelSubscriptionDialog by remember { mutableStateOf(false) }
    var showExtendSubscriptionDialog by remember { mutableStateOf(false) }

    // Sync loading state when logged in changes
    LaunchedEffect(isLoggedIn) {
        isAuthLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PROFILE",
                        fontFamily = TechnicalFontFamily,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text(
                            text = "←",
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedContent(
                targetState = isLoggedIn,
                transitionSpec = {
                    fadeIn(tween(400)) togetherWith fadeOut(tween(300))
                },
                label = "ProfileContentAnim"
            ) { loggedIn ->
                if (loggedIn) {
                    // --- AUTHORIZED USER VIEW ---
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {


                        // User Details Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Почта",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MutedText
                                    )
                                    Text(
                                        text = userEmail,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Тариф",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MutedText
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (userPlan.contains("Premium")) MossGreen.copy(alpha = 0.2f)
                                                    else SakuraPink.copy(alpha = 0.2f)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (userPlan.contains("Premium")) "PREMIUM" else "FREE",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (userPlan.contains("Premium")) MossGreen else SakuraPink
                                            )
                                        }
                                    }
                                }

                                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Действует до",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MutedText
                                    )
                                    Text(
                                        text = userExpiry,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }

                        // Cloud sync item
                        var syncEnabled by remember { mutableStateOf(true) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Облачная синхронизация",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Автоматически сохранять узлы в облако",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontSize = 12.sp,
                                    color = MutedText
                                )
                            }
                            Switch(
                                checked = syncEnabled,
                                onCheckedChange = { syncEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.background,
                                    checkedTrackColor = MossGreen,
                                    uncheckedBorderColor = MutedText
                                )
                            )
                        }

                        // --- Support Bot & Website ---
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "ПОДДЕРЖКА И РЕСУРСЫ",
                                    fontFamily = TechnicalFontFamily,
                                    fontSize = 11.sp,
                                    color = MossGreen,
                                    letterSpacing = 1.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/haiku_vpn_bot"))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Telegram не установлен", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Чат поддержки в Telegram",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "Помощь с настройкой, вопросы и ответы",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MutedText
                                        )
                                    }
                                    Text(
                                        text = "→",
                                        fontSize = 16.sp,
                                        color = MossGreen,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }

                                Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://haikuvpn.org"))
                                            context.startActivity(intent)
                                        }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Официальный сайт",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "Информация о проекте и новости",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MutedText
                                        )
                                    }
                                    Text(
                                        text = "→",
                                        fontSize = 16.sp,
                                        color = MossGreen,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }

                        // --- Subscription Management (for Premium Users) ---
                        if (userPlan.contains("Premium")) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(22.dp),
                                    verticalArrangement = Arrangement.spacedBy(18.dp)
                                ) {
                                    Text(
                                        text = "УПРАВЛЕНИЕ ПОДПИСКОЙ",
                                        fontFamily = TechnicalFontFamily,
                                        fontSize = 11.sp,
                                        color = MossGreen,
                                        letterSpacing = 1.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Column {
                                        Text(
                                            text = "Автопродление: Активно",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "Следующее списание: 14.06.2027",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MutedText
                                        )
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(
                                            onClick = { showExtendSubscriptionDialog = true },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MossGreen,
                                                contentColor = MaterialTheme.colorScheme.background
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            Text("ПРОДЛИТЬ", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = TechnicalFontFamily)
                                        }

                                        OutlinedButton(
                                            onClick = { showCancelSubscriptionDialog = true },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = SakuraPink
                                            ),
                                            border = borderStroke(),
                                            contentPadding = PaddingValues(vertical = 8.dp)
                                        ) {
                                            Text("ОТМЕНИТЬ", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = TechnicalFontFamily)
                                        }
                                    }
                                }
                            }
                        }

                        // Upgrade card & logouts
                        if (!userPlan.contains("Premium")) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.2.dp,
                                    SakuraPink.copy(alpha = 0.35f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "HAIKU PREMIUM",
                                            fontFamily = TechnicalFontFamily,
                                            fontSize = 12.sp,
                                            color = SakuraPink,
                                            letterSpacing = 2.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        
                                        Text(
                                            text = "199 ₽ / мес",
                                            fontFamily = TechnicalFontFamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "Безграничный поток силы",
                                            fontFamily = PoeticFontFamily,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "Полный доступ ко всем узлам без ограничений скорости. Отсутствие рекламы, максимальный приоритет канала.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MutedText,
                                            lineHeight = 20.sp
                                        )
                                    }

                                    // List of detailed features
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    ) {
                                        listOf(
                                            "Максимальная пропускная способность до 1 Гбит/с",
                                            "Шифрование нового поколения Reality (VLESS)",
                                            "Поддержка всех устройств на одном аккаунте",
                                            "Приоритетная линия технической поддержки"
                                        ).forEach { feature ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(SakuraPink)
                                                )
                                                Text(
                                                    text = feature,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = { showUpgradeDialog = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SakuraPink,
                                            contentColor = SumizomeCharcoal
                                        )
                                    ) {
                                        Text(
                                            text = "АКТИВИРОВАТЬ ПОДПИСКУ",
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            fontFamily = TechnicalFontFamily
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = SakuraPink
                            ),
                            border = borderStroke()
                        ) {
                            Text(
                                text = "ВЫЙТИ ИЗ АККАУНТА",
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp,
                                fontFamily = TechnicalFontFamily
                            )
                        }
                    }
                } else {
                    // --- UNAUTHORIZED / LOGIN FORM ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {


                        // Title
                        Text(
                            text = if (isRegistering) "Создать аккаунт" else "Вход в Haiku Cloud",
                            fontFamily = PoeticFontFamily,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isRegistering) "Синхронизируйте свои профили и получите доступ ко всем узлам"
                            else "Авторизуйтесь, чтобы восстановить сохраненные настройки",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Email Field
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Почта (E-mail)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            colors = textFieldColors()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password Field
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Пароль") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            colors = textFieldColors()
                        )

                        // Error Output
                        authError?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = err,
                                color = SakuraPink,
                                fontSize = 13.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                isAuthLoading = true
                                if (isRegistering) {
                                    viewModel.register(emailInput, passwordInput)
                                } else {
                                    viewModel.login(emailInput, passwordInput)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.background
                            ),
                            enabled = !isAuthLoading && emailInput.isNotEmpty() && passwordInput.isNotEmpty()
                        ) {
                            if (isAuthLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.background,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isRegistering) "ЗАРЕГИСТРИРОВАТЬСЯ" else "ВОЙТИ",
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        if (!isRegistering) {
                            Spacer(modifier = Modifier.height(12.dp))

                            // Quick Demo Account Login Button
                            OutlinedButton(
                                onClick = {
                                    isAuthLoading = true
                                    emailInput = "demo@haikuvpn.org"
                                    passwordInput = "123456"
                                    viewModel.login("demo@haikuvpn.org", "123456")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MossGreen
                                ),
                                border = borderStroke()
                            ) {
                                Text(
                                    text = "БЫСТРЫЙ ДЕМО-ВХОД",
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontFamily = TechnicalFontFamily
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Toggle Login/Register
                        Text(
                            text = if (isRegistering) "Уже есть аккаунт? Войти" else "Нет аккаунта? Зарегистрироваться",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MossGreen,
                            modifier = Modifier
                                .clickable {
                                    isRegistering = !isRegistering
                                    viewModel.logout() // clears errors
                                }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    }

    // --- Premium Upgrade Dialog ---
    if (showUpgradeDialog) {
        AlertDialog(
            onDismissRequest = { showUpgradeDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.upgradeToPremium()
                        showUpgradeDialog = false
                        Toast.makeText(context, "Подписка Premium активирована!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MossGreen,
                        contentColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text("ПОДКЛЮЧИТЬ", fontFamily = TechnicalFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpgradeDialog = false }) {
                    Text("ОТМЕНА", color = MutedText, fontFamily = TechnicalFontFamily)
                }
            },
            title = {
                Text(
                    text = "Активация Haiku Premium",
                    fontFamily = PoeticFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Получите полный доступ ко всем возможностям приложения:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("—", color = MossGreen, modifier = Modifier.padding(end = 8.dp), fontFamily = TechnicalFontFamily)
                            Text("Высокоскоростные узлы по всему миру", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("—", color = MossGreen, modifier = Modifier.padding(end = 8.dp), fontFamily = TechnicalFontFamily)
                            Text("Безлимитный трафик без ограничений", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("—", color = MossGreen, modifier = Modifier.padding(end = 8.dp), fontFamily = TechnicalFontFamily)
                            Text("Поддержка шифрования VLESS Reality", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Text(
                        text = "Стоимость: Бесплатно для тестирования интерфейса.",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MossGreen
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- Premium Extend Dialog ---
    if (showExtendSubscriptionDialog) {
        AlertDialog(
            onDismissRequest = { showExtendSubscriptionDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.upgradeToPremium() // Renew Premium date representation
                        showExtendSubscriptionDialog = false
                        Toast.makeText(context, "Подписка продлена до 14.06.2028!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MossGreen,
                        contentColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text("ПРОДЛИТЬ", fontFamily = TechnicalFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExtendSubscriptionDialog = false }) {
                    Text("ОТМЕНА", color = MutedText, fontFamily = TechnicalFontFamily)
                }
            },
            title = {
                Text(
                    text = "Продлить подписку?",
                    fontFamily = PoeticFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = "Продлить срок действия Haiku Premium еще на один год? Списание средств производится в демонстрационном режиме.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // --- Premium Cancel Dialog ---
    if (showCancelSubscriptionDialog) {
        AlertDialog(
            onDismissRequest = { showCancelSubscriptionDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelPremium() // Reset to free plan
                        showCancelSubscriptionDialog = false
                        Toast.makeText(context, "Автопродление отключено. Тариф сброшен.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SakuraPink,
                        contentColor = SumizomeCharcoal
                    )
                ) {
                    Text("ОТКЛЮЧИТЬ", fontFamily = TechnicalFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelSubscriptionDialog = false }) {
                    Text("ОТМЕНА", color = MutedText, fontFamily = TechnicalFontFamily)
                }
            },
            title = {
                Text(
                    text = "Отменить Premium подписку?",
                    fontFamily = PoeticFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = "Вы уверены, что хотите отменить подписку Haiku Premium? Ваша скорость будет ограничена до базовой, и доступ к выделенным узлам закроется.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MutedText,
    cursorColor = MaterialTheme.colorScheme.primary
)

@Composable
private fun borderStroke() = androidx.compose.foundation.BorderStroke(
    width = 1.dp,
    color = SakuraPink.copy(alpha = 0.5f)
)
