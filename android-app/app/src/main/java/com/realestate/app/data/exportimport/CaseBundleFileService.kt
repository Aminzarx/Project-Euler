package com.realestate.app.data.exportimport

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * The only piece of this feature that needs an Android [Context] — writing a shareable file to
 * cache and handing back a `content://` [Uri] via the app's existing FileProvider (the same one
 * StoryCard image sharing already uses), and reading bytes back from a [Uri] the document picker
 * or share target returned. Deliberately thin: everything that decides *what* those bytes mean
 * (FileCrypto, ExportEnvelope, ImportMerge) is pure Kotlin with no Context dependency, so it's
 * unit-testable without this class or an emulator.
 */
class CaseBundleFileService(private val context: Context) {

    /** Writes an encrypted bundle to a fresh cache file and returns a content Uri suitable for
     *  Intent.ACTION_SEND. Bundles are single-use share artifacts, so the folder is cleared first —
     *  same reasoning as StoryCard's image cache cleanup: nothing here is worth keeping around
     *  once shared, and letting it accumulate would leak disk space silently over time. */
    fun writeShareFile(encryptedBytes: ByteArray, fileName: String): Uri {
        val dir = File(context.cacheDir, "bundles").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val file = File(dir, fileName)
        file.writeBytes(encryptedBytes)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun readFile(uri: Uri): ByteArray =
        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("امکان خواندن فایل انتخاب‌شده وجود ندارد")
}
