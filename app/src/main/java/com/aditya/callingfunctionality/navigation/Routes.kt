package com.aditya.callingfunctionality.navigation

sealed class Routes (val routes: String) {

    // main
    object Recents : Routes("recents")
    object Contacts : Routes("contacts")
    object Favorites : Routes("favorites")

    object Connects : Routes("connects")
    object Details : Routes("details")

    object BottomNav : Routes("bottom_nav")


    object PermissionRequest : Routes("permissionRequest")
    object Dialer : Routes("dialer")


}