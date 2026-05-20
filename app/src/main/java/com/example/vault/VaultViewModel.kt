package com.example.vault

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class VaultViewModel(
    private val vaultSettings: VaultSettings,
    private val appDatabase: VaultDatabase,
    private val context: Context
) : ViewModel() {

    private val mediaDao = appDatabase.vaultMediaDao()

    val savedPin: StateFlow<String?> = vaultSettings.pinFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val vaultMedia: StateFlow<List<VaultMedia>> = mediaDao.getAllMedia().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Used to signal navigation to the vault
    private val _navigateToVault = MutableStateFlow(false)
    val navigateToVault = _navigateToVault.asStateFlow()

    fun setPin(pin: String) {
        viewModelScope.launch {
            vaultSettings.savePin(pin)
        }
    }

    fun onPinEntered() {
        _navigateToVault.value = true
    }

    fun onVaultExited() {
        _navigateToVault.value = false
    }

    // Media importing
    fun importMedia(uris: List<Uri>) {
        viewModelScope.launch {
            val vaultDir = File(context.filesDir, "vault_media").apply { mkdirs() }

            for (uri in uris) {
                val mimeType = context.contentResolver.getType(uri) ?: ""
                val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
                val id = UUID.randomUUID().toString()
                val filename = "$id.$extension"
                val destinationFile = File(vaultDir, filename)

                try {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    val outputStream = FileOutputStream(destinationFile)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()

                    val type = if (mimeType.startsWith("video/")) "video" else "image"
                    
                    mediaDao.insertMedia(
                        VaultMedia(
                            id = id,
                            filename = filename,
                            mimeType = mimeType,
                            type = type
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun getMediaFile(filename: String): File {
        return File(File(context.filesDir, "vault_media"), filename)
    }
    
    fun deleteMedia(item: VaultMedia) {
        viewModelScope.launch {
            val file = getMediaFile(item.filename)
            if (file.exists()) {
                file.delete()
            }
            mediaDao.deleteMedia(item.id)
        }
    }
}

class VaultViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VaultViewModel::class.java)) {
            val settings = VaultSettings(context)
            val db = VaultDatabase.getDatabase(context)
            @Suppress("UNCHECKED_CAST")
            return VaultViewModel(settings, db, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
