package com.example.kiskibreakkab.presentation.roomfinder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.kiskibreakkab.core.utils.TimeUtils
import com.example.kiskibreakkab.domain.model.Room

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RoomFinderScreen(
    onNavigateBack: () -> Unit,
    viewModel: RoomFinderViewModel = hiltViewModel()
) {
    val selectedDay by viewModel.selectedDay.collectAsState()
    val selectedSlot by viewModel.selectedSlot.collectAsState()
    val selectedBlock by viewModel.selectedBlock.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val availableBlocks by viewModel.availableBlocks.collectAsState()
    val userClaimedRoomId by viewModel.userClaimedRoomId.collectAsState()

    val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT")
    val slotNumbers = 1..8
    val blocks = availableBlocks

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text("ROOM FINDER", fontWeight = FontWeight.Black, fontSize = 24.sp)
                            Text(
                                "SEARCH AND CLAIM AVAILABLE TACTICAL SPACES.",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // User's Current Claim (Red Banner like website)
                userClaimedRoomId?.let { roomId ->
                    val claimedRoom = rooms.find { it.roomId == roomId }
                    if (claimedRoom != null) {
                        BrutalistCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = KiskiRed
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KiskiWhite, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "YOU'RE IN: ${claimedRoom.roomName}",
                                        color = KiskiWhite,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .background(KiskiWhite)
                                        .border(2.dp, MaterialTheme.colorScheme.onBackground)
                                        .clickable { viewModel.releaseRoom() }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = KiskiRed, modifier = Modifier.size(16.dp))
                                        Text("RELEASE", color = KiskiRed, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Selectors
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BrutalistDropdown(
                            label = "DAY",
                            options = days,
                            selectedOption = selectedDay,
                            onOptionSelected = { viewModel.selectDay(it) },
                            modifier = Modifier.weight(1f)
                        )
                        
                        BrutalistDropdown(
                            label = "TIME SLOT",
                            options = slotNumbers.map { 
                                val slot = TimeUtils.slots.find { s -> s.slotNumber == it }
                                "Slot $it - ${slot?.startTime} - ${slot?.endTime}"
                            },
                            selectedOption = run {
                                val slot = TimeUtils.slots.find { s -> s.slotNumber == selectedSlot }
                                "Slot $selectedSlot - ${slot?.startTime} - ${slot?.endTime}"
                            },
                            onOptionSelected = { 
                                val number = it.split(" ")[1].toInt()
                                viewModel.selectSlot(number) 
                            },
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                    
                    BrutalistDropdown(
                        label = "BUILDING BLOCK",
                        options = blocks,
                        selectedOption = selectedBlock ?: "SELECT BLOCK",
                        onOptionSelected = { viewModel.selectBlock(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Results Card
                BrutalistCard(modifier = Modifier.fillMaxWidth().weight(1f), backgroundColor = MaterialTheme.colorScheme.surface) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AVAILABLE ROOMS (${rooms.count { it.isAvailable }})", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = "REFRESH", 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Black, 
                            color = KiskiRed, 
                            modifier = Modifier
                                .border(1.dp, KiskiRed)
                                .clickable { viewModel.refreshRooms() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (rooms.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                                        .border(2.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("NO ROOMS AVAILABLE", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                                Text("Try changing filters or refreshing.", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(rooms) { room ->
                                RoomItem(
                                    room = room,
                                    onClaim = { 
                                        if (room.isAvailable) {
                                            viewModel.claimRoom(room.roomId) 
                                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrutalistDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            Box(
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .border(2.dp, MaterialTheme.colorScheme.onBackground)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedOption, fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface).border(2.dp, MaterialTheme.colorScheme.onBackground)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoomItem(room: Room, onClaim: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.onBackground)
            .background(if (room.isAvailable) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            .clickable(enabled = room.isAvailable, onClick = onClaim)
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = if (room.isAvailable) KiskiGreen else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(room.roomName.uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                
                Badge(
                    text = if (room.isAvailable) "FREE" else "OCCUPIED",
                    color = if (room.isAvailable) KiskiGreen else Color.Gray
                )
            }

            if (room.occupantNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CURRENTLY HERE (${room.occupantNames.size})", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    room.occupantNames.take(5).forEach { name ->
                        Box(
                            modifier = Modifier
                                .border(1.dp, MaterialTheme.colorScheme.onBackground)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(name, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (room.occupantNames.size > 5) {
                        Text("+${room.occupantNames.size - 5} MORE", fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }
    }
}
