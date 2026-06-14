package com.haiku.vpn.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haiku.vpn.core.HaikuVpnService
import com.haiku.vpn.ui.theme.MossGreen
import com.haiku.vpn.ui.theme.SakuraPink
import com.haiku.vpn.ui.theme.TechnicalFontFamily
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EnsoConnectionButton(
    vpnState: HaikuVpnService.VpnState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 240.dp,
    isAnimating: Boolean = true
) {
    // 1. Color morphing state based on VPN status
    val targetColor = when (vpnState) {
        is HaikuVpnService.VpnState.Disconnected -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
        is HaikuVpnService.VpnState.Connecting -> SakuraPink
        is HaikuVpnService.VpnState.Connected -> MossGreen
        is HaikuVpnService.VpnState.Error -> Color(0xFFC88A8A)
    }
    val animatedColorState = animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing),
        label = "EnsoColor"
    )

    // 2. Infinite transitions - keep as State objects to prevent recomposition when read inside Canvas
    val infiniteTransition = rememberInfiniteTransition(label = "EnsoWavesTransition")
    
    val phase1State = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Phase1"
    )

    val phase2State = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Phase2"
    )

    val phase3State = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(7300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Phase3"
    )

    // 3. Smooth amplitude scaling State
    val targetAmplitudeScale = when (vpnState) {
        is HaikuVpnService.VpnState.Connecting -> 1.4f
        is HaikuVpnService.VpnState.Connected -> 1.0f
        is HaikuVpnService.VpnState.Disconnected -> 0.4f
        is HaikuVpnService.VpnState.Error -> 0.2f
    }
    val amplitudeScaleState = animateFloatAsState(
        targetValue = targetAmplitudeScale,
        animationSpec = tween(durationMillis = 800, easing = EaseInOutSine),
        label = "AmplitudeScale"
    )

    // Ripple wave progress State for connecting state
    val connectingWaveProgressState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "ConnectingWaveProgress"
    )

    // Breathing pulse State for connected state
    val breathScaleState = infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathPulse"
    )

    // 4. Spring-based press scale animation for tactile, high-performance click feedback
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressedScaleState = animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "PressedScale"
    )

    // Precalculate wave math components to save CPU cycles
    val pointsCount = 50
    val cosAngles = remember(pointsCount) {
        FloatArray(pointsCount + 1) { i -> cos((i.toFloat() / pointsCount) * 2 * Math.PI.toFloat()) }
    }
    val sinAngles = remember(pointsCount) {
        FloatArray(pointsCount + 1) { i -> sin((i.toFloat() / pointsCount) * 2 * Math.PI.toFloat()) }
    }
    val innerAngles3 = remember(pointsCount) {
        FloatArray(pointsCount + 1) { i -> 3 * ((i.toFloat() / pointsCount) * 2 * Math.PI.toFloat()) }
    }
    val innerAngles8 = remember(pointsCount) {
        FloatArray(pointsCount + 1) { i -> 8 * ((i.toFloat() / pointsCount) * 2 * Math.PI.toFloat()) }
    }
    val middleAngles4 = remember(pointsCount) {
        FloatArray(pointsCount + 1) { i -> 4 * ((i.toFloat() / pointsCount) * 2 * Math.PI.toFloat()) }
    }
    val middleAngles9 = remember(pointsCount) {
        FloatArray(pointsCount + 1) { i -> 9 * ((i.toFloat() / pointsCount) * 2 * Math.PI.toFloat()) }
    }
    val outerAngles5 = remember(pointsCount) {
        FloatArray(pointsCount + 1) { i -> 5 * ((i.toFloat() / pointsCount) * 2 * Math.PI.toFloat()) }
    }
    val outerAngles11 = remember(pointsCount) {
        FloatArray(pointsCount + 1) { i -> 11 * ((i.toFloat() / pointsCount) * 2 * Math.PI.toFloat()) }
    }

    val innerPath = remember { Path() }
    val middlePath = remember { Path() }
    val outerPath = remember { Path() }

    Box(
        modifier = modifier
            .size(buttonSize)
            .graphicsLayer {
                scaleX = pressedScaleState.value
                scaleY = pressedScaleState.value
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(buttonSize * 0.916f)) {
            // Read animation states ONLY inside draw scope to prevent recomposing EnsoConnectionButton
            val p1 = if (isAnimating) phase1State.value else 0f
            val p2 = if (isAnimating) phase2State.value else 0f
            val p3 = if (isAnimating) phase3State.value else 0f
            val cWaveProgress = if (isAnimating) connectingWaveProgressState.value else 0.5f
            val bScale = if (isAnimating) breathScaleState.value else 1.0f
            val amplitudeScale = amplitudeScaleState.value
            val animatedColor = animatedColorState.value

            val currentScale = when (vpnState) {
                is HaikuVpnService.VpnState.Connected -> bScale
                is HaikuVpnService.VpnState.Connecting -> 1.01f
                else -> 1.0f
            }

            val maxRadius = size.width / 2f
            val baseRadius = maxRadius * 0.7f

            // --- A. Draw outer expanding ripples (connecting state) ---
            if (vpnState is HaikuVpnService.VpnState.Connecting) {
                val r1 = baseRadius + (maxRadius - baseRadius) * cWaveProgress
                val alpha1 = (1f - cWaveProgress) * 0.25f
                drawCircle(
                    color = animatedColor,
                    radius = r1,
                    alpha = alpha1
                )

                val delayedProgress = (cWaveProgress + 0.5f) % 1.0f
                val r2 = baseRadius + (maxRadius - baseRadius) * delayedProgress
                val alpha2 = (1f - delayedProgress) * 0.25f
                drawCircle(
                    color = animatedColor,
                    radius = r2,
                    alpha = alpha2
                )
            }

            // --- B. Draw overlapping, flowing Enso waves ---
            scale(scale = currentScale, pivot = center) {
                // Soft central glow
                if (vpnState is HaikuVpnService.VpnState.Connected || vpnState is HaikuVpnService.VpnState.Connecting) {
                    drawCircle(
                        color = animatedColor,
                        radius = baseRadius * 0.9f,
                        alpha = if (vpnState is HaikuVpnService.VpnState.Connected) 0.1f else 0.05f
                    )
                }

                // 1. INNER WAVE (thick calligraphic stroke, rotating slowly clockwise)
                innerPath.rewind()
                val gapStartInner = 41
                val gapEndInner = 48
                for (i in 0..pointsCount) {
                    if (i in gapStartInner..gapEndInner) continue // Leave Zen calligraphic gap
                    
                    val radiusNoise = (3.5f * sin(innerAngles3[i] + p1) + 1.2f * cos(innerAngles8[i] - p2)) * amplitudeScale
                    val r = (baseRadius * 0.88f) + radiusNoise
                    
                    val x = center.x + r * cosAngles[i]
                    val y = center.y + r * sinAngles[i]
                    
                    if (i == 0 || i == gapEndInner + 1) {
                        innerPath.moveTo(x, y)
                    } else {
                        innerPath.lineTo(x, y)
                    }
                }
                rotate(degrees = (p1 * 180f / Math.PI.toFloat() * 0.3f), pivot = center) {
                    drawPath(
                        path = innerPath,
                        color = animatedColor,
                        alpha = 0.8f,
                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                    )
                }

                // 2. MIDDLE WAVE (medium stroke, rotating counter-clockwise)
                middlePath.rewind()
                val gapStartMiddle = 8
                val gapEndMiddle = 15
                for (i in 0..pointsCount) {
                    if (i in gapStartMiddle..gapEndMiddle) continue
                    
                    val radiusNoise = (5.5f * sin(middleAngles4[i] - p2) + 1.8f * cos(middleAngles9[i] + p3)) * amplitudeScale
                    val r = (baseRadius * 1.02f) + radiusNoise
                    
                    val x = center.x + r * cosAngles[i]
                    val y = center.y + r * sinAngles[i]
                    
                    if (i == 0 || i == gapEndMiddle + 1) {
                        middlePath.moveTo(x, y)
                    } else {
                        middlePath.lineTo(x, y)
                    }
                }
                rotate(degrees = (-p2 * 180f / Math.PI.toFloat() * 0.5f), pivot = center) {
                    drawPath(
                        path = middlePath,
                        color = animatedColor,
                        alpha = 0.5f,
                        style = Stroke(width = 4.5f, cap = StrokeCap.Round)
                    )
                }

                // 3. OUTER WAVE (fine stroke, rotating clockwise)
                outerPath.rewind()
                val gapStartOuter = 25
                val gapEndOuter = 30
                for (i in 0..pointsCount) {
                    if (i in gapStartOuter..gapEndOuter) continue
                    
                    val radiusNoise = (7f * sin(outerAngles5[i] + p3) + 2.5f * cos(outerAngles11[i] - p1)) * amplitudeScale
                    val r = (baseRadius * 1.15f) + radiusNoise
                    
                    val x = center.x + r * cosAngles[i]
                    val y = center.y + r * sinAngles[i]
                    
                    if (i == 0 || i == gapEndOuter + 1) {
                        outerPath.moveTo(x, y)
                    } else {
                        outerPath.lineTo(x, y)
                    }
                }
                rotate(degrees = (p3 * 180f / Math.PI.toFloat() * 0.8f), pivot = center) {
                    drawPath(
                        path = outerPath,
                        color = animatedColor,
                        alpha = 0.25f,
                        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                    )
                }
            }
        }

        // Center state text indicator (styled with premium TechnicalFontFamily, no emojis)
        val buttonText = when (vpnState) {
            is HaikuVpnService.VpnState.Disconnected -> "START"
            is HaikuVpnService.VpnState.Connecting -> "WAIT..."
            is HaikuVpnService.VpnState.Connected -> "STOP"
            is HaikuVpnService.VpnState.Error -> "RETRY"
        }

        Text(
            text = buttonText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = TechnicalFontFamily,
            letterSpacing = 5.sp,
            color = animatedColorState.value.copy(alpha = if (vpnState is HaikuVpnService.VpnState.Disconnected) 0.85f else 0.95f)
        )
    }
}
