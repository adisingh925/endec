package app.adreal.endec.ViewModel

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import app.adreal.endec.Database.Database
import app.adreal.endec.Encryption.Encryption
import app.adreal.endec.Model.File
import app.adreal.endec.Repository.Repository

class MainViewModel(application: Application) : AndroidViewModel(application) {

    var filesData: LiveData<List<File>>

    private val repository: Repository

    init {
        val dao = Database.getDatabase(application).dao()
        repository = Repository(dao)
        filesData = repository.readAll
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun encryptData(data: ByteArray): ByteArray {
        return Encryption(getApplication<Application>().applicationContext).encryptUsingSymmetricKey(
            data
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun decrypt(data: ByteArray): ByteArray {
        return Encryption(getApplication<Application>().applicationContext).decryptUsingSymmetricKey(
            data
        )
    }

    fun monitorFile(context: Context) {
        app.adreal.endec.File.File().monitorFileList(context)
    }
}