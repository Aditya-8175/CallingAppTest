package com.aditya.callingfunctionality.calling


import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.TelecomManager
import android.telecom.VideoProfile
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.aditya.callingfunctionality.calling.speaker.AudioRoute
import com.aditya.callingfunctionality.calling.speaker.CallManager
import com.aditya.callingfunctionality.calling.speaker.CallManagerListener
import com.aditya.callingfunctionality.calling.speaker.NoCall
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class CallActivity : ComponentActivity() {


    private val disposables = CompositeDisposable()
    private lateinit var number: String
    private var contactName: String? = null

    private var contactPhotoUri: String? = null

    private var isIncomingCall = false


    private var proximityWakeLock: PowerManager.WakeLock? = null
    private var screenOnWakeLock: PowerManager.WakeLock? = null

    private var isSpeakerOn = false
    private var isMicrophoneOff = false
    private var isCallEnded = false


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        number = intent.getStringExtra("PHONE_NUMBER") ?: ""
        val callDirection = intent.getStringExtra("CALL_DIRECTION") ?: "unknown"

        isIncomingCall = callDirection == "incoming"


        val (name, photoUri) = getContactDetails(this, number)
        contactName = name
        contactPhotoUri = photoUri

        isIncomingCall = callDirection == "incoming"

        val callInfo = arrayListOf(
            "$number",
            "${contactName ?: "$number"}",
            "${contactPhotoUri}",
            "$callDirection",
            "NEW"
        )


        window.decorView.post {
            window.decorView.requestFocus()
            window.decorView.keepScreenOn = true
        }

        addLockScreenFlags()



        if (CallManager.getPhoneState() == NoCall) {
            finish()
            return
        }


        val audioManager = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_CALL
        addLockScreenFlags()
        CallManager.addListener(callCallback)

        setContent {
            var callState by remember { mutableStateOf(Call.STATE_NEW) }

            DisposableEffect(Unit) {
                val disposable = OngoingCall.state.subscribe { state ->
                    callState = state
                    when (state) {

                        Call.STATE_DIALING -> {
                            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
                            enableProximitySensor()
                        }

                        Call.STATE_ACTIVE, Call.STATE_RINGING -> {
                            enableProximitySensor()
                        }
                        Call.STATE_DISCONNECTED -> {
                            disableProximitySensor()
                            disposables.add(
                                io.reactivex.Observable.timer(1, TimeUnit.SECONDS)
                                    .subscribe { finish() }
                            )
                        }
                        else -> {
                            // For other call states, decide to keep the sensor enabled or disable it.
                            // disableProximitySensor() // if needed
                        }
                    }
                }



                onDispose {
                    disableProximitySensor()
                    disposable.dispose()
                }
            }



            CallScreenMain(
                isIncoming = isIncomingCall,
                callInfo = callInfo,
                onAnswerClick = { OngoingCall.answer() },
                onHangupClick = { OngoingCall.hangup() },
                isSpeakerOn = isSpeakerOn,
                onSpeakerToggle = { toggleSpeaker() }
            )

        }
    }


    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        CallManager.removeListener(callCallback)
    }

    private val callCallback = object : CallManagerListener {
        override fun onStateChanged() {
            updateState()
        }

        override fun onAudioStateChanged(audioState: AudioRoute) {
            updateCallAudioState(audioState)
        }

        override fun onPrimaryCallChanged(call: Call) {
            updateState()
        }
    }


    private fun updateState() {

        updateCallAudioState(CallManager.getCallAudioRoute())
    }

    private fun updateCallAudioState(route: AudioRoute?) {
        route?.let {
            isSpeakerOn = route == AudioRoute.SPEAKER
            if (isSpeakerOn) {
                disableProximitySensor()
            } else {
                enableProximitySensor()
            }
        }
    }

    private fun toggleSpeaker() {
        val newRoute = if (isSpeakerOn) {
            CallAudioState.ROUTE_WIRED_OR_EARPIECE
        } else {
            CallAudioState.ROUTE_SPEAKER
        }
        CallManager.setAudioRoute(newRoute)
    }



    @SuppressLint("NewApi")
    private fun addLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).requestDismissKeyguard(this, null)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }

        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            screenOnWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "com.surefy.connects:full_wake_lock")
            screenOnWakeLock!!.acquire(5 * 1000L)
        } catch (e: Exception) {
        }
    }

    private fun enableProximitySensor() {
        if ( (proximityWakeLock == null || proximityWakeLock?.isHeld == false)) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            proximityWakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "com.surefy.connects:wake_lock")
            proximityWakeLock!!.acquire(60 * 60 * 1000L)
        }
    }

    private fun disableProximitySensor() {
        if (proximityWakeLock?.isHeld == true) {
            proximityWakeLock!!.release()
        }
    }

    companion object {

        @RequiresApi(Build.VERSION_CODES.Q)
        fun getStartIntent(context: Context, call: Call? = null): Intent {
//            val openAppIntent = Intent(context, CallActivity::class.java)
//            openAppIntent.flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
//            return openAppIntent
            return Intent(context, CallActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
//                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
//                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP

                // Add this for Android 10+ behavior
                putExtra(TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE, VideoProfile.STATE_AUDIO_ONLY)

                call?.let {
                    val callDirection = when (call.details.callDirection) {
                        Call.Details.DIRECTION_INCOMING -> "incoming"
                        Call.Details.DIRECTION_OUTGOING -> "outgoing"
                        else -> "unknown"
                    }
                    putExtra("PHONE_NUMBER", call.details.handle.schemeSpecificPart)
                    putExtra("CALL_DIRECTION", callDirection)

                }
            }

        }



        @RequiresApi(Build.VERSION_CODES.Q)
        fun start(context: Context, call: Call) {
            context.startActivity(getStartIntent(context, call))
        }

//        @RequiresApi(Build.VERSION_CODES.Q)
//         fun start(context: Context, call: Call) {
//
//            val callDirection = when (call.details.callDirection) {
//                Call.Details.DIRECTION_INCOMING -> "incoming"
//                Call.Details.DIRECTION_OUTGOING -> "outgoing"
//                else -> "unknown"
//            }
//
////            Intent(context, CallActivity::class.java).apply {
////                flags = Intent.FLAG_ACTIVITY_NEW_TASK
////                data = call.details.handle
////            }.also { context.startActivity(it) }
//
//            Intent(context, CallActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                putExtra("PHONE_NUMBER", call.details.handle.schemeSpecificPart)
//                putExtra("CALL_DIRECTION", callDirection)
//            }.also { context.startActivity(it) }
//
//        }

        fun getContactDetails(context: Context, phoneNumber: String): Pair<String?, String?> {

            val uri: Uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            val projection = arrayOf(
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.PHOTO_URI
            )

            var contactName: String? = null
            var contactPhotoUri: String? = null

            val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    contactName = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
                    contactPhotoUri = it.getString(it.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_URI))
                }
            }
            return Pair(contactName, contactPhotoUri)
        }

