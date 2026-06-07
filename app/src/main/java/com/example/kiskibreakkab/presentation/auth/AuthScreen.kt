package com.example.kiskibreakkab.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kiskibreakkab.core.components.*
import com.example.kiskibreakkab.core.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }
    
    var uid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    fun resetFields() {
        uid = ""
        password = ""
        confirmPassword = ""
        name = ""
        email = ""
        passwordVisible = false
        confirmPasswordVisible = false
    }

    LaunchedEffect(authMode) {
        if (uiState !is AuthUiState.VerificationSent && uiState !is AuthUiState.PasswordResetSent) {
            resetFields()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onAuthSuccess()
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .imePadding()
        .background(MaterialTheme.colorScheme.background)) {
        GridBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        // Shadow
                        Box(modifier = Modifier.size(42.dp).offset(x = 3.dp, y = 3.dp).background(MaterialTheme.colorScheme.onBackground))
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(KiskiRed)
                                .border(2.dp, MaterialTheme.colorScheme.onBackground)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "KBK",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = KiskiWhite
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(
                        text = when(authMode) {
                            AuthMode.LOGIN -> "Login."
                            AuthMode.REGISTER -> "Register"
                            AuthMode.FORGOT_PASSWORD -> "Forgot"
                        },
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
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
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Form
            Column(modifier = Modifier.padding(20.dp)) {
                if (uiState is AuthUiState.VerificationSent) {
                    BrutalistCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(KiskiGreen)
                                .padding(12.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = KiskiWhite)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "VERIFICATION SENT",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = KiskiWhite
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            "TACTICAL LINK MOBILIZED",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Check your university mail ($email) to activate your operative account. Access will remain locked until verified.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        BrutalistButton(
                            text = "BACK TO LOGIN",
                            onClick = { 
                                authMode = AuthMode.LOGIN
                                viewModel.clearState()
                                resetFields()
                            },
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Offset Red Border (Shadow)
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 6.dp, y = 6.dp)
                                .background(KiskiRed)
                                .border(2.dp, MaterialTheme.colorScheme.onBackground)
                        )
                        
                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .border(2.dp, MaterialTheme.colorScheme.onBackground)
                                .padding(24.dp)
                        ) {
                            if (authMode == AuthMode.LOGIN) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("WELCOME BACK", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black, fontSize = 28.sp)
                                        Text("PLEASE IDENTIFY YOURSELF", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    // Secure Access Badge
                                    Box(
                                        modifier = Modifier
                                            .rotate(5f)
                                            .background(KiskiRed)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("SECURE ACCESS", color = KiskiWhite, fontSize = 8.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            } else {
                                Text("NEW PROFILE", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black, fontSize = 28.sp)
                                Text("FILL ALL FIELDS TO PROCEED", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))

                            // Fields
                            Text("COLLEGE UID", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            BrutalistTextField(
                                value = uid, 
                                onValueChange = { uid = it }, 
                                placeholder = "Enter UID"
                            )
                            
                            if (authMode == AuthMode.REGISTER) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("FULL NAME", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                BrutalistTextField(value = name, onValueChange = { name = it }, placeholder = "Your Full Name")
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("COLLEGE MAIL", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                BrutalistTextField(value = email, onValueChange = { email = it }, placeholder = "id@cuchd.in")
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("PASSWORD", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                if (authMode == AuthMode.LOGIN) {
                                    Text("FORGOT?", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 10.sp, modifier = Modifier.clickable { authMode = AuthMode.FORGOT_PASSWORD })
                                }
                            }
                            BrutalistTextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = if (authMode == AuthMode.LOGIN) "******" else "******",
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            )

                            if (authMode == AuthMode.REGISTER) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("CONFIRM PASSWORD", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                BrutalistTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    placeholder = "******",
                                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                            Icon(
                                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                )
                            }

                            if (uiState is AuthUiState.Error) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    (uiState as AuthUiState.Error).message,
                                    color = KiskiRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            if (uiState is AuthUiState.Loading) {
                                CircularProgressIndicator(color = KiskiRed, modifier = Modifier.align(Alignment.CenterHorizontally))
                            } else {
                                BrutalistButton(
                                    text = if (authMode == AuthMode.LOGIN) "ENTER SYSTEM" else "CREATE ACCOUNT",
                                    onClick = {
                                        if (authMode == AuthMode.LOGIN) viewModel.login(uid, password)
                                        else viewModel.register(uid, name, email, password, confirmPassword)
                                    },
                                    containerColor = if (authMode == AuthMode.LOGIN) MaterialTheme.colorScheme.onBackground else KiskiRed,
                                    contentColor = if (authMode == AuthMode.LOGIN) MaterialTheme.colorScheme.background else KiskiWhite,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (authMode == AuthMode.LOGIN) "NEW TO THE NETWORK?" else "ALREADY IN THE SYSTEM?",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (authMode == AuthMode.LOGIN) "REGISTER NOW" else "LOGIN HERE",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.clickable { 
                                        authMode = if (authMode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN 
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class AuthMode {
    LOGIN, REGISTER, FORGOT_PASSWORD
}

@Composable
fun BrutalistTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 14.sp) },
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            focusedBorderColor = MaterialTheme.colorScheme.onSurface
        ),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontSize = 14.sp)
    )
}
