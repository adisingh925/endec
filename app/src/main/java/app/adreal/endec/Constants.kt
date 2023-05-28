package app.adreal.endec

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.MenuInflater
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

object Constants {
    const val AES_KEY = "aesKey"
    const val HOME_DIRECTORY = "/android/media"
    const val DATABASE_NAME = "FilesDatabase"
    const val DEFAULT_ID = 0
    const val PICKER_ID = "*/*"
    const val RECYCLERVIEW_SPAN_COUNT = 3
    const val TABLE_NAME = "FileTable"
    const val PROVIDER = ".provider"
    const val ENCRYPTED_FILE_EXTENSION = ".aes"

    val imageMimeTypes = listOf(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/gif",
        "image/bmp",
        "image/webp",
        "image/svg+xml",
        "image/heic",
        "image/heif",
        "image/x-icon"
    )

    val videoMimeTypes = listOf(
        "video/mp4",
        "video/3gpp",
        "video/x-matroska",
        "video/x-msvideo",
        "video/quicktime"
    )

    val audioMimeTypes = listOf(
        "audio/mpeg",
        "audio/ogg",
        "audio/wav",
        "audio/amr",
        "audio/aac"
    )

    val documentMimeTypes = listOf(
        "application/pdf",
        "application/msword",
        "application/vnd.ms-excel",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "application/vnd.oasis.opendocument.text",
        "text/plain",
        "text/html",
        "text/xml"
    )

    val apkMimeTypes = listOf(
        "application/vnd.android.package-archive"
    )

    fun getFilesDirectoryPath(context: Context): String {
        return context.getExternalFilesDir("")?.path.toString()
    }

    fun getTempFileDirectory(context: Context): File {
        return context.cacheDir
    }

    private fun share(context: Context, fileName: String, type: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val myIntent = Intent(Intent.ACTION_SEND)
            myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            myIntent.type = type
            myIntent.putExtra(
                Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    context,
                    (context.packageName) + PROVIDER,
                    File(getFilesDirectoryPath(context), fileName)
                )
            )
            startActivity(context, Intent.createChooser(myIntent, "Share Options"), null)
        }
    }

    fun showPopup(v: View, context: Context, fileData: app.adreal.endec.Model.File) {
        val popup = PopupMenu(context, v)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu, popup.menu)
        popup.show()

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.share -> {
                    share(context, fileData.fileName, fileData.extension)
                }
            }
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun openFile(context: Context, fileData: app.adreal.endec.Model.File) {
        CoroutineScope(Dispatchers.IO).launch {
            val intent = Intent(Intent.ACTION_VIEW)
                .setDataAndType(
                    FileProvider.getUriForFile(
                        context,
                        (context.packageName) + PROVIDER,
                        File(
                            app.adreal.endec.File.File().createTempFile(context, fileData)
                        )
                    ),
                    fileData.extension
                ).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(context, intent, null)
        }
    }
}