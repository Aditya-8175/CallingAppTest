package com.aditya.callingfunctionality.component

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.PhoneNumberUtils
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

data class ContactInfo(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val photoUri: String? = null
)

data class CallLogItem(
    val id: String,
    val name: String,
    val number: String,
    val type: Int,
    val date: Date,
    val duration: String,
    val photoUri: String? = null,
    val simId: Int,
)


class CallLogHelper(private val context: Context) {

    private val QUERY_LIMIT = 500

    private val reverseLookupCache = mutableMapOf<String, ContactInfo?>()
    private fun normalizePhoneNumber(number: String): String {
        return number.replace(Regex("^\\+?91|^0"), "").trim()
    }


    suspend fun getCallLogs(): List<CallLogItem> = withContext(Dispatchers.IO) {
        val callLogs = mutableListOf<CallLogItem>()
        val contentResolver: ContentResolver = context.contentResolver
//        val uri: Uri = CallLog.Calls.CONTENT_URI
        val callLogUri: Uri = CallLog.Calls.CONTENT_URI

        val contactsMap = getContactsMap()
        val simMapping = getSimMapping()

        val projection = arrayOf(
            CallLog.Calls._ID,   // Added ID field
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.PHONE_ACCOUNT_ID
        )

//        val cursor: Cursor? = contentResolver.query(
//            uri,
//            projection,
//            null,
//            null,
//            "${CallLog.Calls.DATE} DESC"
//        )

//        val limitedUri = callLogUri.buildUpon().appendQueryParameter("limit", QUERY_LIMIT.toString()).build()
        val sortOrder = "${CallLog.Calls.DATE} DESC"


        val cursor: Cursor? = contentResolver.query(
            callLogUri,
            projection,
            null,
            null,
            sortOrder
        )


        cursor?.use {
            val idIndex = it.getColumnIndex(CallLog.Calls._ID)
            val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
            val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
            val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)

            while (it.moveToNext()) {
                val id = it.getString(idIndex) ?: ""
                val number = it.getString(numberIndex) ?: ""
                val type = it.getInt(typeIndex)
                val date = Date(it.getLong(dateIndex))
                val duration = it.getString(durationIndex) ?: "0"

//                val normalizedNumber = PhoneNumberUtils.normalizeNumber(number)
//
//                val contactInfo = reverseLookupCache.getOrPut(normalizedNumber) {
//                    getContactNameAndPhoto(context, normalizedNumber)
//                }
//
//                val name = contactInfo?.name ?: "Unknown"
//                val photoUriString = contactInfo?.photoUri
//
//                callLogs.add(CallLogItem(name, number, type, date, duration, photoUriString))

//                val normalizedNumber = PhoneNumberUtils.normalizeNumber(number)
                val normalizedNumber = PhoneNumberUtils.formatNumberToE164(number, "IN") ?: PhoneNumberUtils.normalizeNumber(number)

                val contactInfo = contactsMap[normalizedNumber]
//                val name = contactInfo?.name ?: "Unknown"
                val name = contactInfo?.name?.takeIf { it.isNotBlank() } ?: number

                val photoUri = contactInfo?.photoUri

                val account = it.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID)
                val accountId = it.getString(account)
                val simId = simMapping[accountId] ?: -1

//                callLogs.add(CallLogItem(name, number, type, date, duration, photoUri, simId))


                val callLogItem = CallLogItem(id,
                    name.toString(), number, type, date, duration, photoUri, simId)
                callLogs.add(callLogItem)
                Log.d("CallLogHelper", "CallLogItem: $callLogItem")


            }

            cursor.close()
        }
        callLogs
    }


    private suspend fun getContactsMap(): Map<String, ContactInfo> = withContext(Dispatchers.IO) {
        val contactsMap = mutableMapOf<String, ContactInfo>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )
        val cursor: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            null
        )
        cursor?.use {
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val photoIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            while (it.moveToNext()) {
                val number = it.getString(numberIndex) ?: continue
//                val normalizedNumber = PhoneNumberUtils.normalizeNumber(number)
                val name = it.getString(nameIndex) ?: "Unknown"
                val normalizedNumber = PhoneNumberUtils.formatNumberToE164(number, "IN") ?: PhoneNumberUtils.normalizeNumber(number)

                val photoUri = if (photoIndex >= 0) it.getString(photoIndex) else null
                if (!normalizedNumber.isNullOrBlank()) {
                    contactsMap[normalizedNumber] = ContactInfo("", name, normalizedNumber, photoUri)
                }
            }
        }
        contactsMap
    }

    private suspend fun getSimMapping(): Map<String, Int> = withContext(Dispatchers.IO) {
        val simAccounts = getSimAccounts()
        val mapping = mutableMapOf<String, Int>()
        simAccounts.forEach { simAccount ->
            // The call log's PHONE_ACCOUNT_ID should match the PhoneAccountHandle.id
            mapping[simAccount.handle.id] = simAccount.id
        }
        mapping
    }

    @SuppressLint("MissingPermission")
    private fun getSimAccounts(): List<SIMAccount> {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val phoneAccounts: List<PhoneAccountHandle> = telecomManager.callCapablePhoneAccounts
        val simAccounts = mutableListOf<SIMAccount>()
        phoneAccounts.forEachIndexed { index, phoneAccountHandle ->
            val phoneAccount = telecomManager.getPhoneAccount(phoneAccountHandle)
            val label = phoneAccount.label?.toString() ?: "SIM ${index + 1}"
            val phoneNumber = phoneAccount.address?.schemeSpecificPart ?: ""
            simAccounts.add(SIMAccount(id = index + 1, handle = phoneAccountHandle, label = label, phoneNumber = phoneNumber))
        }
        return simAccounts
    }



    private fun getContactNameAndPhoto(context: Context, phoneNumber: String): ContactInfo? {
        if (phoneNumber.isBlank()) return null
        val normalizedNumber = PhoneNumberUtils.normalizeNumber(phoneNumber)
        val lookupUri: Uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(normalizedNumber)
        )
        val projection = arrayOf(
            ContactsContract.PhoneLookup._ID,
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_URI
        )
        context.contentResolver.query(lookupUri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup._ID)
                val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                val photoIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI)

                val id = if (idIndex >= 0) cursor.getString(idIndex) ?: "" else ""
//                val name = if (nameIndex >= 0) cursor.getString(nameIndex) ?: "Unknown" else "Unknown"
                val name = if (nameIndex >= 0) cursor.getString(nameIndex)?.takeIf { it.isNotBlank() } ?: phoneNumber else phoneNumber

                val photoUri = if (photoIndex >= 0) cursor.getString(photoIndex) else null


                Log.d("ReverseLookup", "Found contact: $name, photoUri: $photoUri for number: $phoneNumber")


                return ContactInfo(id, name ?: "Unknown", phoneNumber, photoUri)
            }
            else {
                Log.d("ReverseLookup", "No contact found for number: $phoneNumber")
            }
        }
        return null
    }

}

data class SIMAccount(val id: Int, val handle: PhoneAccountHandle, val label: String, val phoneNumber: String)