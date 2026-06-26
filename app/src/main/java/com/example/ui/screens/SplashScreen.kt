package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    isUserLoggedIn: Boolean?,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val logoScale = remember { Animatable(0.3f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(30f) }
    
    val pulseScale1 = remember { Animatable(0.8f) }
    val pulseScale2 = remember { Animatable(0.8f) }
    val pulseAlpha1 = remember { Animatable(0.5f) }
    val pulseAlpha2 = remember { Animatable(0.3f) }
    
    val syncText = remember { mutableStateOf("Initializing DoctorLine Portal...") }
    val syncProgress = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // Pulse glow effects (infinite loop)
        launch {
            while(true) {
                pulseScale1.snapTo(0.9f)
                pulseAlpha1.snapTo(0.6f)
                launch {
                    pulseScale1.animateTo(1.6f, animationSpec = tween(1500, easing = LinearOutSlowInEasing))
                }
                launch {
                    pulseAlpha1.animateTo(0f, animationSpec = tween(1500, easing = LinearOutSlowInEasing))
                }
                delay(750)
                
                pulseScale2.snapTo(0.9f)
                pulseAlpha2.snapTo(0.4f)
                launch {
                    pulseScale2.animateTo(1.6f, animationSpec = tween(1500, easing = LinearOutSlowInEasing))
                }
                launch {
                    pulseAlpha2.animateTo(0f, animationSpec = tween(1500, easing = LinearOutSlowInEasing))
                }
                delay(750)
            }
        }

        // Logo scale and fade in (Bouncy / Spring feel)
        launch {
            logoScale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            logoAlpha.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }

        // Text slide and fade in
        delay(400)
        launch {
            textAlpha.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 800)
            )
        }
        launch {
            textOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // Fake premium portal synchronization
        launch {
            syncProgress.animateTo(0.4f, animationSpec = tween(800, easing = FastOutSlowInEasing))
            syncText.value = "Establishing Secure Handshake..."
            delay(400)
            syncProgress.animateTo(0.7f, animationSpec = tween(600, easing = FastOutSlowInEasing))
            syncText.value = "Syncing Encrypted Healthcare Database..."
            delay(450)
            syncProgress.animateTo(1.0f, animationSpec = tween(400, easing = FastOutSlowInEasing))
            syncText.value = "Access Granted"
            delay(200)
            
            if (isUserLoggedIn == true) {
                onNavigateToHome()
            } else {
                onNavigateToLogin()
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Dark slate-blue
                        Color(0xFF1E1E38), // Rich Dark Purple-Navy
                        Color(0xFF090D16)  // Pitch black-navy
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        val screenWidth = maxWidth
        val logoSize = screenWidth * 0.38f // Logo size is 38% of screen width (satisfies 35-40% rule)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Elegant Glow and Logo container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(logoSize + 60.dp)
            ) {
                // Outer Pulse Glow 1
                Box(
                    modifier = Modifier
                        .size(logoSize)
                        .graphicsLayer(
                            scaleX = pulseScale1.value,
                            scaleY = pulseScale1.value,
                            alpha = pulseAlpha1.value
                        )
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF7C5DFA).copy(alpha = 0.4f), Color.Transparent)
                            )
                        )
                )

                // Outer Pulse Glow 2
                Box(
                    modifier = Modifier
                        .size(logoSize)
                        .graphicsLayer(
                            scaleX = pulseScale2.value,
                            scaleY = pulseScale2.value,
                            alpha = pulseAlpha2.value
                        )
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF10B981).copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                )

                // Core logo with card container
                Box(
                    modifier = Modifier
                        .size(logoSize)
                        .graphicsLayer(
                            scaleX = logoScale.value,
                            scaleY = logoScale.value,
                            alpha = logoAlpha.value
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1E293B).copy(alpha = 0.8f),
                                    Color(0xFF0F172A).copy(alpha = 0.95f)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF7C5DFA).copy(alpha = 0.5f),
                                    Color(0xFF10B981).copy(alpha = 0.2f)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .drawBehind {
                            // Extra drop shadow style glow inside canvas
                            drawCircle(
                                color = Color(0xFF7C5DFA).copy(alpha = 0.15f),
                                radius = size.minDimension / 1.8f
                            )
                        }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "DoctorLine Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App name fade in with offset
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer(
                    alpha = textAlpha.value,
                    translationY = textOffsetY.value
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Doctor",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "Line",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF10B981),
                        letterSpacing = (-1).sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tagline
                Text(
                    text = "Smart Healthcare, Simplified",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Bottom Secure Sync Section (banking app experience)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp, start = 48.dp, end = 48.dp)
                .fillMaxWidth()
                .graphicsLayer(alpha = textAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant slim loading bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(syncProgress.value)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF7C5DFA), Color(0xFF10B981))
                            )
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "Secure Connection",
                    tint = Color(0xFF34D399),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = syncText.value,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
