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
import app.adreal.endec.Model.KDFParameters
import app.adreal.endec.Model.metaData
import app.adreal.endec.SharedPreferences.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.text.DecimalFormat
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import kotlin.math.log10
import kotlin.math.pow

class File {

    companion object {
        const val FILE_DATA = "File Data"
        const val DISPLAY_NAME = "Display Name"
        const val SIZE = "Size"
    }

    private fun getMIMEType(uri: Uri, contentResolver: ContentResolver): String {
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
                fileData.name = displayName.substring(0, displayName.lastIndexOf('.'))
                fileData.size = size.toLong()
            }
        }

        return fileData
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun readFile(uri: Uri, contentResolver: ContentResolver, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val inputStream = contentResolver.openInputStream(uri)
            val fileData = dumpImageMetaData(uri, contentResolver)
            val file = File(Constants.getFilesDirectoryPath(context), fileData.name)
            val cos = withContext(Dispatchers.IO) {
                Encryption(context).encryptUsingSymmetricKey(
                    FileOutputStream(file, true),
                    Encryption(context).getCipher(Cipher.ENCRYPT_MODE)
                )
            }

            CoroutineScope(Dispatchers.IO).launch {
                if (!file.exists()) {
                    withContext(Dispatchers.IO) {
                        file.createNewFile()
                    }
                }
            }.invokeOnCompletion {
                try {
                    inputStream?.copyTo(cos)
                } catch (e: Exception) {
                    Log.d("File Write Exception", e.message.toString())
                } finally {
                    inputStream?.close()
                    cos.close()
                    appendTheDataToTheEnd(file)
                    Database.getDatabase(context).dao().add(
                        app.adreal.endec.Model.File(
                            uri.lastPathSegment.toString(),
                            fileData.name,
                            fileData.size,
                            getMIMEType(uri, contentResolver),
                            System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun appendTheDataToTheEnd(file: File) {
        CoroutineScope(Dispatchers.IO).launch {
            val salt = SharedPreferences.read("salt", "").toString()
            val iteration = SharedPreferences.read("iteration", 0).toString().padStart(6, '0')
            val memoryCost = SharedPreferences.read("memory_cost", 0).toString().padStart(10, '0')
            val hashLength = SharedPreferences.read("hash", 0).toString().padStart(6, '0')
            val version = SharedPreferences.read("version", "").toString().padStart(2, '0')
            val mode = SharedPreferences.read("mode", "").toString().padStart(2, '0')
            val parallelism = SharedPreferences.read("parallelism", 0).toString().padStart(2, '0')

            withContext(Dispatchers.IO) {
                FileOutputStream(
                    file,
                    true
                ).write((salt + iteration + memoryCost + hashLength + version + mode + parallelism).toByteArray())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createTempFile(context: Context, fileData: app.adreal.endec.Model.File): String {
        val outputFile = File.createTempFile(
            fileData.fileName + "_decrypted",
            ".${fileData.extension.substringAfter("/")}",
            Constants.getTempFileDirectory(context)
        )
        val file = File(Constants.getFilesDirectoryPath(context), fileData.fileName)
        val fis = FileInputStream(file)
        val cis = Encryption(context).decryptUsingSymmetricKey(
            fis,
            Encryption(context).getCipher(
                Cipher.DECRYPT_MODE,
                readTheEndOfFile(file)
            )
        )
        cis.copyTo(FileOutputStream(outputFile))
        cis.close()
        fis.close()
        return outputFile.path
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun readTheEndOfFile(
        file: File
    ): KDFParameters {
        val rfs = RandomAccessFile(
            file,
            "rw"
        )

        val newLength = rfs.length() - 44
        rfs.seek(newLength)
        val data = ByteArray(44)
        rfs.read(data)
        val retrievedData = data.decodeToString()

        val length = retrievedData.length

        val parallelism = retrievedData.substring(length - 2).dropWhile { it == '0' }
        val mode = retrievedData.substring(length - 4, length - 2).dropWhile { it == '0' }
        val version = retrievedData.substring(length - 6, length - 4).dropWhile { it == '0' }
        val hash = retrievedData.substring(length - 12, length - 6).dropWhile { it == '0' }
        val memoryCost = retrievedData.substring(length - 22, length - 12).dropWhile { it == '0' }
        val iteration = retrievedData.substring(length - 28, length - 22).dropWhile { it == '0' }
        val salt = retrievedData.substring(length - 44, length - 28)

        rfs.seek(newLength)
        rfs.setLength(newLength)
        rfs.close()

        return KDFParameters(salt, iteration, memoryCost, hash, version, mode, parallelism)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun decryptAndStoreInCache(
        uri: Uri,
        contentResolver: ContentResolver,
        context: Context
    ): String {
        val fileData = dumpImageMetaData(uri, contentResolver)

        val outputFile = File.createTempFile(
            fileData.name + "_decrypted",
            ".jpg",
            Constants.getTempFileDirectory(context)
        )

        val fos = FileOutputStream(outputFile)
        val cis = CipherOutputStream(fos, Encryption(context).getCipher(Cipher.DECRYPT_MODE))
        val inputStream = contentResolver.openInputStream(uri)

        readTheEndOfFile(File(uri.path.toString()))

        inputStream?.copyTo(cis)

        return outputFile.path
    }

    fun fileSize(size2: Long): String {
        if (size2 <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (log10(size2.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size2 / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }

    fun deleteFile(context: Context, fileData: app.adreal.endec.Model.File) {
        CoroutineScope(Dispatchers.IO).launch {
            val file = File(Constants.getFilesDirectoryPath(context), fileData.fileName)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}