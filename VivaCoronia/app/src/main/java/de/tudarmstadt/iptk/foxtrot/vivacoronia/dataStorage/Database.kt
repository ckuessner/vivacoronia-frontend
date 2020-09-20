package de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage

import android.content.Context
import androidx.room.*
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities.AchievementInfo
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities.DBLocation
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities.QuizGame
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities.User

@Database(entities = [User::class, DBLocation::class, QuizGame::class, AchievementInfo::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun coronaDao() : CoronaDao
    abstract fun quizGameDao() : QuizGameDao

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

