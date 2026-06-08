package com.example.kiskibreakkab.presentation.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiskibreakkab.core.components.GridBackground
import com.example.kiskibreakkab.core.theme.KiskiRed
import com.example.kiskibreakkab.core.theme.KiskiWhite
import com.example.kiskibreakkab.domain.model.User
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isReady: Boolean,
    currentUser: User?,
    onNavigateToDashboard: () -> Unit,
    onNavigateToLanding: () -> Unit
) {
    val scale = remember { Animatable(0f) }
    
    LaunchedEffect(isReady) {
        if (isReady) {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = {
                        OvershootInterpolator(2f).getInterpolation(it)
                    }
                )
            )
            delay(1000) // Minimum display time
            if (currentUser != null) {
                onNavigateToDashboard()
            } else {
                onNavigateToLanding()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        GridBackground()
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Tactical Logo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale.value)
                    .background(KiskiRed)
                    .border(4.dp, MaterialTheme.colorScheme.onBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "KBK",
                    color = KiskiWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Initialization Message
            Text(
                text = "INITIALIZING...",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 4.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                modifier = Modifier.width(140.dp),
                color = KiskiRed,
                trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
            )
        }
        
        // System Meta info
        Text(
            text = "TACTICAL AVAILABILITY MODULE V1.0.1",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

// Simple Overshoot Interpolator for Compose
class OvershootInterpolator(private val tension: Float = 2f) {
    fun getInterpolation(t: Float): Float {
        var time = t
        time -= 1.0f
        return time * time * ((tension + 1) * time + tension) + 1.0f
    }
}
