package com.example.kiskibreakkab.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.kiskibreakkab.core.components.*
import com.example.kiskibreakkab.core.theme.*
import com.example.kiskibreakkab.domain.model.Room
import com.example.kiskibreakkab.domain.model.TimetableSlot
import com.example.kiskibreakkab.domain.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToTimetable: () -> Unit,
    onNavigateToFriends: () -> Unit,
    onNavigateToGroups: () -> Unit,
    onNavigateToRoomFinder: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val userData by viewModel.userData.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val currentDay by viewModel.currentDay.collectAsState()
    val currentSlot by viewModel.currentSlot.collectAsState()
    val friendsFree by viewModel.friendsFreeNow.collectAsState()
    val roomsFree by viewModel.freeRooms.collectAsState()
    val squadCount by viewModel.squadCount.collectAsState()
    val friendCount by viewModel.friendCount.collectAsState()

    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)) {
        GridBackground()
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Welcome Banner
            item {
                WelcomeBanner(userName = userData?.name ?: "USER")
            }

            // 2. Free Rooms Widget
            item {
                FreeRoomsWidget(roomsFree, currentSlot)
            }

            // 3. Friends Free Now
            item {
                FriendsFreeWidget(friendsFree)
            }

            // 4. Room Finder
            item {
                RoomFinderWidget(onNavigateToRoomFinder)
            }

            // 5. Status Monitor
            item {
                StatusMonitorWidget(currentDay, currentSlot?.slotNumber?.toString() ?: "-", currentTime)
            }

            // 6. Control Panel
            item {
                ControlPanelWidget(onNavigateToTimetable, onNavigateToFriends, onNavigateToGroups)
            }

            // 7. Network Stats
            item {
                NetworkStatsWidget(friendCount, squadCount)
            }

            // 8. Student ID
            item {
                StudentIdCard(userData)
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun WelcomeBanner(userName: String) {
    BrutalistCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        shadowOffset = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "WELCOME BACK,",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${userName.uppercase()}!",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 32.sp
                    )
                }
                Column(){

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "BREAK",
                        color = KiskiRed,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 24.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                    Text(
                    text = "TIME",
                    color = KiskiRed,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 24.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )}
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(2.dp, 20.dp).background(KiskiRed))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "SYSTEM STATUS: READY TO SYNC",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun FreeRoomsWidget(rooms: List<Room>, slot: TimetableSlot?) {
    val slotText = if (slot != null) {
        "SLOT ${slot.slotNumber} • ${slot.startTime} - ${slot.endTime}"
    } else {
        "NO ACTIVE SLOT"
    }

    BrutalistHeaderCard(
        title = "FREE ROOMS",
        icon = Icons.Default.DoorSliding,
        headerContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Badge("LIVE", KiskiRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text(slotText, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        },
        trailingHeaderContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(rooms.size.toString(), fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                Text("EMPTY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        }
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (rooms.isEmpty()) {
               Text("NO ROOMS DISCOVERED", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            } else {
                rooms.take(2).forEach { room ->
                    RoomChip(room)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("${rooms.size} ROOMS EMPTY", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Gray)
    }
}

@Composable
fun RowScope.RoomChip(room: Room) {
    Box(
        modifier = Modifier
            .weight(1f)
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(room.roomName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black, fontSize = 14.sp)
        }
    }
}

@Composable
fun FriendsFreeWidget(friends: List<User>) {
    BrutalistHeaderCard(
        title = "FRIENDS FREE NOW",
        icon = Icons.Default.People
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .border(2.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.NotInterested, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("NO ONE IS FREE", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text("Everyone is busy attending classes.", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun RoomFinderWidget(onSearch: () -> Unit) {
    BrutalistHeaderCard(
        title = "ROOM FINDER",
        icon = Icons.Default.Place,
        trailingHeaderContent = {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.clickable { onSearch() })
        }
    ) {
        Text("Search & Claim available rooms using the Room Finder module.", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        BrutalistButton(
            text = "OPEN FINDER",
            onClick = onSearch,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun StatusMonitorWidget(day: String, slot: String, time: String) {
    BrutalistHeaderCard(
        title = "STATUS MONITOR",
        trailingHeaderContent = {
            // Updated to be black on white for high contrast as requested
            Badge("LIVE: $time", KiskiWhite, textColor = KiskiBlack)
        }
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatusBox("CURRENT DAY", day, Modifier.weight(1f))
            StatusBox("SLOT NO.", slot, Modifier.weight(1f), isRed = true)
            StatusBox("LIVE TIME", time, Modifier.weight(1.5f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        BrutalistButton(
            text = "WEEKEND", 
            onClick = {}, 
            containerColor = KiskiPurple,
            contentColor = KiskiWhite,
            shadowColor = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun StatusBox(label: String, value: String, modifier: Modifier, isRed: Boolean = false) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
            .border(2.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (isRed) KiskiRed else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ControlPanelWidget(onTimetable: () -> Unit, onFriends: () -> Unit, onGroups: () -> Unit) {
    BrutalistHeaderCard(title = "CONTROL PANEL") {
        ControlItem(Icons.Default.CalendarToday, "EDIT TIMETABLE", onTimetable)
        ControlItem(Icons.Default.People, "MANAGE FRIENDS", onFriends)
        ControlItem(Icons.Default.TrackChanges, "MANAGE GROUPS", onGroups)
    }
}

@Composable
fun ControlItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(2.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun NetworkStatsWidget(friends: Int, squads: Int) {
    BrutalistHeaderCard(title = "NETWORK STATS") {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(friends.toString(), fontSize = 42.sp, fontWeight = FontWeight.Black, color = KiskiRed)
                Text("FRIENDS", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.width(2.dp).height(60.dp).background(MaterialTheme.colorScheme.onBackground))
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(squads.toString(), fontSize = 42.sp, fontWeight = FontWeight.Black, color = KiskiRed)
                Text("GROUPS", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StudentIdCard(user: User?) {
    BrutalistHeaderCard(
        title = "MY STUDENT ID",
        icon = Icons.Default.Shield,
        headerColor = KiskiBlue,
        headerContent = {
            Text("UID: ${user?.uid ?: "NOT FOUND"}", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("CLASS SECTION", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(user?.section ?: "23BCS_FS-601", color = KiskiBlue, fontSize = 32.sp, fontWeight = FontWeight.Black)
            
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 2.dp)
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("LAB GROUP", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(user?.labGroup ?: "B", color = MaterialTheme.colorScheme.onSurface, fontSize = 32.sp, fontWeight = FontWeight.Black)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("VERIFIED RECORD • 2026", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BrutalistHeaderCard(
    title: String,
    icon: ImageVector? = null,
    headerColor: Color = MaterialTheme.colorScheme.onSurface,
    headerContent: @Composable () -> Unit = {},
    trailingHeaderContent: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    BrutalistCard(
        backgroundColor = MaterialTheme.colorScheme.background,
        shadowOffset = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.onBackground, RoundedCornerShape(12.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.background)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Column {
                        Text(title, fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                        headerContent()
                    }
                }
                trailingHeaderContent()
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.onBackground)
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 16.dp),
                content = content
            )
        }
    }
}
