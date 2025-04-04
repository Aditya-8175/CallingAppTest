package com.aditya.callingfunctionality.screen.Connects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aditya.callingfunctionality.component.ContactManager
import com.aditya.callingfunctionality.component.SearchForm
import com.aditya.callingfunctionality.navigation.Routes
import com.aditya.callingfunctionality.screen.BottomNav.AppTabView
import com.surefy.connects.presentation.screen.tabScreens.ContactScreen
import com.surefy.connects.presentation.screen.tabScreens.FavouriteScreen
import com.aditya.callingfunctionality.screen.tabScreens.RecentScreens
import com.aditya.callingfunctionality.screen.tabScreens.recent.CallLogViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectsScreen(
//    selectedTabIndex: Int,
    contactManager: ContactManager,
//    context: Activity,
    navController: NavController,
    onFilterClick: () -> Unit
) {

    val callLogViewModel: CallLogViewModel = hiltViewModel()

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val showIndicator = currentRoute == Routes.Connects.routes

    var searchQuery by remember { mutableStateOf("") }



    Box(modifier = Modifier.fillMaxSize()) {



        Column(modifier = Modifier
//                .fillMaxSize()
            .background(color = Color(0xFFFFFFFF))
        ) {


            SearchForm(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onFilterClick = onFilterClick)
            { query ->
                searchQuery = query
            }

            AppTabView(
                selectedTabIndex = selectedTabIndex,
                showIndicator = showIndicator,
                onTabSelected = { index, route ->
                    selectedTabIndex = index

                }
            )

            when (selectedTabIndex) {
                0 ->   RecentScreens(viewModel = callLogViewModel,  navController,searchQuery )

                1 -> ContactScreen( )
                2 -> FavouriteScreen()
                else ->  RecentScreens( viewModel = callLogViewModel,  navController = navController, searchQuery )

            }

        }



    }

}
