package com.oqba26.abzarforoush.util

import android.content.Context
import android.provider.ContactsContract

object ContactHelper {
    fun getPhoneNumberByName(context: Context, name: String): String? {
        if (name.isBlank() || name.length < 3) return null
        
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        
        // Use LIKE for more flexible matching
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$name%")
        
        return try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val rawNumber = cursor.getString(0)
                    // Basic cleaning here
                    rawNumber?.replace(" ", "")?.replace("-", "")
                        ?.replace("+98", "0")
                        ?.replace(Regex("^98"), "0")
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }
}