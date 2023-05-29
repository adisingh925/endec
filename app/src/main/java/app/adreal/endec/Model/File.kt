package app.adreal.endec.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FileData")
data class File(
    @PrimaryKey val id : String,
    val isParametersAppended : Boolean,
    val fileName : String,
    val size : Long,
    val mimeType : String,
    val insertTime : Long,
    val salt : String,
    val iteration : String,
    val memoryCost : String,
    val hashLength : String,
    val version : String,
    val mode : String,
    val parallelism : String
)
