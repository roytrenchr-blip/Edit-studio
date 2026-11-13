package com.editstudio.app.store

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

@Serializable
data class StoreItem(
    val id: String,
    val name: String,
    val description: String = "",
    val category: String = "Motion Packs",
    val thumbnailUrl: String = "",
    val downloadUrl: String = "",
    val size: String = "1.0 MB",
    val version: Int = 1,
    val isDownloaded: Boolean = false
)

class ContentCache(private val context: Context) {
    private val cacheDir = File(context.filesDir, "store_cache").apply { mkdirs() }
    private val manifestFile = File(cacheDir, "manifest_cache.json")

    fun saveManifestJson(json: String) {
        manifestFile.writeText(json)
    }

    fun loadManifestJson(): String? {
        return if (manifestFile.exists()) manifestFile.readText() else null
    }

    fun getItemFile(itemId: String): File = File(cacheDir, "$itemId.pack")

    fun isItemCached(itemId: String): Boolean = getItemFile(itemId).exists()
}

class ManifestRepository(private val context: Context) {
    private val cache = ContentCache(context)
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun getManifest(sourceUrl: String): List<StoreItem> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val rawJson = try {
            val request = Request.Builder().url(sourceUrl).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        cache.saveManifestJson(body)
                        body
                    } else null
                } else null
            }
        } catch (_: Exception) {
            null
        } ?: cache.loadManifestJson() ?: DEFAULT_STARTER_MANIFEST_JSON

        val parsed = try {
            json.decodeFromString<List<StoreItem>>(rawJson)
        } catch (_: Exception) {
            json.decodeFromString<List<StoreItem>>(DEFAULT_STARTER_MANIFEST_JSON)
        }
        parsed.map { item ->
            item.copy(isDownloaded = cache.isItemCached(item.id))
        }
    }

    companion object {
        const val DEFAULT_STARTER_MANIFEST_JSON = """
[
  {
    "id": "pack_viral_reels",
    "name": "Viral Reels Motion Pack",
    "description": "Zoom punch, whip pan, beat shake aur glitch presets short videos ke liye.",
    "category": "Motion Packs",
    "thumbnailUrl": "",
    "downloadUrl": "",
    "size": "1.2 MB",
    "version": 2
  },
  {
    "id": "pack_cinematic_effects",
    "name": "Cinema Glow & Film Pack",
    "description": "Anamorphic flare, halation bloom aur 35mm film burn effects.",
    "category": "Effect Packs",
    "thumbnailUrl": "",
    "downloadUrl": "",
    "size": "1.8 MB",
    "version": 1
  },
  {
    "id": "tpl_lyric_status",
    "name": "Lyric Glow Status Template",
    "description": "Ready 9:16 template kinetic typography aur neon glow ke saath.",
    "category": "Templates",
    "thumbnailUrl": "",
    "downloadUrl": "",
    "size": "850 KB",
    "version": 1
  },
  {
    "id": "tpl_travel_reel",
    "name": "Travel Beat Cut Template",
    "description": "Fast beat-synced montage template speed ramp ke saath.",
    "category": "Templates",
    "thumbnailUrl": "",
    "downloadUrl": "",
    "size": "1.1 MB",
    "version": 1
  },
  {
    "id": "sticker_neon_badges",
    "name": "Creator Neon Stickers Pack",
    "description": "Subscribe, Like, Arrow, Sparkle aur Glow badges.",
    "category": "Stickers and Shapes",
    "thumbnailUrl": "",
    "downloadUrl": "",
    "size": "920 KB",
    "version": 1
  },
  {
    "id": "lut_teal_orange",
    "name": "Teal & Orange Cinema LUTs",
    "description": "Blockbuster color looks aur warm sunset LUTs.",
    "category": "LUT Looks",
    "thumbnailUrl": "",
    "downloadUrl": "",
    "size": "2.4 MB",
    "version": 1
  },
  {
    "id": "music_lofi_chill",
    "name": "Desi Lo-Fi Chill Beat",
    "description": "Vlogs aur montage ke liye smooth 90 BPM lo-fi track.",
    "category": "Music",
    "thumbnailUrl": "",
    "downloadUrl": "",
    "size": "3.6 MB",
    "version": 1
  },
  {
    "id": "sfx_whoosh_pack",
    "name": "Cinematic Whoosh & Boom SFX",
    "description": "Transitions ke liye punchy risers, whooshes aur bass drops.",
    "category": "Sound Effects",
    "thumbnailUrl": "",
    "downloadUrl": "",
    "size": "4.8 MB",
    "version": 1
  },
  {
    "id": "overlay_film_dust",
    "name": "35mm Grain & Light Leaks",
    "description": "Analog film scratches, subtle dust aur light flares.",
    "category": "Overlays",
    "thumbnailUrl": "",
    "downloadUrl": "",
    "size": "2.1 MB",
    "version": 1
  }
]
"""
    }
}
