package com.oqba26.abzarforoush.util

import android.content.Context
import android.provider.ContactsContract

data class ContactInfo(val name: String, val phoneNumber: String)

object ContactHelper {
    fun getContactsByName(context: Context, query: String): List<ContactInfo> {
        if (query.isBlank() || query.length < 2) return emptyList()
        
        val normalizedQuery = query.normalizePersian()
        if (normalizedQuery.isBlank()) return emptyList()
        
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        
        val contacts = mutableListOf<ContactInfo>()
        
        try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                
                if (nameIndex != -1 && numberIndex != -1) {
                    while (cursor.moveToNext()) {
                        val displayName = cursor.getString(nameIndex) ?: ""
                        if (displayName.normalizePersian().contains(normalizedQuery)) {
                            val rawNumber = cursor.getString(numberIndex)
                            if (!rawNumber.isNullOrBlank()) {
                                val cleanNumber = rawNumber.replace(" ", "")
                                    .replace("-", "")
                                    .replace("+98", "0")
                                    .replace(Regex("^98"), "0")
                                
                                // Avoid duplicate numbers for same name if any
                                if (contacts.none { it.name == displayName && it.phoneNumber == cleanNumber }) {
                                    contacts.add(ContactInfo(displayName, cleanNumber))
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Sort: exact match first, then starts with, then contains
        return contacts.sortedWith(compareByDescending<ContactInfo> { 
            it.name.normalizePersian() == normalizedQuery 
        }.thenByDescending { 
            it.name.normalizePersian().startsWith(normalizedQuery) 
        }).take(5) // Limit to top 5 matches
    }

    private fun String.normalizePersian(): String {
        return this.lowercase()
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace(" ", "")
            .replace("\u200c", "") // half-space
    }
}
