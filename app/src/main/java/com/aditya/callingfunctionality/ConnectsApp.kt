
package com.aditya.callingfunctionality


import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.telecom.Call
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aditya.callingfunctionality.calling.ACCEPT_CALL
import com.aditya.callingfunctionality.calling.CallActionReceiver
import com.aditya.callingfunctionality.calling.CallActivity
import com.aditya.callingfunctionality.calling.DECLINE_CALL
import dagger.hilt.android.HiltAndroidApp


const val URGENT = "urgent"
const val URGENT_NAME = "urgent_name"

@HiltAndroidApp
class ConnectsApp: Application() {

    override fun onCreate() {
        super.onCreate()
    }
}


@SuppressLint("NewApi")
@RequiresApi(Build.VERSION_CODES.Q)
fun showNotification(
    context: Context,
    fullScreen: Boolean = false,
    call: Call? = null,
    forceLowPriority: Boolean = false
) {
    val customView = RemoteViews(context.packageName, R.layout.custom_notification)

    val expandView = RemoteViews(context.packageName, R.layout.custom_notification)


    val isOutgoing = call?.details?.callDirection == Call.Details.DIRECTION_OUTGOING

    val isActive =  call?.details?.state == Call.STATE_ACTIVE


    if (call == null) {
        return
    } else {


        val phoneNumber = call.details.handle.schemeSpecificPart
        val (name, photoUri) = CallActivity.getContactDetails(context, phoneNumber)


//            customView.setTextViewText(R.id.tvContactName, candidate?: phoneNumber)
        customView.setTextViewText(R.id.tvContactName,  name?: phoneNumber)
        expandView.setTextViewText(R.id.tvContactName,  name?: phoneNumber)

        customView.setTextViewText(R.id.tvNumber, name ?: "Unknown")
        expandView.setTextViewText(R.id.tvNumber, name ?: "Unknown")

        if (!photoUri.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(photoUri)
                val inputStream = context.contentResolver.openInputStream(uri)
                val contactBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (contactBitmap != null) {
                    customView.setImageViewBitmap(R.id.ivProfile, contactBitmap)
                    expandView.setImageViewBitmap(R.id.ivProfile, contactBitmap)
                } else {
                    customView.setImageViewResource(R.id.ivProfile, R.drawable.personal)
                    expandView.setImageViewResource(R.id.ivProfile, R.drawable.personal)
                }
            } catch (e: Exception) {
                customView.setImageViewResource(R.id.ivProfile, R.drawable.personal)
                expandView.setImageViewResource(R.id.ivProfile, R.drawable.personal)
            }
        } else {
            customView.setImageViewResource(R.id.ivProfile, R.drawable.personal)
            expandView.setImageViewResource(R.id.ivProfile, R.drawable.personal)
        }
    }



    when (call.details.callDirection) {
        Call.Details.DIRECTION_OUTGOING -> {
            customView.setViewVisibility(R.id.btnAccept, View.GONE)
            customView.setViewVisibility(R.id.btnDecline, View.GONE)
            customView.setViewVisibility(R.id.hangup_btn, View.GONE)


            customView.setViewPadding(
                R.id.tvContactName,
                4,
                0,
                200,
                0
            )
            customView.setViewPadding(
                R.id.tvNumber,
                4,
                0,
                50,
                0
            )
            expandView.setViewPadding(
                R.id.tvContactName,
                4,
                0,
                50,
                0
            )
            expandView.setViewPadding(
                R.id.tvNumber,
                4,
                0,
                50,
                0
            )
            customView.setTextViewTextSize(
                R.id.tvContactName,
                TypedValue.COMPLEX_UNIT_SP,
                12f
            )

            expandView.setTextViewTextSize(
                R.id.tvNumber,
                TypedValue.COMPLEX_UNIT_SP,
                12f
            )

            expandView.setViewVisibility(R.id.tvNumber, View.VISIBLE)
            customView.setViewVisibility(R.id.tvNumber, View.VISIBLE)

            customView.setViewVisibility(R.id.msgIncoming, View.GONE)
            customView.setViewVisibility(R.id.divider, View.GONE)
            customView.setViewVisibility(R.id.tvIncomingCall, View.GONE)
            customView.setViewVisibility(R.id.tvLocation, View.GONE)
            customView.setViewVisibility(R.id.ivProfile, View.VISIBLE)
            customView.setViewVisibility(R.id.msgOutgoing, View.GONE)

            expandView.setViewVisibility(R.id.btnAccept, View.GONE)
            expandView.setViewVisibility(R.id.btnDecline, View.GONE)
            expandView.setViewVisibility(R.id.hangup_btn, View.VISIBLE)
            expandView.setViewVisibility(R.id.msgIncoming, View.GONE)
            expandView.setViewVisibility(R.id.msgOutgoing, View.VISIBLE)
            expandView.setViewVisibility(R.id.divider,View.VISIBLE)
//            expandView.setViewVisibility(R.id.tvIncomingCall, View.GONE)
            expandView.setViewVisibility(R.id.tvLocation, View.GONE)

            expandView.setViewVisibility(R.id.tvIncomingCall, View.VISIBLE)
            expandView.setTextViewText(R.id.tvIncomingCall, "Ongoing Call")


            val hangupIntent = Intent(context, CallActionReceiver::class.java).apply {
                action = DECLINE_CALL
            }
            val hangupPendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                hangupIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            customView.setOnClickPendingIntent(R.id.hangup_btn, hangupPendingIntent)
            expandView.setOnClickPendingIntent(R.id.hangup_btn, hangupPendingIntent)
        }
        Call.Details.DIRECTION_INCOMING -> {
            when (call.details.state) {
                Call.STATE_RINGING -> {
                    customView.setViewVisibility(R.id.btnAccept, View.VISIBLE)
                    customView.setViewVisibility(R.id.btnDecline, View.VISIBLE)
                    customView.setViewVisibility(R.id.hangup_btn, View.GONE)
                    customView.setViewVisibility(R.id.msgIncoming, View.GONE)
                    customView.setViewVisibility(R.id.divider, View.GONE)
                    customView.setViewVisibility(R.id.msgOutgoing, View.GONE)


//                    customView.setViewVisibility(R.id.btnAccept, View.GONE)
//                    customView.setViewVisibility(R.id.btnDecline, View.GONE)

                    customView.setViewVisibility(R.id.tvIncomingCall, View.GONE)
                    customView.setViewVisibility(R.id.tvLocation, View.GONE)


                    customView.setViewVisibility(R.id.tvContactName, View.VISIBLE)
                    customView.setViewVisibility(R.id.tvNumber, View.VISIBLE)
                    customView.setViewVisibility(R.id.ivProfile, View.VISIBLE)


                    customView.setViewPadding(
                        R.id.tvContactName,
                        4,
                        0,
                        300,
                        0
                    )
                    customView.setViewPadding(
                        R.id.tvNumber,
                        4,
                        0,
                        300,
                        0
                    )
                    expandView.setViewPadding(
                        R.id.tvContactName,
                        4,
                        0,
                        300,
                        0
                    )
                    expandView.setViewPadding(
                        R.id.tvNumber,
                        4,
                        0,
                        300,
                        0
                    )

                    val acceptCallIntent = Intent(context, CallActionReceiver::class.java).apply {
                        action = ACCEPT_CALL
                    }
                    val acceptPendingIntent = PendingIntent.getBroadcast(
                        context,
                        0,
                        acceptCallIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                    customView.setOnClickPendingIntent(R.id.btnAccept, acceptPendingIntent)

                    val declineCallIntent = Intent(context, CallActionReceiver::class.java).apply {
                        action = DECLINE_CALL
                    }
                    val declinePendingIntent = PendingIntent.getBroadcast(
                        context,
                        1,
                        declineCallIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                    customView.setOnClickPendingIntent(R.id.btnDecline, declinePendingIntent)



                    expandView.setViewVisibility(R.id.btnDecline, View.VISIBLE)
                    expandView.setViewVisibility(R.id.btnAccept, View.VISIBLE)
                    expandView.setViewVisibility(R.id.hangup_btn, View.GONE)
                    expandView.setViewVisibility(R.id.msgIncoming, View.VISIBLE)
                    expandView.setViewVisibility(R.id.msgOutgoing, View.GONE)
                    expandView.setViewVisibility(R.id.divider, View.VISIBLE)
                    expandView.setOnClickPendingIntent(R.id.btnDecline, declinePendingIntent)
                    expandView.setOnClickPendingIntent(R.id.btnAccept, acceptPendingIntent)

                }
                Call.STATE_ACTIVE -> {
                    customView.setViewVisibility(R.id.btnAccept, View.GONE)
                    customView.setViewVisibility(R.id.btnDecline, View.GONE)
                    customView.setViewVisibility(R.id.hangup_btn, View.VISIBLE)
                    customView.setViewVisibility(R.id.divider, View.GONE)
                    customView.setViewVisibility(R.id.msgIncoming, View.GONE)
                    customView.setViewVisibility(R.id.tvIncomingCall, View.GONE)
                    expandView.setViewVisibility(R.id.msgOutgoing, View.GONE)
                    expandView.setViewVisibility(R.id.tvIncomingCall, View.VISIBLE)
                    customView.setViewVisibility(R.id.tvLocation, View.GONE)
                    expandView.setViewVisibility(R.id.tvLocation, View.VISIBLE)
//                    customView.setTextViewText(R.id.tvIncomingCall, "Ongoing Call")


                    expandView.setViewVisibility(R.id.tvLocation, View.GONE)
                    expandView.setTextViewText(R.id.tvIncomingCall, "Ongoing Call")
                    expandView.setViewVisibility(R.id.btnAccept, View.GONE)
                    expandView.setViewVisibility(R.id.btnDecline, View.GONE)
                    expandView.setViewVisibility(R.id.hangup_btn, View.VISIBLE)
                    expandView.setViewVisibility(R.id.divider, View.VISIBLE)
                    expandView.setViewVisibility(R.id.msgIncoming, View.GONE)
                    expandView.setViewVisibility(R.id.msgOutgoing, View.VISIBLE)


                    val hangupIntent = Intent(context, CallActionReceiver::class.java).apply {
                        action = DECLINE_CALL
                    }
                    val hangupPendingIntent = PendingIntent.getBroadcast(
                        context,
                        1,
                        hangupIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                    customView.setOnClickPendingIntent(R.id.hangup_btn, hangupPendingIntent)
                    expandView.setOnClickPendingIntent(R.id.hangup_btn, hangupPendingIntent)
                }
                else -> {

                    // if need i will  , handle other states (e.g., dialing or disconnected)
                }
            }
        }
    }

    val messageIntent = Intent(context, CallActionReceiver::class.java).apply {
        action = "SEND_MESSAGE"
    }
    val messagePendingIntent = PendingIntent.getBroadcast(
        context,
        2,
        messageIntent,
        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
    )

    val openAppIntent = CallActivity.getStartIntent(context, call)
    val openAppPendingIntent = PendingIntent.getActivity(
        context,
        3,
        openAppIntent,
//        PendingIntent.FLAG_MUTABLE

        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    )

    val isHighPriority = call.details?.state == Call.STATE_RINGING && !forceLowPriority
    val channelId = if (isHighPriority) "call_high_priority" else "call_low_priority"

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val importance = if (isHighPriority) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT

    val name = if (isHighPriority) "call_notification_channel_high_priority" else "call_notification_channel"

    NotificationChannel(channelId, name, importance).apply {
        setSound(null, null)
        notificationManager.createNotificationChannel(this)
    }



    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.app_notification_icon)
//        .setSmallIcon(R.drawable.ic_stat_name)
        .setContentIntent(openAppPendingIntent)
        .setPriority(if (isHighPriority) NotificationManager.IMPORTANCE_HIGH else NotificationCompat.PRIORITY_DEFAULT)
        .setCategory(Notification.CATEGORY_CALL)
        .setCustomContentView(customView)
        .setCustomBigContentView(expandView)

        .setOngoing(true)
        .setSound(null)
        .setUsesChronometer(call.details.state == Call.STATE_ACTIVE)
        .setChannelId(channelId)
        .setStyle(NotificationCompat.DecoratedCustomViewStyle())

    val priority =  if (!isOutgoing || isActive ) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_LOW


    if (isHighPriority) {
        builder.setFullScreenIntent(openAppPendingIntent, true)
    }

    val notification = builder.build()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(12, notification)
        }
    } else {
        NotificationManagerCompat.from(context).notify(12, notification)
    }


}