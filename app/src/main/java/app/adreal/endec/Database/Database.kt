package app.adreal.endec.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.adreal.endec.Constants
import app.adreal.endec.Model.File

@Database(entities = [File::class], version = 1, exportSchema = false)
abstract class Database : RoomDatabase() {

    abstract fun dao() : app.adreal.endec.Dao

    companion object{

        @Volatile
        private var INSTANCE : app.adreal.endec.Database.Database? = null

        fun getDatabase(context: Context):app.adreal.endec.Database.Database
        {
            val tempInstance = INSTANCE
            if(tempInstance!=null)
            {
                return tempInstance
            }
            synchronized(this)
            {
                val instance = Room.databaseBuilder(context.applicationContext,
                    app.adreal.endec.Database.Database::class.java, Constants.DATABASE_NAME).build()

                INSTANCE = instance
                return instance
            }
        }
    }
}