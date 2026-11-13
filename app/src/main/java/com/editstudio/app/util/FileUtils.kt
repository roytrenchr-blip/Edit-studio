package com.editstudio.app.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {
    fun copyUriToAppStorage(context: Context, uri: Uri, prefix: String = "media"): File? {
        return try {
            val contentResolver = context.contentResolver
            val extension = getExtensionFromUri(context, uri) ?: "bin"
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val targetFile = File(context.filesDir, "${prefix}_${timeStamp}.${extension}")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getExtensionFromUri(context: Context, uri: Uri): String? {
        val mime = context.contentResolver.getType(uri) ?: return null
        return when {
            mime.contains("video/mp4") -> "mp4"
            mime.contains("video/") -> "mp4"
            mime.contains("image/png") -> "png"
            mime.contains("image/jpeg") -> "jpg"
            mime.contains("image/") -> "png"
            mime.contains("audio/mpeg") -> "mp3"
            mime.contains("audio/wav") -> "wav"
            mime.contains("audio/") -> "mp3"
            else -> "bin"
        }
    }

    fun saveVideoToGallery(context: Context, sourceFile: File, title: String): Uri? {
        return try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, "${title}_${System.currentTimeMillis()}.mp4")
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/EditStudio")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val uri = resolver.insert(collection, contentValues) ?: return null
            resolver.openOutputStream(uri)?.use { out ->
                FileInputStream(sourceFile).use { input ->
                    input.copyTo(out)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getExportOutputFile(context: Context, extension: String = "mp4"): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(exportDir, "EditStudio_${timeStamp}.$extension")
    }
}
