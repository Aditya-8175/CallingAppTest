package com.aditya.callingfunctionality.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aditya.callingfunctionality.screen.BottomNav.BottomNav

@Composable
fun NavGraph(navController: NavHostController) {

    NavHost(navController = navController, startDestination = Routes.PermissionRequest.routes) {

        composable(Routes.BottomNav.routes){
            BottomNav()
        }
        composable(Routes.PermissionRequest.routes){
//            PermissionRequestScreen(navController)
        }


    }

}