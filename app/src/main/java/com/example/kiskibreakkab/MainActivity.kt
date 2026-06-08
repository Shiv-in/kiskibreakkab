package com.example.kiskibreakkab

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.kiskibreakkab.presentation.auth.AuthScreen
import com.example.kiskibreakkab.presentation.auth.LandingScreen
import com.example.kiskibreakkab.presentation.auth.SplashScreen
import com.example.kiskibreakkab.presentation.dashboard.DashboardScreen
import com.example.kiskibreakkab.presentation.timetable.TimetableScreen
import com.example.kiskibreakkab.presentation.friends.FriendScreen
import com.example.kiskibreakkab.presentation.friends.FriendScheduleScreen
import com.example.kiskibreakkab.presentation.groups.SquadScreen
import com.example.kiskibreakkab.presentation.roomfinder.RoomFinderScreen
import com.example.kiskibreakkab.presentation.profile.ProfileScreen
import com.example.kiskibreakkab.presentation.MainViewModel
import com.example.kiskibreakkab.core.navigation.Screen
import com.example.kiskibreakkab.core.theme.KiskiBreakKabTheme
import com.example.kiskibreakkab.core.theme.KiskiRed
import com.example.kiskibreakkab.core.theme.KiskiWhite
import com.example.kiskibreakkab.core.utils.FriendStatusWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        scheduleFriendStatusChecks()

        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()
            val user by mainViewModel.currentUser.collectAsState()
            val isAuthReady by mainViewModel.isAuthReady.collectAsState()

            KiskiBreakKabTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showTopBar = currentRoute != Screen.Landing.route && currentRoute != Screen.Auth.route && currentRoute != Screen.Splash.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (showTopBar) {
                            KiskiTopBar(
                                userName = user?.name ?: "P",
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { mainViewModel.toggleTheme() },
                                onProfileClick = { navController.navigate(Screen.Profile.route) }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Splash.route) {
                            SplashScreen(
                                isReady = isAuthReady,
                                currentUser = user,
                                onNavigateToDashboard = {
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                },
                                onNavigateToLanding = {
                                    navController.navigate(Screen.Landing.route) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Screen.Landing.route) {
                            LandingScreen(
                                onGetStarted = { navController.navigate(Screen.Auth.route) },
                                onLogin = { navController.navigate(Screen.Auth.route) },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { mainViewModel.toggleTheme() }
                            )
                        }
                        composable(Screen.Auth.route) {
                            AuthScreen(
                                onAuthSuccess = {
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Auth.route) { inclusive = true }
                                    }
                                },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { mainViewModel.toggleTheme() }
                            )
                        }
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(
                                onNavigateToTimetable = { navController.navigate(Screen.Timetable.route) },
                                onNavigateToFriends = { navController.navigate(Screen.Friends.route) },
                                onNavigateToGroups = { navController.navigate(Screen.Groups.route) },
                                onNavigateToRoomFinder = { navController.navigate(Screen.RoomFinder.route) }
                            )
                        }
                        composable(Screen.Timetable.route) { 
                            TimetableScreen(onNavigateBack = { navController.popBackStack() }) 
                        }
                        composable(Screen.Friends.route) { 
                            FriendScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onViewSchedule = { friendId -> 
                                    navController.navigate(Screen.FriendSchedule.createRoute(friendId))
                                }
                            ) 
                        }
                        composable(Screen.FriendSchedule.route) { 
                            FriendScheduleScreen(onNavigateBack = { navController.popBackStack() })
                        }
                        composable(Screen.Groups.route) { 
                            SquadScreen(onNavigateBack = { navController.popBackStack() })
                        }
                        composable(Screen.RoomFinder.route) {
                            RoomFinderScreen(onNavigateBack = { navController.popBackStack() })
                        }
                        composable(Screen.Profile.route) {
                            ProfileScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onLogout = {
                                    navController.navigate(Screen.Auth.route) {
                                        popUpTo(0) { inclusive = true }
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

private fun MainActivity.scheduleFriendStatusChecks() {
    val request = PeriodicWorkRequestBuilder<FriendStatusWorker>(1, TimeUnit.HOURS)
        .build()
    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "friend_status_check",
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KiskiTopBar(
    userName: String,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onProfileClick: () -> Unit
) {
    var showNotifications by remember { mutableStateOf(false) }

    Column {
        TopAppBar(
            title = {
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
                        Text("KBK", color = KiskiWhite, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            },
            actions = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TopBarActionIconButton(
                        if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                        onClick = onToggleTheme
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TopBarActionIconButton(
                        Icons.Default.NotificationsNone,
                        onClick = { showNotifications = true }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.onBackground)
                            .border(2.dp, MaterialTheme.colorScheme.onBackground)
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            userName.take(1).uppercase(),
                            color = MaterialTheme.colorScheme.background,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
        HorizontalDivider(thickness = 3.dp, color = KiskiRed)
    }

    if (showNotifications) {
        AlertDialog(
            onDismissRequest = { showNotifications = false },
            title = { Text("NOTIFICATIONS", fontWeight = FontWeight.Black) },
            text = { 
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• No new tactical alerts.", fontSize = 14.sp)
                    Text("• System is synchronized.", fontSize = 14.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotifications = false }) {
                    Text("CLOSE", fontWeight = FontWeight.Bold, color = KiskiRed)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun TopBarActionIconButton(icon: ImageVector, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(Color.Transparent)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.onBackground,
                shape = RectangleShape
            )
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(20.dp)
        )
    }
}
