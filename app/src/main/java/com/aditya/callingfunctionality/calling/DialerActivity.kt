package com.aditya.callingfunctionality.calling


import android.Manifest
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.aditya.callingfunctionality.R
import com.aditya.callingfunctionality.navigation.Routes
import com.aditya.callingfunctionality.ui.theme.CallingfunctionalityTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


class DialerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get phone number from intent data, if available
        val initialNumber = intent?.data?.schemeSpecificPart ?: ""

        setContent {

            CallingfunctionalityTheme  {

                val navController = rememberNavController()
                DialerScreen(
                    navController = navController,
                    initialPhoneNumber = initialNumber,
                    onCallInitiated = { phoneNumber ->
                        // Navigate to OutgoingCallScreen or take further action

                        navController.navigate("outgoingCall/$phoneNumber") {
                            popUpTo(Routes.Dialer.routes)
                            { inclusive = false }
                        }

                    }


                )

            }

        }
    }
}

@Composable
fun DialerScreen(
    navController: NavHostController,
    initialPhoneNumber: String = "",
    onCallInitiated: (String) -> Unit
) {
    val context = LocalContext.current
    var phoneNumber by remember { mutableStateOf(initialPhoneNumber) }
    var cursorPosition by remember { mutableStateOf(initialPhoneNumber.length) }
    var isCursorVisible by remember { mutableStateOf(true) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var isDeleting by remember { mutableStateOf(false) }
    val clipboardManager = LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    var showOptionsMenu by remember { mutableStateOf(false) }
    var pastePosition by remember { mutableIntStateOf(0) }
    var menuOffset by remember { mutableStateOf(Offset.Zero) }

    // SIM card selection state
    var simInfo by remember { mutableStateOf(emptyList<Pair<String, Int>>()) }
    var selectedSimId by remember { mutableIntStateOf(-1) }
    var contactName by remember { mutableStateOf<String?>(null) }

    val density = LocalDensity.current
    val screenWidth = remember {
        with(density) { context.resources.displayMetrics.widthPixels.toDp() }
    }
    val buttonSize = (screenWidth / 4).coerceAtMost(70.dp) // Adjust button size dynamically

    Timber.d("DialerScreen launched with initialPhoneNumber: $initialPhoneNumber")

    // Cursor blinking effect
    DisposableEffect(Unit) {
        val job = coroutineScope.launch {
            while (true) {
                delay(500)
                isCursorVisible = !isCursorVisible
            }
        }
        onDispose {
            job.cancel()
        }
    }

    // Handle back press
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Timber.d("Back pressed in DialerScreen")
//                navController.popBackStack()

                if (!navController.popBackStack()) {
                    navController.navigate(Routes.Recents.routes)
                    {
                        // Optionally, clear any intermediate destinations
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    }
                }



            }

        }
        backDispatcher?.addCallback(lifecycleOwner, callback)
        onDispose {
            callback.remove()
        }
    }

    // Fetch SIM card information
    LaunchedEffect(Unit) {
        val sims = getSimInfo(context)
        simInfo = sims
        if (sims.isNotEmpty()) {
            selectedSimId = sims[0].second
        }
        Timber.d("SIM info loaded: $simInfo, selectedSimId: $selectedSimId")
    }

    // Update contact name based on the current phone number
    LaunchedEffect(phoneNumber) {
        contactName = if (phoneNumber.isNotEmpty()) {
            context.getContactName(phoneNumber)
        } else {
            null
        }
        Timber.d("Contact name updated: $contactName for $phoneNumber")
    }

    // Function to make a call using the selected SIM card
    fun initiateCall(phoneNumber: String, subscriptionId: Int) {
        val encodedPhoneNumber = phoneNumber.replace("#", "%23")
        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$encodedPhoneNumber")
            putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", subscriptionId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Timber.d("Starting call intent for $encodedPhoneNumber with SIM ID: $subscriptionId")
                context.startActivity(callIntent)
                OngoingCall.simulateCallState(Call.STATE_RINGING)
                onCallInitiated(phoneNumber) // Trigger navigation to OutgoingCallScreen
            } else {
                Timber.w("CALL_PHONE permission denied")
//                Toast.makeText(context, "Permission denied!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to initiate call for $phoneNumber")
//            Toast.makeText(context, "Failed to start call", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // SIM Card Selection UI at the top
        if (simInfo.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                simInfo.forEachIndexed { index, (simName, subscriptionId) ->
                    SimSelectionButton(
                        text = simName,
                        isSelected = selectedSimId == subscriptionId,
                        shape = RoundedCornerShape(
                            topStart = if (index == 0) 8.dp else 0.dp,
                            bottomStart = if (index == 0) 8.dp else 0.dp,
                            topEnd = if (index == simInfo.size - 1) 8.dp else 0.dp,
                            bottomEnd = if (index == simInfo.size - 1) 8.dp else 0.dp
                        )
                    ) { selectedSimId = subscriptionId }
                }
            }
        }

        // Rest of the UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = if (simInfo.size > 1) 60.dp else 0.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Phone Number Input Field with Copy-Paste menu
            Box {
                Column {
                    if (showOptionsMenu) {
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false },
                            offset = DpOffset(
                                with(density) { menuOffset.x.toDp() },
                                (-40).dp
                            )
                        ) {
                            DropdownMenuItem(
                                text = { Text("Copy") },
                                onClick = {
                                    val clipData = ClipData.newPlainText("PhoneNumber", phoneNumber)
                                    clipboardManager.setPrimaryClip(clipData)
                                    Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                    showOptionsMenu = false
                                }
                            )
                            clipboardManager.primaryClip?.getItemAt(0)?.text?.let { copiedText ->
                                DropdownMenuItem(
                                    text = { Text("Paste") },
                                    onClick = {
                                        val newText = phoneNumber.substring(0, pastePosition) + copiedText + phoneNumber.substring(pastePosition)
                                        if (newText.length <= 15) {
                                            phoneNumber = newText
                                            cursorPosition = pastePosition + copiedText.length
                                        } else {
                                            Toast.makeText(context, "Cannot paste, exceeds 15 characters!", Toast.LENGTH_SHORT).show()
                                        }
                                        showOptionsMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .background(Color.White)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { offset ->
                                        textLayoutResult?.let { layout ->
                                            cursorPosition = layout.getOffsetForPosition(offset).coerceIn(0, phoneNumber.length)
                                        }
                                    },
                                    onLongPress = { offset ->
                                        textLayoutResult?.let { layout ->
                                            pastePosition = layout.getOffsetForPosition(offset).coerceIn(0, phoneNumber.length)
                                        }
                                        menuOffset = Offset(offset.x, offset.y - 50)
                                        showOptionsMenu = true
                                    }
                                )
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = phoneNumber.take(cursorPosition) +
                                        (if (isCursorVisible) "|" else " ") +
                                        phoneNumber.drop(cursorPosition),
                                fontSize = 28.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                onTextLayout = { textLayoutResult = it }
                            )
                        }
                        if (phoneNumber.isNotEmpty()) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(35.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                isDeleting = true
                                                coroutineScope.launch {
                                                    while (isDeleting && cursorPosition > 0) {
                                                        phoneNumber = phoneNumber.removeRange(cursorPosition - 1, cursorPosition)
                                                        cursorPosition--
                                                        delay(200)
                                                    }
                                                }
                                                tryAwaitRelease()
                                                isDeleting = false
                                            }
                                        )
                                    }
                            ) {
                                Text(text = "⌫", fontSize = 26.sp, color = Color.Black)
                            }
                        }
                    }
                }
            }

            if (phoneNumber.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionIcon("Add To Existing") {
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            type = ContactsContract.Contacts.CONTENT_TYPE
                        }
                        context.startActivity(intent)
                    }
                    HorizontalDivider(modifier = Modifier.height(35.dp).width(1.dp), color = Color(0xFFD9D9D9))
                    ActionIcon("Add New") {
                        val addContactIntent = Intent(Intent.ACTION_INSERT).apply {
                            type = "vnd.android.cursor.dir/contact"
                            putExtra("phone", phoneNumber)
                        }
                        context.startActivity(addContactIntent)
                    }
                    HorizontalDivider(modifier = Modifier.height(35.dp).width(1.dp), color = Color(0xFFD9D9D9))
                    ActionIcon("Send Message") {
                        val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("sms:$phoneNumber")
                        }
                        context.startActivity(smsIntent)
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                val buttons = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("*", "0", "#")
                )

                buttons.forEach { row ->
                    Row {
                        row.forEach { digit ->
                            DialButton(number = digit, buttonSize = buttonSize) { input ->
                                phoneNumber = phoneNumber.substring(0, cursorPosition) + input + phoneNumber.substring(cursorPosition)
                                cursorPosition++
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (phoneNumber.isNotEmpty()) {
                            Timber.d("Call button clicked with phoneNumber: $phoneNumber")
                            initiateCall(phoneNumber, selectedSimId)
                        } else {
                            Timber.w("Phone number is empty; call not initiated")
                            Toast.makeText(context, "Enter a phone number", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5864F8)),
                    modifier = Modifier.size(buttonSize)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

fun makeCall(context: Context, phoneNumber: String, subscriptionId: Int) {
    val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)

    // Encode special characters like #
    val encodedPhoneNumber = phoneNumber.replace("#", "%23")
    val callUri = Uri.parse("tel:$encodedPhoneNumber")

    // Create the call intent
    val callIntent = Intent(Intent.ACTION_CALL).apply {
        data = callUri
    }

    // Find the PhoneAccountHandle for the selected SIM
    val phoneAccountHandle = subscriptionManager?.activeSubscriptionInfoList
        ?.find { it.subscriptionId == subscriptionId }
        ?.let { subscriptionInfo ->
            telecomManager.callCapablePhoneAccounts?.find {
                it.id.contains(subscriptionInfo.subscriptionId.toString())
            }
        }

    // Set the PhoneAccountHandle for the selected SIM
    if (phoneAccountHandle != null) {
        callIntent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandle)
    }

    // Check for CALL_PHONE permission
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
        == PackageManager.PERMISSION_GRANTED
    ) {
        context.startActivity(callIntent)
    } else {
        Toast.makeText(context, "Permission denied!", Toast.LENGTH_SHORT).show()
    }
}

// Dial Pad Buttons
@Composable
fun DialButton(number: String, buttonSize: Dp, onClick: (String) -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(12.dp)
            .size(buttonSize)
            .clip(CircleShape)
            .background(Color(0xFFF3F4FF)) // New Blue Color
            .clickable { onClick(number) }
    ) {
        Text(text = number, fontSize = 28.sp, color = Color.Black)
    }
}