//        fun getContactDetails(context: Context, phoneNumber: String): Pair<String?, String?> {
//            // Trim and sanitize the input
//            val sanitizedNumber = phoneNumber.trim()
//            if (sanitizedNumber.isEmpty()) {
//                Timber.w("No valid phone number provided; skipping contact lookup")
//                return Pair(null, null)  // Or return Pair(null, null) if you prefer a non-null Pair
//            }
//
//            // Build the lookup URI using the sanitized number
//            val uri: Uri = Uri.withAppendedPath(
//                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
//                Uri.encode(sanitizedNumber)
//            )
//            val projection = arrayOf(
//                ContactsContract.PhoneLookup.DISPLAY_NAME,
//                ContactsContract.PhoneLookup.PHOTO_URI
//            )
//
//            var contactName: String? = null
//            var contactPhotoUri: String? = null
//
//            try {
//                context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
//                    if (cursor.moveToFirst()) {
//                        contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME))
//                        contactPhotoUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.PHOTO_URI))
//                    }
//                }
//            } catch (e: Exception) {
//                Timber.e(e, "Failed to query contact for $sanitizedNumber")
//            }
//
//            return Pair(contactName, contactPhotoUri)
//        }


    }

}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun CallScreenMain(
    isIncoming: Boolean,
    callInfo: ArrayList<String>,
    onAnswerClick: () -> Unit,
    onHangupClick: () -> Unit,

    isSpeakerOn: Boolean,
    onSpeakerToggle: () -> Unit
) {
    val context = LocalContext.current

    var callState by remember { mutableStateOf(Call.STATE_NEW) }



    DisposableEffect(Unit) {
        val disposable = OngoingCall.state.subscribe { state -> callState = state }
        onDispose { disposable.dispose() }
    }


    when {
        isIncoming && callState == Call.STATE_RINGING -> {
            IncomingCallScreen(
                callInfo = callInfo,
                onAnswerClick = { onAnswerClick() },
                onHangupClick = { onHangupClick() }
            )
        }
        !isIncoming || callState == Call.STATE_RINGING ||callState == Call.STATE_ACTIVE
            -> {
            OutgoingCallScreen(
                callInfo = callInfo,
                onHangupClick = { onHangupClick() },

                isSpeakerOn = isSpeakerOn,
                onSpeakerToggle = onSpeakerToggle

            )

        }

    }
}