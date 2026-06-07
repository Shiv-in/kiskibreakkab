package com.example.kiskibreakkab.presentation.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.kiskibreakkab.domain.model.Squad
import com.example.kiskibreakkab.domain.model.TimetableSlot
import androidx.compose.material.icons.automirrored.filled.ArrowForward

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadScreen(
    onNavigateBack: () -> Unit,
    viewModel: SquadViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var squadName by remember { mutableStateOf("") }
    val selectedMemberIds = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text("SQUAD OPS", fontWeight = FontWeight.Black, fontSize = 24.sp, color = KiskiRed)
                            Text(
                                "COORDINATE TACTICAL UNITS AND GROUP ACTIVITIES.",
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
                // Mobilize Squad Section
                BrutalistCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(KiskiRed)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = KiskiWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("MOBILIZE SQUAD", color = KiskiWhite, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("SQUAD DESIGNATION", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                    BrutalistTextField(
                        value = squadName,
                        onValueChange = { squadName = it },
                        placeholder = "E.G. ALPHA TEAM",
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("SELECT OPERATIVES", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .border(2.dp, MaterialTheme.colorScheme.onBackground)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(8.dp)
                    ) {
                        if (uiState.availableFriends.isEmpty()) {
                            Text("NO FRIENDS AVAILABLE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(uiState.availableFriends) { friend ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (selectedMemberIds.contains(friend.userId)) {
                                                    selectedMemberIds.remove(friend.userId)
                                                } else {
                                                    selectedMemberIds.add(friend.userId)
                                                }
                                            }
                                            .padding(4.dp)
                                    ) {
                                        Checkbox(
                                            checked = selectedMemberIds.contains(friend.userId),
                                            onCheckedChange = null,
                                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.onSurface)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(friend.name.uppercase(), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        BrutalistButton(
                            text = "CREATE SQUAD",
                            onClick = { 
                                viewModel.createSquad(squadName, selectedMemberIds.toList())
                                squadName = ""
                                selectedMemberIds.clear()
                            },
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Active Units Section
                BrutalistCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.onBackground)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ACTIVE UNITS", color = MaterialTheme.colorScheme.background, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.background, modifier = Modifier.size(16.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (uiState.squads.isEmpty()) {
                        Text("NO ACTIVE UNITS", modifier = Modifier.padding(vertical = 16.dp), fontWeight = FontWeight.Bold, color = Color.Gray)
                    } else {
                        uiState.squads.forEach { squad ->
                            SquadItem(
                                squad = squad,
                                onViewIntel = { viewModel.viewSquadIntel(squad) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            // Intel Overlay (Common Free Time Dialog)
            if (uiState.selectedSquad != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        .clickable { viewModel.closeIntel() },
                    contentAlignment = Alignment.Center
                ) {
                    BrutalistCard(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .clickable(enabled = false) {},
                        backgroundColor = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().background(KiskiPurple).padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SQUAD INTEL: ${uiState.selectedSquad?.squadName}", color = KiskiWhite, fontWeight = FontWeight.Black, fontSize = 14.sp)
                            IconButton(onClick = { viewModel.closeIntel() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = KiskiWhite)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("COMMON FREE SLOTS", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        
                        if (uiState.isLoadingCommonSlots) {
                            CircularProgressIndicator(color = KiskiPurple, modifier = Modifier.padding(32.dp).align(Alignment.CenterHorizontally))
                        } else {
                            if (uiState.commonFreeSlots.isEmpty()) {
                                Text("NO COMMON FREE TIME DETECTED", modifier = Modifier.padding(vertical = 32.dp).align(Alignment.CenterHorizontally), fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
                            } else {
                                Box(modifier = Modifier.heightIn(max = 300.dp)) {
                                    LazyColumn {
                                        items(uiState.commonFreeSlots) { slot ->
                                            CommonSlotItem(slot)
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        BrutalistButton(text = "CLOSE INTEL", onClick = { viewModel.closeIntel() }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

@Composable
fun SquadItem(squad: Squad, onViewIntel: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.onBackground)
            .padding(12.dp)
    ) {
        Column {
            Text(squad.squadName.uppercase(), fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("ID: ${squad.squadId.take(6)}", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(2.dp, 16.dp).background(MaterialTheme.colorScheme.onBackground))
                Spacer(modifier = Modifier.width(8.dp))
                Text("MEMBERS: ${squad.memberIds.size}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
            TextButton(onClick = onViewIntel, modifier = Modifier.align(Alignment.End)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("VIEW INTEL", fontWeight = FontWeight.Black, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun CommonSlotItem(slot: TimetableSlot) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.onBackground)
            .background(KiskiGreen.copy(alpha = 0.2f))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(slot.day, fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("SLOT ${slot.slotNumber}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
        Text("${slot.startTime} - ${slot.endTime}", fontWeight = FontWeight.Black, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
