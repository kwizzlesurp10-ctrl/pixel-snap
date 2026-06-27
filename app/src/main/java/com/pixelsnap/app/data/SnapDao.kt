package com.pixelsnap.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SnapDao {
    @Query("SELECT * FROM snaps ORDER BY timestamp DESC")
    fun getAllSnaps(): Flow<List<Snap>>

    @Query("SELECT * FROM snaps WHERE id = :id")
    suspend fun getSnapById(id: String): Snap?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnap(snap: Snap)

    @Update
    suspend fun updateSnap(snap: Snap)

    @Delete
    suspend fun deleteSnap(snap: Snap)

    @Query("DELETE FROM snaps WHERE id = :id")
    suspend fun deleteById(id: String)
}
