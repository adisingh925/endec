package app.adreal.endec.File

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import androidx.annotation.RequiresApi
import app.adreal.endec.Constants
import app.adreal.endec.Database.Database
import app.adreal.endec.Encryption.Encryption
import app.adreal.endec.Model.metaData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow


class File {

    companion object {
        const val MIME_TYPE = "MIME Type"
        const val FILE_DATA = "File Data"
        const val DISPLAY_NAME = "Display Name"
        const val SIZE = "Size"
    }

    private fun getMIMEType(uri: Uri, contentResolver: ContentResolver) : String {
        return contentResolver.getType(uri).toString()
    }

    private fun dumpImageMetaData(uri: Uri, contentResolver: ContentResolver): metaData {
        val fileData = metaData("", 0)
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayName: String =
                    it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                Log.d(FILE_DATA, "$DISPLAY_NAME $displayName")
                val sizeIndex: Int = it.getColumnIndex(OpenableColumns.SIZE)
                val size: String = if (!it.isNull(sizeIndex)) {
                    it.getString(sizeIndex)
                } else {
                    "Unknown"
                }
                Log.d(FILE_DATA, "$SIZE $size")
                fileData.name = displayName
                fileData.size = size.toLong()
            }
        }

        return fileData
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun readFile(uri: Uri, contentResolver: ContentResolver, context: Context) {
        val inputStream = contentResolver.openInputStream(uri)
        val fileData = dumpImageMetaData(uri, contentResolver)
        val f = File(Constants.getFilesDirectoryPath(context), fileData.name)

        CoroutineScope(Dispatchers.IO).launch {
            if (!f.exists()) {
                withContext(Dispatchers.IO) {
                    f.createNewFile()
                }
            }
        }.invokeOnCompletion {
            CoroutineScope(Dispatchers.IO).launch {
                if (inputStream != null) {
                    withContext(Dispatchers.IO) {
                        Encryption(context).encryptUsingSymmetricKey(
                            FileOutputStream(f),
                            inputStream.readBytes()
                        )
                    }
                }
            }.invokeOnCompletion {
                inputStream?.close()
                Database.getDatabase(context).dao().add(
                    app.adreal.endec.Model.File(
                        uri.lastPathSegment.toString(),
                        fileData.name,
                        fileData.size,
                        getMIMEType(uri, contentResolver).substringAfterLast("/"),
                        System.currentTimeMillis()
                    )
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createTempFile(context: Context, fileData : app.adreal.endec.Model.File) : String{
        val outputFile = File.createTempFile(fileData.fileName + "_decrypted", ".${fileData.extension}", context.cacheDir)
        val fos = FileInputStream(File(Constants.getFilesDirectoryPath(context), fileData.fileName))
        FileOutputStream(outputFile).use {
            it.write(Encryption(context).decryptUsingSymmetricKey(fos))
        }

        return outputFile.path
    }

    fun fileSize(size2:Int):String {
        val size = size2.toLong()
        if (size <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }
}