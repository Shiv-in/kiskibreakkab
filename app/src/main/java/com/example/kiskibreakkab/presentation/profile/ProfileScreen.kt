package com.example.kiskibreakkab.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kiskibreakkab.core.components.*
import com.example.kiskibreakkab.core.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val context = LocalContext.current
    
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importRoomsFromPdf(it, context)
        }
    }
    
    var name by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var labGroup by remember { mutableStateOf("") }
    var roomsInput by remember { mutableStateOf("") }

    LaunchedEffect(uiState.user) {
        uiState.user?.let {
            name = it.name
            section = it.section
            labGroup = it.labGroup
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("COMMAND PROFILE", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                HorizontalDivider(thickness = 3.dp, color = MaterialTheme.colorScheme.onBackground)
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.background)) {
            GridBackground()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Tactical Identity
                BrutalistCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(KiskiRed)
                                .border(3.dp, MaterialTheme.colorScheme.onBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(uiState.user?.name?.take(1)?.uppercase() ?: "?", color = KiskiWhite, fontSize = 32.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("ACTIVE OPERATIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text(uiState.user?.uid ?: "SCANNING...", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(uiState.user?.email ?: "", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                // Edit Fields
                BrutalistCard(modifier = Modifier.fillMaxWidth()) {
                    Text("TACTORIAL UPDATES", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("FULL NAME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    BrutalistTextField(value = name, onValueChange = { name = it }, placeholder = "Full Name")
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SECTION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            BrutalistTextField(value = section, onValueChange = { section = it }, placeholder = "FS-601")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("LAB GROUP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            BrutalistTextField(value = labGroup, onValueChange = { labGroup = it }, placeholder = "B")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        BrutalistButton(
                            text = "SAVE CHANGES",
                            onClick = { viewModel.updateProfile(name, section, labGroup) },
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background
                        )
                    }
                }

                // Admin Override Section
                if (isAdmin) {
                    BrutalistCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.surface
                    ) {
                        Text("COMMAND OVERRIDE (ADMIN)", fontWeight = FontWeight.Black, fontSize = 14.sp, color = KiskiRed)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("BULK ROOM IMPORT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Format: RoomName, Block, Department", fontSize = 8.sp, color = Color.Gray)
                        
                        OutlinedTextField(
                            value = roomsInput,
                            onValueChange = { roomsInput = it },
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            placeholder = { Text("610, B6, CSE\n101, B1, ME", color = Color.Gray, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                focusedBorderColor = KiskiRed
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        BrutalistButton(
                            text = "DEPLOY UNITS",
                            onClick = { 
                                viewModel.importRooms(roomsInput)
                                roomsInput = ""
                            },
                            containerColor = KiskiRed,
                            contentColor = KiskiWhite,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("OR SELECT DOCUMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                        BrutalistButton(
                            text = "IMPORT FROM PDF",
                            onClick = { pdfPickerLauncher.launch("application/pdf") },
                            containerColor = MaterialTheme.colorScheme.onSurface,
                            contentColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Success/Error Messages
                uiState.importProgress?.let {
                    BrutalistCard(modifier = Modifier.fillMaxWidth(), backgroundColor = KiskiGreen.copy(alpha = 0.1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = KiskiGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(it, color = KiskiGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                uiState.successMessage?.let {
                    Text(it, color = KiskiGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                uiState.error?.let {
                    Text(it, color = KiskiRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Actions
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Logout Action
                    BrutalistButton(
                        text = "TERMINATE SESSION (LOGOUT)",
                        onClick = { 
                            viewModel.logout()
                            onLogout()
                        },
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Delete Action
                    var showDeleteDialog by remember { mutableStateOf(false) }
                    
                    BrutalistButton(
                        text = "PURGE ACCOUNT (DELETE)",
                        onClick = { showDeleteDialog = true },
                        containerColor = KiskiRed,
                        contentColor = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("CONFIRM PURGE", fontWeight = FontWeight.Black) },
                            text = { Text("This will permanently delete your tactical data and account. This action cannot be undone.") },
                            confirmButton = {
                                TextButton(onClick = { 
                                    viewModel.deleteAccount(onLogout)
                                    showDeleteDialog = false
                                }) {
                                    Text("PURGE", color = KiskiRed, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) {
                                    Text("CANCEL", color = MaterialTheme.colorScheme.onSurface)
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            textContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
