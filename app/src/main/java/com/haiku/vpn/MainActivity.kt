package com.haiku.vpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.os.Build
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import com.haiku.vpn.ui.VpnViewModel
import com.haiku.vpn.ui.screens.MainScreen
import com.haiku.vpn.ui.screens.ProfileScreen
import com.haiku.vpn.ui.screens.ServerListScreen
import com.haiku.vpn.ui.screens.SettingsScreen
import com.haiku.vpn.ui.theme.HaikuTheme

class MainActivity : ComponentActivity() {

    private val viewModel: VpnViewModel by viewModels()

    // Register Activity Result Launcher for VPN service permission request
    private val vpnPrepareLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Permission granted, execute connection
            viewModel.toggleConnection()
        } else {
            Toast.makeText(this, "VPN permission denied. Cannot establish tunnel.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        super.onCreate(savedInstanceState)
        
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            HaikuTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HaikuAppNavigation(
                        viewModel = viewModel,
                        onToggleConnection = { handleVpnToggle() }
                    )
                }
            }
        }
    }

    /**
     * Intercepts VPN connection triggers to check and request Android VpnService permissions first.
     */
    private fun handleVpnToggle() {
        val prepareIntent = VpnService.prepare(this)
        if (prepareIntent != null) {
            // Android VpnService requires system user authorization first
            vpnPrepareLauncher.launch(prepareIntent)
        } else {
            // Already authorized, toggle connection directly
            viewModel.toggleConnection()
        }
    }
}

@Composable
fun HaikuAppNavigation(
    viewModel: VpnViewModel,
    onToggleConnection: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main",
        enterTransition = {
            fadeIn(animationSpec = tween(350)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(350)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(350)) + slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(350)) + slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350)
            )
        }
    ) {
        composable("main") {
            MainScreen(
                viewModel = viewModel,
                onToggleConnection = onToggleConnection,
                onNavigateToServers = { navController.navigate("servers") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }
        
        composable("servers") {
            ServerListScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("profile") {
            ProfileScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

