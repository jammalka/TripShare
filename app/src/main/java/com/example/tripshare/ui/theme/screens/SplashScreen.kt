package com.example.tripshare.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.graphicsLayer
import com.example.tripshare.R

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val fullText = "TripShare"
    var visibleText by remember { mutableStateOf("") }

    // Rolling animation for logo
    val rotation = rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Reveal text letter by letter
    LaunchedEffect(Unit) {
        fullText.forEachIndexed { index, _ ->
            delay(150)
            visibleText = fullText.take(index + 1)
        }
        delay(3000) // wait after animation
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF87CEFA)), // light blue
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular rolling logo
            Image(
                painter = painterResource(id = R.drawable.tripshare), // place your logo in res/drawable
                contentDescription = "TripShare Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .graphicsLayer(rotationZ = rotation.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Animated text reveal
            Text(
                text = visibleText,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Catchy motto
            Text(
                text = "Ride Together. Share the Journey.",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
