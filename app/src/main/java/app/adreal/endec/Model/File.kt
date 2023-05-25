package app.adreal.endec.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FileData")
data class File(
    @PrimaryKey val id : String,
    val fileName : String,
    val size : Long
)
