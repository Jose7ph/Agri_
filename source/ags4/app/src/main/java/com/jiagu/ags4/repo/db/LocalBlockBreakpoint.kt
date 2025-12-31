package com.jiagu.ags4.repo.db

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query


@Entity(
    tableName = "block_breakpoint",
    indices = [
        Index(value = ["local_block_id"], unique = true),
        Index(value = ["create_time"])
    ]
)
@Keep
data class LocalBlockBreakpoint(
    @PrimaryKey @ColumnInfo(name = "local_block_id") var localBlockId: Long,
    @ColumnInfo(name = "breakpoint") var breakpoint: String,
    @ColumnInfo(name = "create_time") var createTime: Long = System.currentTimeMillis(),
)

@Dao
interface LocalBlockBreakpointDao {
    @Query("DELETE FROM block_breakpoint WHERE local_block_id = :localBlockId")
    fun delete(localBlockId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(blockBreakpoint: LocalBlockBreakpoint): Long

    @Query("SELECT * FROM block_breakpoint WHERE local_block_id = :localBlockId and create_time > (strftime('%s', 'now') - 7 * 24 * 60 * 60)")
    fun getLocalBlockBreakpoint(localBlockId: Long): LocalBlockBreakpoint
}