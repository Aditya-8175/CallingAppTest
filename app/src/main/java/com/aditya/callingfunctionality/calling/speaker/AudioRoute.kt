package com.aditya.callingfunctionality.calling.speaker

import android.telecom.CallAudioState;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import com.aditya.callingfunctionality.R


enum class AudioRoute(val route: Int, @StringRes val stringRes: Int, @DrawableRes val iconRes: Int) {
    SPEAKER(CallAudioState.ROUTE_SPEAKER, R.string.audio_route_speaker, R.drawable.speaker_call),
    EARPIECE(CallAudioState.ROUTE_EARPIECE, R.string.audio_route_earpiece, R.drawable.mute_call),
    BLUETOOTH(CallAudioState.ROUTE_BLUETOOTH, R.string.audio_route_bluetooth, R.drawable.speaker_call),
    WIRED_HEADSET(CallAudioState.ROUTE_WIRED_HEADSET, R.string.audio_route_wired_headset, R.drawable.mute_call),
    WIRED_OR_EARPIECE(CallAudioState.ROUTE_WIRED_OR_EARPIECE, R.string.audio_route_wired_or_earpiece, R.drawable.speaker_call);

    companion object {
        fun fromRoute(route: Int?) = values().firstOrNull { it.route == route }
    }
}