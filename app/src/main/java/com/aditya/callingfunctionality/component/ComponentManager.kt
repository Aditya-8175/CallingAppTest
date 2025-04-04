package com.aditya.callingfunctionality.component

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract

data class Contact(
    val id: String,
    var name: String,
    var phoneNumber: String,
    var photoUri: String? = null
)

class ContactManager(private val context: Context) {
    private val contentResolver: ContentResolver = context.contentResolver

    fun readContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )

        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val name = cursor.getString(1)
                val number = cursor.getString(2)
                val photoUri = cursor.getString(3)

                contacts.add(Contact(id, name, number, photoUri))
            }
            cursor.close()
        }

        return contacts
    }


    fun readFavoriteContacts(context: Context): List<Contact> {
        val contactList = mutableListOf<Contact>()
        val contentResolver: ContentResolver = context.contentResolver

        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_URI
        )

        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            "${ContactsContract.Contacts.STARRED}=?",
            arrayOf("1"),
            "${ContactsContract.Contacts.DISPLAY_NAME} ASC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val photoIndex = it.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)

            while (it.moveToNext()) {
                val id = if (idIndex != -1) it.getString(idIndex) else "N/A"
                val name = if (nameIndex != -1) it.getString(nameIndex) else "Unknown"
                val photo = if (photoIndex != -1) it.getString(photoIndex) else null

                // Fetch phone number separately
                val number = getPhoneNumber(contentResolver, id)

                contactList.add(Contact(id, name, number, photo))
            }
        }

        return contactList
    }

    private fun getPhoneNumber(contentResolver: ContentResolver, contactId: String): String {
        val phoneCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )

        var phoneNumber = "No Number"
        phoneCursor?.use {
            if (it.moveToFirst()) {
                phoneNumber = it.getString(0)
            }
        }
        return phoneNumber
    }


}



