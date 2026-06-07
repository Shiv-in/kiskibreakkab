package com.example.kiskibreakkab.presentation.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiskibreakkab.core.components.*
import com.example.kiskibreakkab.core.theme.*
import kotlinx.coroutines.launch

@Composable
fun LandingScreen(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit,
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {}
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        GridBackground(modifier = Modifier.fillMaxSize())
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header Section
            item {
                LandingHeader(onLogin, isDarkTheme, onToggleTheme)
            }

            // Hero Section
            item {
                HeroSection(onGetStarted, onLogin)
            }

            // Features Section
            item {
                FeaturesSection()
            }

            // CTA Section
            item {
                CTASection(onGetStarted)
            }

            // Footer
            item {
                Footer()
            }
        }
    }
}

@Composable
fun LandingHeader(
    onLogin: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo
        Box {
            // Shadow
            Box(modifier = Modifier.size(48.dp).offset(x = 3.dp, y = 3.dp).background(MaterialTheme.colorScheme.onBackground))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(KiskiRed)
                    .border(2.dp, MaterialTheme.colorScheme.onBackground),
                contentAlignment = Alignment.Center
            ) {
                Text("KBK", color = KiskiWhite, fontWeight = FontWeight.Black, fontSize = 14.sp)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Transparent)
                    .border(2.dp, MaterialTheme.colorScheme.onBackground)
                    .clickable { onToggleTheme() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = "LOGIN",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                modifier = Modifier.clickable { onLogin() }
            )
        }
    }
}

@Composable
fun HeroSection(onGetStarted: () -> Unit, onLogin: () -> Unit) {
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Badge
        Box(
            modifier = Modifier
                .rotate(rotation.value - 2f)
                .clickable {
                    scope.launch {
                        repeat(3) {
                            rotation.animateTo(5f, animationSpec = tween(50))
                            rotation.animateTo(-5f, animationSpec = tween(50))
                        }
                        rotation.animateTo(0f, animationSpec = tween(50))
                    }
                }
        ) {
            // Red Shadow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 4.dp, y = 4.dp)
                    .background(KiskiRed)
            )
            // Surface
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .border(2.dp, MaterialTheme.colorScheme.onBackground)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "STUDENT MADE • FOR STUDENTS",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Headline
        Column {
            Text(
                text = "WHO IS",
                fontSize = 80.sp,
                lineHeight = 75.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Box(modifier = Modifier.wrapContentSize()) {
                // Thick White/Dynamic Underline
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(0.5f)
                        .height(12.dp)
                        .background(MaterialTheme.colorScheme.onBackground)
                )
                Text(
                    text = "FREE",
                    fontSize = 80.sp,
                    lineHeight = 75.sp,
                    fontWeight = FontWeight.Black,
                    color = KiskiRed
                )
            }
            Text(
                text = "RN?",
                fontSize = 80.sp,
                lineHeight = 75.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Sync timetables. Find friends.\nMake every campus break count.",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(64.dp))

        // Hero Buttons
        BrutalistButton(
            text = "",
            onClick = onGetStarted,
            containerColor = KiskiRed,
            contentColor = Color.White,
            shadowColor = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("GET STARTED", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BrutalistButton(
            text = "LOGIN",
            onClick = onLogin,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            shadowColor = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun FeaturesSection() {
    Column(modifier = Modifier.padding(24.dp)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(48.dp))
        
        FeatureItem(
            icon = Icons.Default.Schedule,
            title = "Unknown Variable? No.",
            description = "Real-time status updates so you never have to text \"kahan hai?\" again."
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(48.dp))
        
        FeatureItem(
            icon = Icons.Default.CalendarToday,
            title = "Sync & Forget.",
            description = "One-time timetable setup. We handle the conflicting slots logic automatically."
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(48.dp))
        
        FeatureItem(
            icon = Icons.Default.Group,
            title = "Squad Goals.",
            description = "Create groups. See availability heatmaps. Plan that mass bunk (jk... unless?)."
        )
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun FeatureItem(icon: ImageVector, title: String, description: String) {
    Column {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun CTASection(onLaunch: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(64.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(2.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(vertical = 48.dp, horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            val stripeColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
            // Diagonal Stripes Background
            Canvas(modifier = Modifier.matchParentSize()) {
                val step = 40.dp.toPx()
                val strokeWidth = 20.dp.toPx()
                for (i in -10..20) {
                    drawLine(
                        color = stripeColor,
                        start = Offset(i * step, 0f),
                        end = Offset(i * step + size.height, size.height),
                        strokeWidth = strokeWidth
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "READY TO BREAK?",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onLaunch,
                    colors = ButtonDefaults.buttonColors(containerColor = KiskiRed),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp)
                ) {
                    Text(
                        "LAUNCH APP",
                        color = KiskiWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(64.dp))
        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)
    }
}

@Composable
fun Footer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(20.dp).background(MaterialTheme.colorScheme.onBackground))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "KISKIBREAKKAB",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                letterSpacing = 1.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FooterLink("GITHUB")
            FooterLink("IG")
            FooterLink("TWITTER")
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            "© 2026 STUDENT PROJECT • V1.0.0",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun FooterLink(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Black,
        fontSize = 14.sp,
        modifier = Modifier.clickable { }
    )
}
