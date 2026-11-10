package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextSecondaryMuted

@Composable
fun SplashScreen() {
    val scale = remember { Animatable(0.85f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("splash_screen_view"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Official KivoPay Logo on White Background
            Image(
                painter = painterResource(id = R.drawable.kivopay_logo),
                contentDescription = "KivoPay Logo",
                modifier = Modifier
                    .width(220.dp)
                    .scale(scale.value)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Modern Digital Banking & Payment Platform",
                fontSize = 13.sp,
                color = PrimaryBlue,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Smooth Loading Animation & 10s progress
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = PrimaryBlue,
                strokeWidth = 3.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Menghubungkan ke server aman...",
                fontSize = 12.sp,
                color = TextSecondaryMuted,
                fontWeight = FontWeight.Medium
            )
        }

        // Regulated by Bank Indonesia & App Version at Bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "KIVOPAY DIAWASI OLEH",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = PrimaryBlue
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bank Indonesia Official Logo - Full and Prominent
            Image(
                painter = painterResource(id = R.drawable.logo_bank_indonesia),
                contentDescription = "Bank Indonesia Logo",
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(52.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "versi aplikasi 2.4.5",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondaryMuted
            )
            Text(
                text = "PT Kivo Finansial Indonesia • Berizin & Diawasi Bank Sentral RI",
                fontSize = 10.sp,
                color = TextSecondaryMuted.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
