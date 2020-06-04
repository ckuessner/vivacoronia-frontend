package de.tudarmstadt.iptk.foxtrot.vivacoronia.DataStorage

import androidx.room.*
import de.tudarmstadt.iptk.foxtrot.vivacoronia.DataStorage.Entities.DBLocation

// create different DAOs for Entities if this class gets to crowded
@Dao
interface CoronaDao {

    // Queries
    @Query("SELECT * FROM dblocation")
    fun getLocations(): List<DBLocation>

    @Query("SELECT userID FROM user")
    fun getUserID(): Int

    // Data Update; if the data update is so fast that the time (which is the primary key) is the same, the new location can be taken directly because nobody can run so fast
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addLocation(DBLocation: DBLocation)

    @Delete
    fun deleteLocations(locations: List<DBLocation>)
}