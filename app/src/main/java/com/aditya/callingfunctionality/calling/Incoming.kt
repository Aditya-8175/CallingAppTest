package com.aditya.callingfunctionality.calling

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aditya.callingfunctionality.R


//@Preview(showSystemUi = true, device = "id:pixel_8")
@Composable
fun IncomingCallScreen(
    callInfo: ArrayList<String>,
    onAnswerClick: () -> Unit,
    onHangupClick: () -> Unit
) {
    val context = LocalContext.current
    val profileUri = callInfo.getOrNull(2)
    var showMessageDialog by remember { mutableStateOf(false) }

    val quickResponses = listOf(
        "I'm busy now, call me later.",
        "Call me after some time.",
        "I'll call you back soon."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Incoming...",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = callInfo[1],
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 80.dp)
        )
        Text(
            text = "personal",
            fontSize = 16.sp,
            color = Color(0xFF5864F8),
            modifier = Modifier.padding(bottom = 40.dp)
        )

        if (!profileUri.isNullOrEmpty()) {
            AsyncImage(
                model = profileUri,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                ,
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_personal),
                contentDescription = "Default Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                ,
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = callInfo[0],
            fontSize = 18.sp,
            fontWeight = FontWeight.Light,
            color = Color(0xFF808080),
            modifier = Modifier.padding(top = 24.dp)
        )
        Text(
            text = callInfo[1],
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Airtel - Thane Maharashtra",
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 100.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.decline_call),
                contentDescription = "Decline Call",
                modifier = Modifier.size(56.dp).clickable {
                    onHangupClick.invoke()
                }
            )
            Image(
                painter = painterResource(id = R.drawable.incall_msg),
                contentDescription = "Message",
                modifier = Modifier.size(56.dp).clickable {
                    showMessageDialog = true
                }
            )
            Image(
                painter = painterResource(id = R.drawable.pick_up_call),
                contentDescription = "Accept Call",
                modifier = Modifier.size(56.dp).clickable {
                    onAnswerClick.invoke()
                }
            )
        }
    }

    if (showMessageDialog) {
        AlertDialog(
            onDismissRequest = { showMessageDialog = false },
            title = { Text(text = "Quick Responses") },
            text = {
                Column {
                    quickResponses.forEach { message ->
                        Text(
                            text = message,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    sendSMS(context, callInfo[1], message)
                                    onHangupClick.invoke()
                                    showMessageDialog = false
                                }
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }
}

fun sendSMS(context: Context, phoneNumber: String, message: String) {
    try {
        val smsIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("smsto:$phoneNumber")
            putExtra("sms_body", message)
        }
        context.startActivity(smsIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to send message", Toast.LENGTH_SHORT).show()
    }
}





