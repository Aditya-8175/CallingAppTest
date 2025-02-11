package com.aditya.callingfunctionality

import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.getSystemService
import com.aditya.callingfunctionality.ui.theme.CallingfunctionalityTheme

//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            CallingfunctionalityTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//
//                }
//            }
//        }
//    }
//}

//class MainActivity : ComponentActivity() {
//    companion object {
//        private const val REQUEST_CODE_ROLE_DIALER = 1001
//    }
//
//    @RequiresApi(Build.VERSION_CODES.Q)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            CallingfunctionalityTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                   Text(text = "hello Adita", modifier = Modifier.padding(innerPadding))
//                }
//            }
//        }
//
//        requestDefaultDialerRole()
//    }
//
//    @RequiresApi(Build.VERSION_CODES.Q)
//    private fun requestDefaultDialerRole() {
//        val roleManager = getSystemService(ROLE_SERVICE) as RoleManager
//        if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
//            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
//            startActivityForResult(intent, REQUEST_CODE_ROLE_DIALER)
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_CODE_ROLE_DIALER) {
//            if (resultCode == Activity.RESULT_OK) {
//                Toast.makeText(this, "App set as default dialer!", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(this, "Failed to set as default dialer", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//}


@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
    companion object {
        private const val REQUEST_CODE_DEFAULT_DIALER = 1001
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CallingfunctionalityTheme {
                Scaffold { innerPadding ->
                    RoleRequestScreen(
                        modifier = Modifier.padding(innerPadding),
                        onRequestRole = ::requestDefaultDialerRole
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestDefaultDialerRole() {
        val roleManager = getSystemService(ROLE_SERVICE) as RoleManager
        if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
//            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER).apply {
//                putExtra(
//                    TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE,
//                    getSystemService<TelecomManager>()?.defaultOutgoingPhoneAccount("tel")
//                )
//            }

            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)


            startActivityForResult(intent, REQUEST_CODE_DEFAULT_DIALER)
        } else {
            Toast.makeText(this, "Already default dialer", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_DEFAULT_DIALER -> handleRoleResult(resultCode)
        }
    }

    private fun handleRoleResult(resultCode: Int) {
        when (resultCode) {
            Activity.RESULT_OK -> showToast("Successfully set as default dialer")
            Activity.RESULT_CANCELED -> showToast("Failed to set as default dialer")
        }
    }

    @Composable
    private fun RoleRequestScreen(
        modifier: Modifier = Modifier,
        onRequestRole: () -> Unit
    ) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onRequestRole,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Set as Default Dialer", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
