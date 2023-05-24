package app.adreal.endec.FileObserver

import android.content.Context
import android.os.FileObserver
import android.util.Log

class FileObserver(path: String) : FileObserver(path, CREATE) {
    override fun onEvent(event: Int, path: String?) {
        if (path != null) {
            Log.e("FileObserver: ", "File Created")
        }
    }
}