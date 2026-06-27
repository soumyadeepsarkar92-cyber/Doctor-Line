package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SplashScreen(
    isUserLoggedIn: Boolean?,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val logoScale = remember { Animatable(0.95f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(50f) }
    
    val pulseScale = remember { Animatable(1.0f) }
    val pulseAlpha = remember { Animatable(0.6f) }
    
    val floatOffset = remember { Animatable(0f) }
    val heartbeatProgress = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // Logo fade in and zoom in
        launch {
            logoAlpha.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 1500, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            logoScale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing)
            )
        }

        // Text slide up and fade in
        launch {
            delay(1000)
            textAlpha.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            delay(1000)
            textOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )
        }

        // Pulse animation
        launch {
            while (true) {
                pulseScale.snapTo(1.0f)
                pulseAlpha.snapTo(0.6f)
                launch {
                    pulseScale.animateTo(1.3f, animationSpec = tween(1500, easing = LinearOutSlowInEasing))
                }
                launch {
                    pulseAlpha.animateTo(0f, animationSpec = tween(1500, easing = LinearOutSlowInEasing))
                }
                delay(1600)
            }
        }

        // Float animation
        launch {
            while (true) {
                floatOffset.animateTo(10f, animationSpec = tween(1500, easing = FastOutLinearInEasing))
                floatOffset.animateTo(-10f, animationSpec = tween(1500, easing = FastOutLinearInEasing))
            }
        }

        // Heartbeat animation
        launch {
            while (true) {
                heartbeatProgress.snapTo(0f)
                heartbeatProgress.animateTo(1f, animationSpec = tween(2500, easing = LinearEasing))
            }
        }

        // Navigation delay
        delay(3000)
        if (isUserLoggedIn == true) {
            onNavigateToHome()
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF003366), // Dark Premium Blue
                        Color(0xFF00509E), // Mid Blue
                        Color(0xFF001F3F)  // Navy
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Particle Shimmer Effect & Heartbeat Line
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.5f)) {
            val width = size.width
            val height = size.height

            // Draw Heartbeat Line
            val path = Path()
            val centerY = height * 0.7f
            path.moveTo(0f, centerY)
            path.lineTo(width * 0.2f, centerY)
            path.lineTo(width * 0.25f, centerY - 40f)
            path.lineTo(width * 0.3f, centerY + 50f)
            path.lineTo(width * 0.35f, centerY - 20f)
            path.lineTo(width * 0.4f, centerY)
            path.lineTo(width, centerY)

            val progress = heartbeatProgress.value
            val segment = width * progress
            
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.2f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Draw Particles
            val numParticles = 20
            val time = System.currentTimeMillis() / 1000f
            for (i in 0 until numParticles) {
                val seed = i * 1000
                val rand = java.util.Random(seed.toLong())
                val startX = rand.nextFloat() * width
                val startY = rand.nextFloat() * height
                val speedY = rand.nextFloat() * 20f + 10f
                val sizeVal = rand.nextFloat() * 4f + 2f
                val alphaVal = rand.nextFloat() * 0.5f + 0.1f

                val yOffset = (time * speedY) % height
                var currentY = startY - yOffset
                if (currentY < 0) currentY += height
                
                val currentX = startX + sin(time + i) * 20f

                drawCircle(
                    color = Color.White.copy(alpha = alphaVal),
                    radius = sizeVal,
                    center = Offset(currentX, currentY)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon Container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = logoScale.value,
                        scaleY = logoScale.value,
                        alpha = logoAlpha.value,
                        translationY = floatOffset.value
                    )
            ) {
                // Glow Pulse
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .graphicsLayer(
                            scaleX = pulseScale.value,
                            scaleY = pulseScale.value,
                            alpha = pulseAlpha.value
                        )
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White, Color.Transparent)
                            )
                        )
                )

                // Soft glowing effect
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .shadow(
                            elevation = 30.dp,
                            shape = RoundedCornerShape(32.dp),
                            spotColor = Color.White.copy(alpha = 0.5f),
                            ambientColor = Color.White.copy(alpha = 0.5f)
                        )
                )

                // The Official App Icon
                Image(
                    painter = painterResource(id = R.drawable.medical_app_icon_1782119145796),
                    contentDescription = "Doctor Line App Icon",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(160.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Text section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer(
                    alpha = textAlpha.value,
                    translationY = textOffsetY.value
                )
            ) {
                Text(
                    text = "Doctor Line",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Connecting Patients, Pharmacies\n& Healthcare",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
