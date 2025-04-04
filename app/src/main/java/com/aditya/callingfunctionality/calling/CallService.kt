package com.aditya.callingfunctionality.calling

import android.app.KeyguardManager
import android.os.Build
import android.os.PowerManager
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.aditya.callingfunctionality.calling.speaker.CallManager
import com.aditya.callingfunctionality.showNotification


class CallService : InCallService() {

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        super.onCreate()

    }



    private val callCallback = object : Call.Callback() {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onStateChanged(call: Call, state: Int) {

            if (call.details.callDirection == Call.Details.DIRECTION_INCOMING) {
                when (state) {
                    Call.STATE_RINGING -> {
//                        showNotification(this@CallService, fullScreen = true, call = call)
                        updateNotification(fullScreen = false, call = call)

                    }
                    Call.STATE_ACTIVE -> {
                        showNotification(this@CallService, fullScreen = false, call = call)
                    }
                    Call.STATE_DISCONNECTED  -> {
                        NotificationManagerCompat.from(this@CallService).cancel(12)
                    }
                }
            }
        }
    }





    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)

        CallManager.onCallAdded(call)
        CallManager.inCallService = this

        OngoingCall.call = call
        call.registerCallback(callCallback)

        when (call.details.callDirection) {
            Call.Details.DIRECTION_INCOMING -> handleIncomingCall(call)
            Call.Details.DIRECTION_OUTGOING -> handleOutgoingCall(call)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun handleIncomingCall(call: Call) {
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val isScreenLocked = keyguardManager.isDeviceLocked
        val isNonInteractive = !powerManager.isInteractive

        if (isScreenLocked || isNonInteractive) {
            try {
                showNotification(this, true, call, forceLowPriority = false)

//                startActivity(CallActivity.getStartIntent(this))
                CallActivity.start(this, call)

            } catch (e: Exception) {
//                showNotification(this, true, call, forceLowPriority = true)
                showNotification(this,  false, call = call, false )

            }
//            updateNotification(fullScreen = true, call = call)
//            showNotification(this, true, call, forceLowPriority = true)
        } else {
            showNotification(this,  false, call = call )
        }


    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun handleOutgoingCall(call: Call) {
        // Immediately launch the outgoing call UI
        CallActivity.start(this, call)
        showNotification(this, false, call, forceLowPriority = true)

//        updateNotification(fullScreen = false, call = call)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updateNotification(fullScreen: Boolean, call: Call) {
        val notification = showNotification(this, fullScreen, call)

//        NotificationManagerCompat.from(this).cancel(12)
//        NotificationManagerCompat.from(this).notify(12, notification)
        NotificationManagerCompat.from(this)

    }


    override fun onCallRemoved(call: Call) {

        super.onCallRemoved(call)

        call.unregisterCallback(callCallback)
        CallManager.onCallRemoved(call)

        OngoingCall.call = null

        NotificationManagerCompat.from(this).cancel(12)


    }

    @Deprecated("Deprecated in Java")
    override fun onCallAudioStateChanged(audioState: CallAudioState?) {
        super.onCallAudioStateChanged(audioState)
        if (audioState != null) {
            CallManager.onAudioStateChanged(audioState)
        }
    }

}

