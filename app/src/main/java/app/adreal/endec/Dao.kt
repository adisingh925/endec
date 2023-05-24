package app.adreal.endec

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.adreal.endec.Model.File

@Dao
interface Dao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun add(item : File)

    @Update
    suspend fun update(item : File)

    @Delete
    suspend fun delete(item : File)

    @Query("DELETE FROM FileData")
    suspend fun deleteAll()

    @Query("SELECT * from FileData")
    fun readAll() : LiveData<List<File>>

}