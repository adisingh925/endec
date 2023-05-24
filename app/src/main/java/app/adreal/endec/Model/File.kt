package app.adreal.endec.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FileData")
data class File(
    @PrimaryKey(autoGenerate = true) val id : Int,
    val fileName : String
)
