package com.editstudio.app.export

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import com.editstudio.app.domain.FrameRate
import com.editstudio.app.domain.Project
import com.editstudio.app.domain.VideoResolution
import com.editstudio.app.engine.ProjectSerializer
import com.editstudio.app.render.SharedFrameRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer

enum class ExportFormat(val label: String, val mimeType: String, val extension: String) {
    MP4_H264("MP4 (H.264)", MediaFormat.MIMETYPE_VIDEO_AVC, "mp4"),
    MP4_H265("MP4 (HEVC 4K)", MediaFormat.MIMETYPE_VIDEO_HEVC, "mp4"),
    GIF_LOOP("Animated GIF", "image/gif", "gif"),
    PNG_SEQUENCE("PNG Frame", "image/png", "png")
}

data class ExportConfig(
    val format: ExportFormat = ExportFormat.MP4_H264,
    val resolution: VideoResolution = VideoResolution.FHD_1080P,
    val frameRate: FrameRate = FrameRate.FPS_30,
    val bitrateMbps: Int = 12
)

data class ExportProgress(
    val isExporting: Boolean = false,
    val progress: Float = 0f,
    val outputPath: String? = null,
    val error: String? = null
)

object AudioMixer {
    fun mixProjectAudio(project: Project): FloatArray {
        val sampleCount = 1024
        return FloatArray(sampleCount) { 0f }
    }
}

class VideoExporter(private val context: Context) {

    fun exportProject(
        project: Project,
        config: ExportConfig,
        onProgress: (Float) -> Unit
    ): Result<String> {
        return try {
            val width = (project.widthFor(config.resolution) / 16 * 16).coerceAtLeast(320)
            val height = (project.heightFor(config.resolution) / 16 * 16).coerceAtLeast(320)
            val fps = config.frameRate.fps
            val durationMs = project.durationMs.coerceAtLeast(1000L)
            val totalFrames = ((durationMs / 1000f) * fps).toInt().coerceIn(1, 1800)

            val outDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "EditStudio").apply { mkdirs() }
            val safeName = project.name.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "EditStudio_Video" }
            val outFile = File(outDir, "${safeName}_${System.currentTimeMillis()}.${config.format.extension}")

            if (config.format == ExportFormat.PNG_SEQUENCE || config.format == ExportFormat.GIF_LOOP) {
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                SharedFrameRenderer.drawProjectFrame(canvas, project, 0L, width.toFloat(), height.toFloat(), isExport = true)
                outFile.outputStream().use { fos ->
                    bmp.compress(Bitmap.CompressFormat.PNG, 95, fos)
                }
                bmp.recycle()
                onProgress(1f)
                saveToMediaStore(outFile, config.format)
                return Result.success(outFile.absolutePath)
            }

            // Encode using MediaCodec surface + MediaMuxer
            val mime = config.format.mimeType
            val mediaFormat = MediaFormat.createVideoFormat(mime, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, config.bitrateMbps * 1_000_000)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            val encoder = MediaCodec.createEncoderByType(mime)
            encoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = encoder.createInputSurface()
            encoder.start()

            val muxer = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var trackIndex = -1
            var muxerStarted = false
            val bufferInfo = MediaCodec.BufferInfo()

            val frameStepMs = (1000L / fps).coerceAtLeast(16L)
            for (frameIdx in 0 until totalFrames) {
                val timeMs = (frameIdx * frameStepMs).coerceAtMost(durationMs)
                val canvas = inputSurface.lockCanvas(null)
                try {
                    SharedFrameRenderer.drawProjectFrame(
                        canvas = canvas,
                        project = project,
                        timeMs = timeMs,
                        width = width.toFloat(),
                        height = height.toFloat(),
                        isExport = true
                    )
                } finally {
                    inputSurface.unlockCanvasAndPost(canvas)
                }

                drainEncoder(encoder, muxer, bufferInfo, endOfStream = false) { newTrackIdx, format ->
                    trackIndex = muxer.addTrack(format)
                    muxer.start()
                    muxerStarted = true
                    trackIndex
                }

                onProgress((frameIdx + 1).toFloat() / totalFrames.toFloat())
            }

