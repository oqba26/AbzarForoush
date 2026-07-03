package com.oqba26.abzarforoush.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val SELECTED_FONT = stringPreferencesKey("selected_font")
        val SELECTED_THEME = stringPreferencesKey("selected_theme")
        val SUPABASE_URL = stringPreferencesKey("supabase_url")
        val SUPABASE_KEY = stringPreferencesKey("supabase_key")
        val IS_SYNC_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("is_sync_enabled")
        val SHOP_NAME = stringPreferencesKey("shop_name")
        val SHOP_PHONE = stringPreferencesKey("shop_phone")
        val SHOP_ADDRESS = stringPreferencesKey("shop_address")
        val SHOP_TAX_ID = stringPreferencesKey("shop_tax_id")
    }

    val selectedFont: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_FONT] ?: "Vazirmatn"
    }

    val selectedTheme: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_THEME] ?: "Purple"
    }

    val supabaseUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SUPABASE_URL] ?: "https://qkhpoovfuvlusqvhdyjd.supabase.co"
    }

    val supabaseKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SUPABASE_KEY] ?: "sb_publishable_0QX4i8XTDpf7ZfO9T4u9Sg_mdUGUX81"
    }

    val isSyncEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_SYNC_ENABLED] ?: true
    }

    val shopName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SHOP_NAME] ?: ""
    }

    val shopPhone: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SHOP_PHONE] ?: ""
    }

    val shopAddress: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SHOP_ADDRESS] ?: ""
    }

    val shopTaxId: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SHOP_TAX_ID] ?: ""
    }

    suspend fun saveFont(fontName: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_FONT] = fontName
        }
    }

    suspend fun saveTheme(themeName: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_THEME] = themeName
        }
    }

    suspend fun setSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_SYNC_ENABLED] = enabled
        }
    }

    suspend fun saveShopInfo(name: String, phone: String, address: String, taxId: String) {
        context.dataStore.edit { preferences ->
            preferences[SHOP_NAME] = name
            preferences[SHOP_PHONE] = phone
            preferences[SHOP_ADDRESS] = address
            preferences[SHOP_TAX_ID] = taxId
        }
    }
}
