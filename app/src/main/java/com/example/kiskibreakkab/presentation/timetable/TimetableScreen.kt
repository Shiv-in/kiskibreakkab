package com.example.kiskibreakkab.presentation.timetable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kiskibreakkab.core.components.*
import com.example.kiskibreakkab.core.theme.*

import com.example.kiskibreakkab.core.utils.TimeUtils
import com.example.kiskibreakkab.domain.model.TimetableSlot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    onNavigateBack: () -> Unit,
    viewModel: TimetableViewModel = hiltViewModel()
) {
    val timetable by viewModel.timetable.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val userUid by viewModel.userUid.collectAsState()
    val days = listOf("MON", "TUE", "WED", "THU", "FRI","SAT")
    val slotNumbers = 1..8

    var showLocationDialog by remember { mutableStateOf(false) }
    var selectedSlotInfo by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var locationInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("TIME TABLE", fontWeight = FontWeight.Black, fontSize = 24.sp)
                        Text(
                            "CONFIGURE YOUR AVAILABILITY MODULE. MARK SLOTS AS 'FREE' TO ENABLE SOCIAL SYNCHRONIZATION.",
                            fontSize = 8.sp,
                            lineHeight = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                modifier = Modifier.drawBehind {
                    val strokeWidth = 3.dp.toPx()
                    val y = size.height - strokeWidth / 2
                    drawLine(
                        color = Color.Black,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(MaterialTheme.colorScheme.background)) {
            GridBackground()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BrutalistCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("WEEKLY CONFIGURATION", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).background(KiskiGreen).border(1.dp, MaterialTheme.colorScheme.onBackground))
                            Text(" FREE ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Box(modifier = Modifier.size(12.dp).background(KiskiRed).border(1.dp, MaterialTheme.colorScheme.onBackground))
                            Text(" BUSY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Text("Tap slots to toggle status.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Grid Header and Table
                    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        Column(modifier = Modifier.border(2.dp, MaterialTheme.colorScheme.onBackground)) {
                            // Header Row
                            Row(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                                TableCell(text = "DAY", isHeader = true)
                                slotNumbers.forEach { num ->
                                    val time = TimeUtils.slots.find { it.slotNumber == num }?.startTime ?: ""
                                    TableCell(text = "S$num\n$time", isHeader = true)
                                }
                            }

                            // Day Rows
                            days.forEach { day ->
                                Row {
                                    TableCell(text = day, isHeader = true)
                                    slotNumbers.forEach { slotNum ->
                                        val slot = timetable.find { it.day == day && it.slotNumber == slotNum }
                                        SlotCell(
                                            isFree = slot?.isFree ?: true,
                                            location = slot?.location,
                                            onClick = { viewModel.toggleSlot(day, slotNum) },
                                            onLongClick = {
                                                selectedSlotInfo = day to slotNum
                                                locationInput = slot?.location ?: ""
                                                showLocationDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = MaterialTheme.colorScheme.onBackground)
                } else {
                    BrutalistButton(
                        text = "CONFIRM CHANGES",
                        onClick = { viewModel.saveChanges() },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = MaterialTheme.colorScheme.background
                    )
                }

                // Bottom System ID info (like in screenshot)
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SYSTEM ID: $userUid", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text("SECURE CONNECTION ESTABLISHED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
    }

    if (showLocationDialog && selectedSlotInfo != null) {
        LocationTagDialog(
            day = selectedSlotInfo!!.first,
            slot = selectedSlotInfo!!.second,
            initialLocation = locationInput,
            onConfirm = { loc ->
                viewModel.updateSlotLocation(selectedSlotInfo!!.first, selectedSlotInfo!!.second, loc)
                showLocationDialog = false
            },
            onDismiss = { showLocationDialog = false }
        )
    }
}

@Composable
fun LocationTagDialog(
    day: String,
    slot: Int,
    initialLocation: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialLocation) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) {
                Text("SAVE", fontWeight = FontWeight.Bold, color = KiskiRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = MaterialTheme.colorScheme.onSurface)
            }
        },
        title = { Text("TAG LOCATION (S$slot - $day)", fontWeight = FontWeight.Black) },
        text = {
            Column {
                Text("Where are you sitting during this free slot?", fontSize = 10.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                BrutalistTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = "e.g. Library, Cafe, M-713"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        shape = RectangleShape
    )
}

@Composable
fun TableCell(text: String, isHeader: Boolean = false) {
    Box(
        modifier = Modifier
            .size(70.dp, 50.dp)
            .border(1.dp, MaterialTheme.colorScheme.onBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = if (isHeader) 10.sp else 12.sp,
            fontWeight = if (isHeader) FontWeight.Black else FontWeight.Bold,
            lineHeight = 12.sp,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SlotCell(
    isFree: Boolean, 
    location: String? = null, 
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(70.dp, 50.dp)
            .background(if (isFree) KiskiGreen else KiskiRed)
            .border(1.dp, MaterialTheme.colorScheme.onBackground)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                if (isFree) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isFree) KiskiBlack else KiskiWhite
            )
            Text(
                text = if (isFree) "FREE" else "BUSY",
                color = if (isFree) KiskiBlack else KiskiWhite,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black
            )
            if (isFree && !location.isNullOrBlank()) {
                Text(
                    text = "📍$location",
                    color = KiskiBlack.copy(alpha = 0.7f),
                    fontSize = 6.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}
