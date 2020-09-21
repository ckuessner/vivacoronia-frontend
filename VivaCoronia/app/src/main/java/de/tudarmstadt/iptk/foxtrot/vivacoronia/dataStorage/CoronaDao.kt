package de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage

import androidx.room.*
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities.AchievementInfo
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.entities.DBLocation

// create different DAOs for Entities if this class gets to crowded
@Dao
interface CoronaDao {

    // Queries
    @Query("SELECT * FROM dblocation")
    fun getLocations(): List<DBLocation>

    @Insert
    fun initAchievement(ach: AchievementInfo)

    @Query("SELECT * from achievementinfo")
    fun getAllAchievements() : List<AchievementInfo>

    @Update
    fun updateAchievement(AchievementInfo: AchievementInfo)



    // Data Update; if the data update is so fast that the time (which is the primary key) is the same, the new location can be taken directly because nobody can run so fast
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addLocation(DBLocation: DBLocation)

    @Delete
    fun deleteLocations(locations: List<DBLocation>)
}