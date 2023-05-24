package app.adreal.endec

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import app.adreal.endec.FileObserver.FileObserver
import app.adreal.endec.SharedPreferences.SharedPreferences

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SharedPreferences.init(this)

        val directoryFileObserver = FileObserver(this.getExternalFilesDir(null)!!.path)

        directoryFileObserver.startWatching()
    }
}