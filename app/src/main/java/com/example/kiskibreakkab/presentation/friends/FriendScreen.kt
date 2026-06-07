package com.example.kiskibreakkab.presentation.friends

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kiskibreakkab.core.components.*
import com.example.kiskibreakkab.core.theme.*
import com.example.kiskibreakkab.domain.model.FriendRequest
import com.example.kiskibreakkab.domain.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendScreen(
    onNavigateBack: () -> Unit,
    onViewSchedule: (String) -> Unit,
    viewModel: FriendViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchUid by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text("FRIEND ZONE", fontWeight = FontWeight.Black, fontSize = 24.sp, color = KiskiPurple)
                            Text(
                                "MANAGE SOCIAL CONNECTIONS AND NETWORK REQUESTS.",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Add Connection Section
                BrutalistCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(KiskiPurple)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = KiskiWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ADD CONNECTION", color = KiskiWhite, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("ENTER USER ID", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    BrutalistTextField(
                        value = searchUid,
                        onValueChange = { searchUid = it },
                        placeholder = "E.G. 22BCS12345",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = KiskiPurple, modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        BrutalistButton(
                            text = "SEND REQUEST",
                            onClick = { 
                                viewModel.sendFriendRequest(searchUid)
                                searchUid = ""
                            },
                            containerColor = if (searchUid.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    uiState.error?.let {
                        Text(it, color = KiskiRed, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    }
                    uiState.successMessage?.let {
                        Text(it, color = KiskiPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    }
                }

                // Incoming Requests Section
                BrutalistCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(KiskiLightGray)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("INCOMING (${uiState.incomingRequests.size})", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (uiState.incomingRequests.isEmpty()) {
                        Text(
                            "NO PENDING REQUESTS",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    } else {
                        uiState.incomingRequests.forEach { request ->
                            IncomingRequestItem(
                                request = request,
                                onAccept = { viewModel.acceptRequest(request) },
                                onReject = { viewModel.rejectRequest(request.requestId) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                // Established Connections Section
                BrutalistCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.onBackground)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ESTABLISHED CONNECTIONS", color = MaterialTheme.colorScheme.background, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("TOTAL: ${uiState.friends.size}", color = MaterialTheme.colorScheme.background, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (uiState.friends.isEmpty()) {
                        Text("NO FRIENDS YET", modifier = Modifier.padding(vertical = 16.dp), fontWeight = FontWeight.Bold, color = Color.Gray)
                    } else {
                        uiState.friends.forEach { friend ->
                            FriendItem(
                                friend = friend,
                                onViewSchedule = { onViewSchedule(friend.userId) },
                                onTerminate = { viewModel.removeFriend(friend.userId) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IncomingRequestItem(
    request: FriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, KiskiBlack)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(KiskiRed)
                    .border(2.dp, KiskiBlack),
                contentAlignment = Alignment.Center
            ) {
                Text(request.senderName.take(1).uppercase(), color = KiskiWhite, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(request.senderName.uppercase(), fontWeight = FontWeight.Black, fontSize = 14.sp)
                Text(request.senderUid, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
            IconButton(onClick = onAccept) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KiskiPurple)
            }
            IconButton(onClick = onReject) {
                Icon(Icons.Default.Cancel, contentDescription = null, tint = KiskiRed)
            }
        }
    }
}

@Composable
fun FriendItem(
    friend: User,
    onViewSchedule: () -> Unit,
    onTerminate: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, KiskiBlack)
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(KiskiRed)
                        .border(2.dp, KiskiBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Text(friend.name.take(1).uppercase(), color = KiskiWhite, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(friend.name.uppercase(), fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text(friend.uid, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = KiskiBlack, modifier = Modifier.padding(vertical = 4.dp), thickness = 1.dp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onViewSchedule) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("VIEW SCHEDULE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = KiskiBlack)
                    }
                }
                TextButton(onClick = onTerminate) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PersonRemove, contentDescription = null, modifier = Modifier.size(14.dp), tint = KiskiRed)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TERMINATE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = KiskiRed)
                    }
                }
            }
        }
    }
}
