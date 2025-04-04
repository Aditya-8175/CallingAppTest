package com.aditya.callingfunctionality


import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.telecom.TelecomManager.ACTION_CHANGE_DEFAULT_DIALER
import android.telecom.TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.aditya.callingfunctionality.navigation.NavGraph
import com.aditya.callingfunctionality.navigation.Routes
import com.aditya.callingfunctionality.screen.BottomNav.BottomNav
import com.aditya.callingfunctionality.ui.theme.CallingfunctionalityTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CallingfunctionalityTheme {
                val navController = rememberNavController()
//                NavGraph(navController)
//PermissionRequestScreen(navController)


                    val context = LocalContext.current
                    val requiredPermissions = arrayOf(
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.WRITE_CALL_LOG,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_PHONE_STATE
                    )
                    var permissionsGranted by remember { mutableStateOf(false) }

                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions ->
                        permissionsGranted = permissions.values.all { it }
                    }

                    LaunchedEffect(Unit) {
                        val allGranted = requiredPermissions.all {
                            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                        }
                        if (allGranted) {
                            permissionsGranted = true
                        } else {
                            launcher.launch(requiredPermissions)
                        }
                        requestDefaultDialer(context)
                    }

                    if (permissionsGranted) {
                        BottomNav()

//        LaunchedEffect(Unit ) {
//
//                navController.navigate(Routes.BottomNav.routes)
//            }
//            {
//                popUpTo(Routes.PersonalActivity.routes) { inclusive = true }
//            }
//        }
//        navController.navigate(Routes.BottomNav.routes)
                    } else {

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Box(
                                modifier = Modifier.size(height = 450.dp, width = 400.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.permission_img),
                                    contentDescription = "Permission",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
//        CircularProgressIndicator()
                    }





            }
        }
    }
}


private fun requestDefaultDialer(context: Context) {
    val telecomManager = context.getSystemService(TelecomManager::class.java)
    if (telecomManager?.defaultDialerPackage != context.packageName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                val REQUEST_DEFAULT_DIALER = 1
                (context as? Activity)?.startActivityForResult(intent, REQUEST_DEFAULT_DIALER)
            }
        } else {
            val intent = Intent(ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
            }
            context.startActivity(intent)
        }
    }
}