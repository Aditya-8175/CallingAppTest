package com.aditya.callingfunctionality.screen.BottomNav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.aditya.callingfunctionality.calling.DialerScreen
import com.aditya.callingfunctionality.component.ConnectLogo
import com.aditya.callingfunctionality.component.ContactManager
import com.aditya.callingfunctionality.navigation.Routes
import com.aditya.callingfunctionality.screen.Connects.ConnectsScreen
import com.aditya.callingfunctionality.screen.tabScreens.recent.CallLogViewModel
import com.aditya.callingfunctionality.screen.Details.DetailScreen
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNav(
) {
    val local = LocalContext.current
    val bottomNavController = rememberNavController()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val contactManager = ContactManager(local)
    val callLogViewModel: CallLogViewModel = hiltViewModel()

    var showFilterScreen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            ConnectLogo(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xFFFFFFFF)),
                navController = bottomNavController
            )
        },


    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Routes.Connects.routes,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Tab Screens
            composable(Routes.Connects.routes) {
                ConnectsScreen(
                    contactManager,
                    bottomNavController,
                    onFilterClick = { showFilterScreen = true }
                )
            }
            composable(Routes.Details.routes) { DetailScreen(navController = bottomNavController) }
//

            composable(
                route = Routes.Dialer.routes + "?phoneNumber={phoneNumber}",
                arguments = listOf(
                    navArgument("phoneNumber") {
                        defaultValue = ""
                    }
                ),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "tel:{phoneNumber}" }
                )
            ) { backStackEntry ->
                val initialNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
                DialerScreen(
                    navController = bottomNavController,
                    initialPhoneNumber = initialNumber,
                    onCallInitiated = { phoneNumber ->
                        Timber.d("DialerScreen onCallInitiated: $phoneNumber")
                        bottomNavController.navigate("outgoingCall/$phoneNumber") {
                            popUpTo(Routes.Dialer.routes) { inclusive = false }
                        }
                    }
                )
            }


//
        }

        if (showFilterScreen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .blur(15.dp)
                    .zIndex(1f)
            )
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = { showFilterScreen = false },
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                scrimColor = Color.Transparent,
                dragHandle = null
            ) {
            }
        }
    }
}

@Composable
fun AppTabView(
    showIndicator: Boolean,
    selectedTabIndex: Int,
    onTabSelected: (index: Int, route: String) -> Unit
) {
    val tabs = listOf(
        "Recents" to Routes.Recents.routes,
        "Contacts" to Routes.Contacts.routes,
        "Favorites" to Routes.Favorites.routes
    )

    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color(0xFFFFFFFF),
        contentColor = Color(0xFF5864F8),
        indicator = if (showIndicator) { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = Color(0xFF5864F8)
                )
            }
        } else {
            {}
        }
    ) {
        tabs.forEachIndexed { index, (title, route) ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index, route) },
                text = { Text(title, color = Color(0xFF5864F8), fontSize = 14.sp, fontWeight = FontWeight(500)) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}