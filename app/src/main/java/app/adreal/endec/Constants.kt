package app.adreal.endec

import android.content.Context

object Constants {
    const val AES_KEY = "aesKey"
    const val HOME_DIRECTORY = "/android/media"
    const val DATABASE_NAME = "FilesDatabase"
    const val DEFAULT_ID = 0
    const val PICKER_ID = "image/*"
    const val RECYCLERVIEW_SPAN_COUNT = 3
    const val TABLE_NAME = "FileTable"

    fun getFilesDirectoryPath(context: Context) : String{
        return context.getExternalFilesDir("")?.path.toString()
    }
}