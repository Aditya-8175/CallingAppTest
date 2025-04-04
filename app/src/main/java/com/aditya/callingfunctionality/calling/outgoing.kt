
package com.aditya.callingfunctionality.calling

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aditya.callingfunctionality.R
import com.aditya.callingfunctionality.calling.speaker.CallManager
import kotlinx.coroutines.delay


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OutgoingCallScreen(
    callInfo: ArrayList<String>,
    onHangupClick: () -> Unit,
    isSpeakerOn: Boolean,
    onSpeakerToggle: () -> Unit
) {
    val profileUri = callInfo.getOrNull(2)
    var callStartTime by remember { mutableStateOf<Long?>(null) }
    var callDuration by remember { mutableStateOf("Calling...") }
    var isMessageSectionVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Observe call state
    DisposableEffect(Unit) {
        val disposable = OngoingCall.state
            .subscribe { state ->
                if (state == android.telecom.Call.STATE_ACTIVE && callStartTime == null) {
                    callStartTime = System.currentTimeMillis()
                }
            }
        onDispose { disposable.dispose() }
    }

    // Update call duration every second
    LaunchedEffect(callStartTime) {
        while (callStartTime != null) {
            val elapsedTime = (System.currentTimeMillis() - callStartTime!!) / 1000
            val minutes = elapsedTime / 60
            val seconds = elapsedTime % 60
            callDuration = String.format("%02d:%02d", minutes, seconds)
            delay(1000) // Update every second
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(top = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Call duration
        Text(callDuration, fontSize = 14.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(40.dp))

        // Recipient name and type
        Text(callInfo[1], fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Personal", fontSize = 14.sp, color = Color.Blue)
        Spacer(modifier = Modifier.height(12.dp))

        // Profile picture
        if (!profileUri.isNullOrEmpty()) {
            AsyncImage(
                model = profileUri,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                        contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_personal),
                contentDescription = "Default Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                        contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(callInfo[0], fontSize = 18.sp, fontWeight = FontWeight.Medium)
        Text(callInfo[1], fontSize = 16.sp, fontWeight = FontWeight.Light)
        Spacer(modifier = Modifier.height(40.dp))

        // Call Actions
        CallActions(
            onHangupClick = onHangupClick,
            recipientNumber = callInfo[1],
            onMessageClick = { /* Define what happens when message button is clicked */ },
            isSpeakerOn = isSpeakerOn,
            onSpeakerToggle = onSpeakerToggle

        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CallActions(
    onHangupClick: () -> Unit,
    recipientNumber: String,
    onMessageClick: () -> Unit,

    isSpeakerOn: Boolean,
    onSpeakerToggle: () -> Unit
) {
    val context = LocalContext.current
    var isMuted by remember { mutableStateOf(false) } // State to track mute status
//    var isSpeakerOn by remember { mutableStateOf(false) } // State to track speaker status
    val isSpeakerOn = isSpeakerOn
    var isDialpadVisible by remember { mutableStateOf(false) } // State to track dialpad visibility
    var enteredDigits by remember { mutableStateOf("") } // State to track entered digits in dialpad

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Show entered digits when dialpad is open
        if (isDialpadVisible) {
            Text(
                text = enteredDigits.ifEmpty { "Enter Number" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Row for Mute, Message, and Dialpad buttons
        if (!isDialpadVisible) {
            Row(
                modifier = Modifier.padding(25.dp),
                horizontalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                // Mute Button
                CallActionButton(
                    icon = R.drawable.mute_call,
                    label = "Mute",
                    backgroundColor = if (isMuted) Color.Green else Color(0xFFF3F4FF)
                ) {
                    isMuted = !isMuted // Toggle mute state
                    toggleMute(context, isMuted) // Mute or unmute the microphone
                }

                // Message Button
                CallActionButton(icon = R.drawable.msg_call, label = "Message") {
                    onMessageClick()
                }

                // Dialpad Button
                CallActionButton(icon = R.drawable.dialpad_call, label = "Keypad") {
                    isDialpadVisible = true // Open dialpad
                }
            }

            // Row for Speaker, Add Call, and End Call buttons
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(40.dp)
            ) {


                // Add Call Button
                CallActionButton(icon = R.drawable.add_call, label = "Add") {
                    openPhoneAppHome(context) // Open phone app
                }

                // call merge visible only during multiple call
                CallActionButton(icon = R.drawable.call_merge, label = "Merge") {
                   CallManager.merge()
                }
                // call swap  visible only during multiple call
                CallActionButton(icon = R.drawable.swap_calls, label = "Swap") {
                   CallManager.swap()
                }

            }

            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                // Speaker Button
                CallActionButton(
                    icon = R.drawable.speaker_call,
                    label = "Speaker",
                    backgroundColor = if (isSpeakerOn) Color.Green else Color(0xFFF3F4FF)
                ) {
                    onSpeakerToggle()
                }

                CallActionButton(icon = R.drawable.add_call, label = "Add") {
                    openPhoneAppHome(context)
                }

                CallActionButton(
                    icon = R.drawable.decline_call,
                    label = "End",
                    backgroundColor = Color.Red,
                    constantBackground = true
                ) {
                    onHangupClick.invoke()
                }
            }
        }

        if (isDialpadVisible) {
            Dialpad(
                onDigitPress = { digit ->
                    enteredDigits += digit
                    sendDTMFTone(context, digit.first()) // Send DTMF tone
                },
                onClose = { isDialpadVisible = false }, // Close dialpad
                onHangupClick = onHangupClick // End call
            )
        }
    }
}

// Function to toggle mute state
fun toggleMute(context: Context, mute: Boolean) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    try {
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isMicrophoneMute = mute
        Log.d("MuteState", "Microphone muted: $mute")
    } catch (e: Exception) {
        Log.e("MuteState", "Failed to toggle mute: ${e.message}")
    }
}

// Fallback solution to block microphone
private var mediaRecorder: MediaRecorder? = null

fun blockMicrophone(context: Context) {
    try {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile("/dev/null") // Discard audio data
            prepare()
            start()
        }
        Log.d("MicrophoneBlock", "Microphone blocked")
    } catch (e: Exception) {
        Log.e("MicrophoneBlock", "Failed to block microphone: ${e.message}")
    }
}

fun unblockMicrophone() {
    try {
        mediaRecorder?.stop()
        mediaRecorder?.release()
        mediaRecorder = null
        Log.d("MicrophoneBlock", "Microphone unblocked")
    } catch (e: Exception) {
        Log.e("MicrophoneBlock", "Failed to unblock microphone: ${e.message}")
    }
}

// Function to force audio mode (speaker or earpiece)
fun forceAudioMode(context: Context, isSpeakerOn: Boolean) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
    audioManager.isSpeakerphoneOn = isSpeakerOn
    Log.d("AudioMode", "Speakerphone on: $isSpeakerOn")
}

// Function to send DTMF tones
fun sendDTMFTone(context: Context, digit: Char) {
    val call = OngoingCall.call
    if (call == null) {
        println("No active call found.")
        return
    }

    try {
        call.playDtmfTone(digit) // Play DTMF tone
        call.stopDtmfTone() // Stop after a short delay
        println("DTMF tone sent: $digit")
    } catch (e: Exception) {
        println("Error sending DTMF tone: ${e.message}")
    }
}
@Composable
fun Dialpad(
    onDigitPress: (String) -> Unit,
    onClose: () -> Unit,
    onHangupClick: () -> Unit
) {
    val digits = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("*", "0", "#")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dialpad buttons with reduced padding
        for (row in digits) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp), // Reduced spacing
                modifier = Modifier.padding(vertical = 4.dp) // Less vertical padding
            ) {
                for (digit in row) {
                    Button(
                        onClick = { onDigitPress(digit) },
                        modifier = Modifier
                            .size(60.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F3F3))
                    ) {
                        Text(
                            text = digit,
                            fontSize = 24.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Row for Back & End Call buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(90.dp), // Adjusted spacing
            modifier = Modifier.padding(top = 4.dp) // Reduced top padding
        ) {
            // Back Button (below #)
            CallActionButton(icon = R.drawable.dialpad_call, label = "Keypad") {
                onClose() // Close dialpad
            }

            // End Call Button (below 0)
            CallActionButton(
                icon = R.drawable.decline_call,
                label = "End",
                backgroundColor = Color.Red,
                constantBackground = true // Keeps color unchanged
            ) {
                onHangupClick.invoke()
            }
        }
    }
}
fun openPhoneAppHome(context: Context) {
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_APP_CONTACTS) // Opens the Phone app (Call Logs & Contacts)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
}

@Composable
fun CallActionButton(
    icon: Int,
    label: String,
    backgroundColor: Color = Color(0xFFF3F4FF),
    constantBackground: Boolean = false, // New parameter
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) } // State to track button press

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable {
            isPressed = !isPressed
            onClick.invoke()
        }
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = if (constantBackground) backgroundColor else if (isPressed) Color(0xFF5864F8) else backgroundColor
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.padding(12.dp),
                colorFilter = if (!constantBackground && isPressed) ColorFilter.tint(Color.White) else null
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (!constantBackground && isPressed) Color(0xFF5864F8) else Color(0xFF5864F8)
        )
    }
}