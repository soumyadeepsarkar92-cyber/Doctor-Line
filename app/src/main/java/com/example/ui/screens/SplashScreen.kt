package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val logoScale = remember { Animatable(0.8f) }
    val logoAlpha = remember { Animatable(0f) }
    val glowScale = remember { Animatable(0.8f) }
    val glowAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // Phase 2: Logo scale & fade in (Apple-style, ease out)
        launch {
            logoScale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 1000, easing = androidx.compose.animation.core.FastOutSlowInEasing)
            )
        }
        launch {
            logoAlpha.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 800)
            )
        }
        launch {
            // Glow burst: scales up and fades out
            glowAlpha.animateTo(0.35f, animationSpec = tween(400))
            glowScale.animateTo(1.3f, animationSpec = tween(1000, easing = androidx.compose.animation.core.LinearOutSlowInEasing))
            glowAlpha.animateTo(0f, animationSpec = tween(600))
        }

        // Phase 3: Text and tagline fade in shortly after logo starts
        delay(400)
        textAlpha.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 1000)
        )

        // Phase 4: Smooth transition delay before navigation (total launch time ~2.5s)
        delay(1100)

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
                        Color(0xFF2563EB), // MedicalBlue
                        Color(0xFF1E3A8A)  // Deep Royal Blue
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Logo container for soft glow effect
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                // Soft glow element (Phase 2)
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .graphicsLayer(
                            scaleX = glowScale.value,
                            scaleY = glowScale.value,
                            alpha = glowAlpha.value
                        )
                        .clip(CircleShape)
                        .background(Color.White)
                )

                // Logo element (Phase 2)
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "DoctorLine Logo",
                    modifier = Modifier
                        .size(140.dp)
                        .graphicsLayer(
                            scaleX = logoScale.value,
                            scaleY = logoScale.value,
                            alpha = logoAlpha.value
                        )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App name fade in (Phase 3)
            Text(
                text = "DoctorLine",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = (-1.5).sp,
                modifier = Modifier.graphicsLayer(alpha = textAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline fade in (Phase 3)
            Text(
                text = "Smart Healthcare, Simplified",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer(alpha = textAlpha.value)
            )
        }

        // Footnote (Phase 3)
        Text(
            text = "Powered by DoctorLine Network",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.4f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .graphicsLayer(alpha = textAlpha.value)
        )
    }
}
