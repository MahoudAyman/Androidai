package com.example.vault

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vault_settings")

class VaultSettings(private val context: Context) {
    private val PIN_KEY = stringPreferencesKey("vault_pin")

    val pinFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PIN_KEY]
    }

    suspend fun savePin(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[PIN_KEY] = pin
        }
    }
}