// Action Icons
@Composable
fun ActionIcon(text: String, onClick: () -> Unit) {
    val iconRes = when (text) {
        "Add To Existing" -> R.drawable.add_to_existing
        "Add New" -> R.drawable.addcontact
        "Send Message" -> R.drawable.send_message
        else -> null
    }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        iconRes?.let {
            Image(
                painter = painterResource(id = it),
                contentDescription = text,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF171D1D)
        )
    }
}

// SIM Selection Button
@Composable
fun SimSelectionButton(
    text: String,
    isSelected: Boolean,
    shape: Shape,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(shape)
            .background(if (isSelected) Color(0xFF5864F8) else Color(0xFFF3F4FF))
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Black,
            modifier = Modifier.padding(8.dp),
            fontSize = 20.sp // Increased text size
        )
    }
}

// Function to get SIM card information
fun getSimInfo(context: Context): List<Pair<String, Int>> {
    val simInfoList = mutableListOf<Pair<String, Int>>()
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
        val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
        subscriptionManager?.activeSubscriptionInfoList?.forEach { subscriptionInfo ->
            val carrierName = subscriptionInfo.carrierName.toString()
            val shortenedName = shortenCarrierName(carrierName)
            simInfoList.add(Pair(shortenedName, subscriptionInfo.subscriptionId))
        }
    }
    return simInfoList
}

