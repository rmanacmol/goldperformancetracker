package com.renz.golfperformancetracker.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.renz.golfperformancetracker.data.local.entity.PlayerEntity
import com.renz.golfperformancetracker.data.local.entity.PlayerWithShotsEntity
import com.renz.golfperformancetracker.data.local.entity.ShotEntity
import com.renz.golfperformancetracker.data.local.entity.SyncMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Query("SELECT * FROM players ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE id = :playerId LIMIT 1")
    fun observeById(playerId: String): Flow<PlayerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(players: List<PlayerEntity>)

    @Query("SELECT COUNT(*) FROM players")
    suspend fun count(): Int
}

@Dao
interface ShotDao {

    @Query("SELECT * FROM shots WHERE playerId = :playerId ORDER BY recordedAt DESC")
    fun observeByPlayer(playerId: String): Flow<List<ShotEntity>>

    @Query("SELECT * FROM shots WHERE playerId = :playerId ORDER BY recordedAt DESC")
    fun pagingSource(playerId: String): PagingSource<Int, ShotEntity>

    @Query("SELECT * FROM shots WHERE playerId = :playerId ORDER BY recordedAt DESC")
    suspend fun getAllByPlayer(playerId: String): List<ShotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(shots: List<ShotEntity>)

    @Query("DELETE FROM shots WHERE playerId = :playerId")
    suspend fun deleteByPlayer(playerId: String)
}

@Dao
interface PlayerDetailDao {

    @Transaction
    @Query("SELECT * FROM players WHERE id = :playerId LIMIT 1")
    fun observePlayerWithShots(playerId: String): Flow<PlayerWithShotsEntity?>
}

@Dao
interface SyncMetadataDao {

    @Query("SELECT * FROM sync_metadata WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): SyncMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: SyncMetadataEntity)
}