            encoder.signalEndOfInputStream()
            drainEncoder(encoder, muxer, bufferInfo, endOfStream = true) { _, format ->
                if (!muxerStarted) {
                    trackIndex = muxer.addTrack(format)
                    muxer.start()
                    muxerStarted = true
                }
                trackIndex
            }

            encoder.stop()
            encoder.release()
            inputSurface.release()
            if (muxerStarted) {
                muxer.stop()
            }
            muxer.release()

            saveToMediaStore(outFile, config.format)
            Result.success(outFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun drainEncoder(
        encoder: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        endOfStream: Boolean,
        onTrackAdded: (Int, MediaFormat) -> Int
    ) {
        var trackIndex = -1
        while (true) {
            val status = encoder.dequeueOutputBuffer(bufferInfo, 10_000L)
            if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
            } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                trackIndex = onTrackAdded(trackIndex, encoder.outputFormat)
            } else if (status >= 0) {
                val encodedData: ByteBuffer? = encoder.getOutputBuffer(status)
                if (encodedData != null && (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0 && bufferInfo.size > 0 && trackIndex >= 0) {
                    encodedData.position(bufferInfo.offset)
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)
                    muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                }
                encoder.releaseOutputBuffer(status, false)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) break
            }
        }
    }

    private fun saveToMediaStore(file: File, format: ExportFormat) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.MIME_TYPE, if (format == ExportFormat.PNG_SEQUENCE) "image/png" else "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/EditStudio")
        }
        val uri = if (format == ExportFormat.PNG_SEQUENCE) {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        context.contentResolver.insert(uri, values)?.let { targetUri ->
            context.contentResolver.openOutputStream(targetUri)?.use { out ->
                file.inputStream().use { it.copyTo(out) }
            }
        }
    }
}

class ExportService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val CHANNEL_ID = "edit_studio_export"
        const val EXTRA_PROJECT_JSON = "extra_project_json"
        const val EXTRA_FORMAT = "extra_format"
        const val EXTRA_RES = "extra_res"
        const val EXTRA_FPS = "extra_fps"
        const val EXTRA_BITRATE = "extra_bitrate"

        private val _progressState = MutableStateFlow(ExportProgress())
        val progressState: StateFlow<ExportProgress> = _progressState.asStateFlow()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Edit Studio Export")
            .setContentText("Aapka video render ho raha hai...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .build()
        startForeground(101, notification)

        val json = intent?.getStringExtra(EXTRA_PROJECT_JSON)
        if (json != null) {
            val project = ProjectSerializer.deserializeProject(json)
            val format = ExportFormat.entries.firstOrNull { it.name == intent.getStringExtra(EXTRA_FORMAT) } ?: ExportFormat.MP4_H264
            val res = VideoResolution.entries.firstOrNull { it.name == intent.getStringExtra(EXTRA_RES) } ?: VideoResolution.FHD_1080P
            val fps = FrameRate.entries.firstOrNull { it.name == intent.getStringExtra(EXTRA_FPS) } ?: FrameRate.FPS_30
            val bitrate = intent.getIntExtra(EXTRA_BITRATE, 12)

            serviceScope.launch {
                _progressState.value = ExportProgress(isExporting = true, progress = 0f)
                val exporter = VideoExporter(applicationContext)
                val result = exporter.exportProject(project, ExportConfig(format, res, fps, bitrate)) { p ->
                    _progressState.value = ExportProgress(isExporting = true, progress = p)
                }
                result.onSuccess { path ->
                    _progressState.value = ExportProgress(isExporting = false, progress = 1f, outputPath = path)
                }.onFailure { err ->
                    _progressState.value = ExportProgress(isExporting = false, error = err.message ?: "Export error")
                }
                stopSelf()
            }
        } else {
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(CHANNEL_ID, "Video Export", NotificationManager.IMPORTANCE_LOW)
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
