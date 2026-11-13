package com.vertex.silentskip.presentation.action

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.vertex.silentskip.R
import com.vertex.silentskip.common.Logger
import com.vertex.silentskip.domain.model.AudioRecordModel
import java.io.File
import javax.inject.Inject

class ShareAudioAction @Inject constructor() {

    fun share(context: Context, recording: AudioRecordModel) {
        try {
            val file = File(recording.filePath)
            if (!file.exists()) {
                Logger.e("Cannot share audio file: File does not exist at ${recording.filePath}")
                return
            }

            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserTitle = context.getString(R.string.share_chooser_title)
            val chooserIntent = Intent.createChooser(shareIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            Logger.e("Failed to share audio recording", e)
        }
    }
}
