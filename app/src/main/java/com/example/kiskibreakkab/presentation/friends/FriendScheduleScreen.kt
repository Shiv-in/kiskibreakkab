package com.example.kiskibreakkab.presentation.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.kiskibreakkab.core.utils.TimeUtils
import com.example.kiskibreakkab.presentation.timetable.SlotCell
import com.example.kiskibreakkab.presentation.timetable.TableCell

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendScheduleScreen(
    onNavigateBack: () -> Unit,
    viewModel: FriendScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val days = TimeUtils.WEEK_DAYS
    val slotNumbers = 1..8

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(uiState.friend?.name?.uppercase() ?: "FRIEND", fontWeight = FontWeight.Black, fontSize = 24.sp, color = KiskiPurple)
                            Text(
                                "TACTICAL AVAILABILITY LOG: ${uiState.friend?.uid ?: "SCANNING..."}",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = KiskiWhite)
                )
                HorizontalDivider(thickness = 3.dp, color = KiskiBlack)
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            GridBackground()
            
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = KiskiBlack)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BrutalistCard(modifier = Modifier.fillMaxWidth()) {
                        Text("MISSION STATUS: ${if (uiState.timetable.isEmpty()) "UNCONFIGURED" else "READY"}", fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Grid
                        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            Column(modifier = Modifier.border(2.dp, KiskiBlack)) {
                                // Header Row
                                Row(modifier = Modifier.background(KiskiLightGray)) {
                                    TableCell(text = "DAY", isHeader = true)
                                    slotNumbers.forEach { num ->
                                        TableCell(text = "S$num", isHeader = true)
                                    }
                                }

                                // Day Rows
                                days.forEach { day ->
                                    Row {
                                        TableCell(text = day, isHeader = true)
                                        slotNumbers.forEach { slotNum ->
                                            val slot = uiState.timetable.find { it.day == day && it.slotNumber == slotNum }
                                            SlotCell(
                                                isFree = slot?.isFree ?: true,
                                                location = slot?.location,
                                                onClick = { /* Read only */ },
                                                onLongClick = { /* Read only */ }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    BrutalistButton(
                        text = "CLOSE LOG",
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
