package com.example.kiskibreakkab.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiskibreakkab.core.theme.KiskiBlack
import com.example.kiskibreakkab.core.theme.KiskiGridGray
import com.example.kiskibreakkab.core.theme.KiskiWhite

@Composable
fun BrutalistCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderWidth: Dp = 2.dp,
    shadowOffset: Dp = 6.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier) {
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(MaterialTheme.colorScheme.onBackground)
                .border(borderWidth, MaterialTheme.colorScheme.onBackground)
        )
        // Card
        Column(
            modifier = Modifier
                .background(backgroundColor)
                .border(borderWidth, MaterialTheme.colorScheme.onBackground)
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
fun BrutalistButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    shadowColor: Color = MaterialTheme.colorScheme.onBackground,
    content: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val shadowOffset = 6.dp
    val pressOffset = if (isPressed) 4.dp else 0.dp

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(shadowColor)
                .border(2.dp, shadowColor)
        )
        // Button Surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = pressOffset, y = pressOffset)
                .background(containerColor)
                .border(2.dp, shadowColor)
                .padding(vertical = 14.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (content != null) {
                content()
            } else {
                Text(
                    text = text,
                    color = contentColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun BrutalistTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, MaterialTheme.colorScheme.onBackground),
            placeholder = { Text(placeholder, fontWeight = FontWeight.Bold, color = Color.Gray) },
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            trailingIcon = trailingIcon,
            shape = RoundedCornerShape(0.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onBackground,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun GridBackground(modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background.toArgb() != Color.White.toArgb()
    // Increased alpha to 0.5f for high visibility in dark theme
    val gridColor = if (isDark) Color.White.copy(alpha = 0.5f) else KiskiGridGray.copy(alpha = 0.8f)
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val gridSize = 20.dp.toPx()
                for (x in 0..(size.width / gridSize).toInt()) {
                    drawLine(
                        gridColor,
                        Offset(x * gridSize, 0f),
                        Offset(x * gridSize, size.height),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..(size.height / gridSize).toInt()) {
                    drawLine(
                        gridColor,
                        Offset(0f, y * gridSize),
                        Offset(size.width, y * gridSize),
                        strokeWidth = 1f
                    )
                }
            }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrutalistTopBar(title: String) {
    val borderColor = MaterialTheme.colorScheme.onBackground
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp
            )
        },
        modifier = Modifier.drawBehind {
            val strokeWidth = 3.dp.toPx()
            val y = size.height - strokeWidth / 2
            drawLine(
                color = borderColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = strokeWidth
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun Badge(text: String, color: Color, textColor: Color = KiskiWhite) {
    Box(
        modifier = Modifier
            .background(color)
            .border(1.dp, MaterialTheme.colorScheme.onBackground)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
        )
    }
}
