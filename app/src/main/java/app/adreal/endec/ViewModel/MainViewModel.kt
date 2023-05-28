package app.adreal.endec.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import app.adreal.endec.Database.Database
import app.adreal.endec.Model.File
import app.adreal.endec.Repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    var filesData: LiveData<List<File>>
    var filesList = mutableListOf<File>()

    private val repository: Repository

    init {
        val dao = Database.getDatabase(application).dao()
        repository = Repository(dao)
        filesData = repository.readAll
    }

    fun readAllForExtension(mimeTypes : List<String>) : LiveData<List<File>>{
        return repository.readAllForExtension(mimeTypes)
    }

    fun add(item: File) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addUser(item)
        }
    }

    fun delete(item: File) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(item)
        }
    }
}