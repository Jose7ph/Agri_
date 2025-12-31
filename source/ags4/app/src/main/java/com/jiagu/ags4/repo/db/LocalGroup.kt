package com.jiagu.ags4.repo.db

import androidx.room.*

@Entity(tableName = "team")
class LocalGroup(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var _id: Long,

    @ColumnInfo(name = "team_id") var groupId: Long,
    @ColumnInfo(name = "team_name") var groupName: String
) {
    constructor(gid: Long, name: String) : this(0, gid, name)
}

@Dao
interface LocalGroupDao {
    @Query("SELECT * FROM team")
    fun getGroups(): List<LocalGroup>

    @Query("DELETE FROM team")
    fun removeAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(groups: List<LocalGroup>)
}