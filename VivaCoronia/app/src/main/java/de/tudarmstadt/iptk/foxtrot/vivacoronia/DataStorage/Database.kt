package de.tudarmstadt.iptk.foxtrot.vivacoronia.DataStorage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.tudarmstadt.iptk.foxtrot.vivacoronia.DataStorage.Entities.DBLocation
import de.tudarmstadt.iptk.foxtrot.vivacoronia.DataStorage.Entities.User

@Database(entities = arrayOf(User::class, DBLocation::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun coronaDao() : CoronaDao

    // define a Singleton so that only on DB exists
    companion object{

        @Volatile   // updates are visible for all threads directly
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase{
            val tmp = INSTANCE
            if (tmp != null){
                return tmp
            }
            synchronized(this){
                val instance = Room.databaseBuilder(context, AppDatabase::class.java, "my_database").build()
                INSTANCE = instance
                return instance
            }
        }
    }


}