// Function to shorten the carrier name
fun shortenCarrierName(carrierName: String): String {
    return when {
        carrierName.contains("BSNL") -> "BSNL"
        carrierName.contains("Jio") -> "Jio"
        carrierName.contains("Airtel") -> "Airtel"
        carrierName.contains("Vodafone") -> "Vodafone"
        carrierName.contains("Idea") -> "Idea"
        // Add more conditions as needed
        else -> carrierName.take(10) // Default to first 10 characters if no match
    }


}

// Function to get contact name from phone number
//fun Context.getContactName(phoneNumber: String): String? {
//    val uri = Uri.withAppendedPath(
//        ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
//        Uri.encode(phoneNumber)
//    )
//    val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
//    contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
//        if (cursor.moveToFirst()) {
//            return cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
//        }
//    }
//    return null
//}
//

fun Context.getContactName(phoneNumber: String): String? {
    if (phoneNumber.isBlank()) {
        Timber.w("Phone number is blank; skipping contact lookup")
        return null
    }
    if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
        Timber.w("READ_CONTACTS permission not granted")
        return null
    }
    try {
        val sanitizedPhoneNumber = phoneNumber.trim().replace("[^0-9+]".toRegex(), "") // Sanitize input
        if (sanitizedPhoneNumber.isEmpty()) {
            Timber.w("Sanitized phone number is empty; skipping lookup")
            return null
        }
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(sanitizedPhoneNumber)
        )
        Timber.d("Querying contact URI: $uri")
        contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
                Timber.d("Found contact name: $name for $sanitizedPhoneNumber")
                return name
            }
        }
        Timber.d("No contact found for $sanitizedPhoneNumber")
        return null
    } catch (e: Exception) {
        Timber.e(e, "Failed to query contact for $phoneNumber")
        return null
    }
}