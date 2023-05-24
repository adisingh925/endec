package app.adreal.endec.File

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class File {

    companion object {
        const val MIME_TYPE = "MIME Type"
        const val FILE_DATA = "File Data"
        const val DISPLAY_NAME = "Display Name"
        const val SIZE = "Size"
    }

    fun getMIMEType(uri: Uri, contentResolver: ContentResolver) {
        Log.d(MIME_TYPE, contentResolver.getType(uri).toString())
    }

    fun dumpImageMetaData(uri: Uri, contentResolver: ContentResolver) {
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayName: String = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                Log.d(FILE_DATA, "$DISPLAY_NAME $displayName")
                val sizeIndex: Int = it.getColumnIndex(OpenableColumns.SIZE)
                val size: String = if (!it.isNull(sizeIndex)) {
                    it.getString(sizeIndex)
                } else {
                    "Unknown"
                }
                Log.d(FILE_DATA, "$SIZE $size")
            }
        }
    }

    fun createOrGetFile(context: Context, fileName: String, data: ByteArray) {
        CoroutineScope(Dispatchers.IO).launch {
            val folder = context.getExternalFilesDir(null)
            val f = File(folder?.path, fileName)

            if (!f.exists()) {
                withContext(Dispatchers.IO) {
                    f.createNewFile()
                }
            }

            withContext(Dispatchers.IO) {
                FileOutputStream(f).use {
                    it.write(data)
                }
            }
        }
    }

    fun monitorFileList(context: Context) : List<String> {
        val filesList = mutableListOf<String>()
        for(i in context.getExternalFilesDir(null)?.list()!!){
            filesList.add(i)
        }

        return filesList.toList()
    }
}