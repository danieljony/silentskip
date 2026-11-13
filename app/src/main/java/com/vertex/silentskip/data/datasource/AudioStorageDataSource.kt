package com.vertex.silentskip.data.datasource

import android.content.Context
import android.media.MediaMetadataRetriever
import com.vertex.silentskip.common.Logger
import com.vertex.silentskip.domain.model.AudioRecordModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val RECORDINGS_SUB_DIR = "recordings"
private const val DEFAULT_EXTENSION = "wav"
private const val EXT_M4A = ".m4a"
private const val EXT_3GP = ".3gp"
private const val EXT_WAV = ".wav"
private const val DATE_PATTERN = "dd-MM-yyyy - HH-mm"
private const val DISPLAY_DATE_PATTERN = "dd/MM/yyyy HH:mm"

class AudioStorageDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val recordingsDir: File by lazy {
        val dir = File(context.filesDir, RECORDINGS_SUB_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }

    private val _recordingsFlow = MutableStateFlow<List<AudioRecordModel>>(emptyList())
    val recordingsFlow: StateFlow<List<AudioRecordModel>> = _recordingsFlow.asStateFlow()

    init {
        refreshRecordings()
    }

    fun generateNewRecordingFile(extension: String = DEFAULT_EXTENSION): File {
        val formatter = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
        val timestampName = formatter.format(Date())
        var file = File(recordingsDir, "$timestampName.$extension")
        var count = 1
        while (file.exists()) {
            file = File(recordingsDir, "$timestampName ($count).$extension")
            count++
        }
        return file
    }

    fun refreshRecordings() {
        val files = recordingsDir.listFiles { _, name ->
            name.endsWith(EXT_M4A) || name.endsWith(EXT_3GP) || name.endsWith(EXT_WAV)
        } ?: emptyArray()

        val list = files.mapNotNull { file ->
            createRecordModelFromFile(file)
        }.sortedByDescending { it.timestamp }

        _recordingsFlow.value = list
    }

    fun getRecordings(): List<AudioRecordModel> {
        refreshRecordings()
        return _recordingsFlow.value
    }

    fun renameRecording(id: String, newNameRaw: String): Boolean {
        val currentList = _recordingsFlow.value
        val model = currentList.find { it.id == id } ?: return false
        val oldFile = File(model.filePath)
        if (!oldFile.exists()) return false

        val cleanName = newNameRaw.trim()
            .removeSuffix(EXT_M4A)
            .removeSuffix(EXT_3GP)
            .removeSuffix(EXT_WAV)
        val extension = oldFile.extension.ifBlank { DEFAULT_EXTENSION }
        val newFile = File(oldFile.parentFile, "$cleanName.$extension")

        val success = oldFile.renameTo(newFile)
        if (success) {
            refreshRecordings()
        }
        return success
    }

    fun deleteRecording(id: String): Boolean {
        val currentList = _recordingsFlow.value
        val model = currentList.find { it.id == id } ?: return false
        val file = File(model.filePath)
        val success = if (file.exists()) file.delete() else true
        if (success) {
            refreshRecordings()
        }
        return success
    }

    private fun createRecordModelFromFile(file: File): AudioRecordModel? {
        if (!file.exists()) return null
        val duration = extractDurationMs(file)
        val dateFormatter = SimpleDateFormat(DISPLAY_DATE_PATTERN, Locale.getDefault())
        val formattedDate = dateFormatter.format(Date(file.lastModified()))

        return AudioRecordModel(
            id = file.absolutePath,
            name = file.nameWithoutExtension,
            filePath = file.absolutePath,
            durationMs = duration,
            sizeBytes = file.length(),
            createdAtFormatted = formattedDate,
            timestamp = file.lastModified()
        )
    }

    private fun extractDurationMs(file: File): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Logger.e("Error extracting audio duration", e)
            0L
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Logger.e("Error releasing MediaMetadataRetriever", e)
            }
        }
    }
}
