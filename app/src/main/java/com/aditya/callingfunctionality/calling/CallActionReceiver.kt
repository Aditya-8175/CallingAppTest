package com.aditya.callingfunctionality.calling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RequiresApi

class CallActionReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onReceive(context: Context, intent: Intent) {

        val nextIntent = Intent(context, CallActivity::class.java)

        when (intent.action) {

            ACCEPT_CALL -> {
                val callIntent = CallActivity.getStartIntent(context, OngoingCall.call)
                callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(callIntent) // Launch UI
                OngoingCall.answer()

                // Update notification to show only Hang Up.
//                showNotification(context, fullScreen = false, call = OngoingCall.call)
            }

            DECLINE_CALL -> OngoingCall.hangup()
        }


    }
}


const val DECLINE_CALL = "decline_call"
const val ACCEPT_CALL =  "accept_call"



val Context.powerManager: PowerManager get() = getSystemService(Context.POWER_SERVICE) as PowerManager
