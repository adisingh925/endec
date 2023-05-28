package app.adreal.endec.Repository

import androidx.lifecycle.LiveData
import app.adreal.endec.Dao
import app.adreal.endec.Model.File

class Repository(private val dao: Dao) {

    val readAll = dao.readAll()

    fun readAllForExtension(mimeTypes : List<String>) : LiveData<List<File>>{
        return dao.readAllForExtension(mimeTypes)
    }

    fun addUser(item: File) {
        dao.add(item)
    }

    suspend fun update(item: File) {
        dao.update(item)
    }

    suspend fun delete(item: File) {
        dao.delete(item)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }
}