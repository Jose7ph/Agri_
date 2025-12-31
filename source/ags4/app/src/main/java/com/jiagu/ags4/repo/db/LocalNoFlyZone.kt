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
    tableName = "no_fly_zone",
    indices = [
        Index(value = ["nofly_id"], unique = true),
        Index(value = ["nofly_type"]),
        Index(value = ["detail_address"]),
        Index(value = ["lat"]),
        Index(value = ["lng"])
    ]
)
@Keep
data class LocalNoFlyZone(
    @PrimaryKey @ColumnInfo(name = "nofly_id") var noflyId: Long,

    @ColumnInfo(name = "effect_start_time") var effectStartTime: Long,
    @ColumnInfo(name = "orbit_str") var orbitStr: String,
    @ColumnInfo(name = "detail_address") var detailAddress: String,
    @ColumnInfo(name = "effect_end_time") var effectEndTime: Long,
    @ColumnInfo(name = "orbit") var orbit: String,
    @ColumnInfo(name = "nofly_type") var noflyType: Int,
    @ColumnInfo(name = "is_enable") var isEnable: Int,
    @ColumnInfo(name = "effect_status") var effectStatus: Int,
    //中心点
    @ColumnInfo(name = "lat") var lat: Double = 0.0,
    @ColumnInfo(name = "lng") var lng: Double = 0.0,
)

@Dao
interface LocalNoFlyZoneDao {
    @Query("DELETE FROM no_fly_zone")
    fun removeAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertList(noFlyZones: List<LocalNoFlyZone>): List<Long>

    @Query("SELECT * FROM no_fly_zone WHERE lat > :s AND lat < :n AND lng > :w AND lng < :e")
    fun getNoFlyZoneList(
        w: Double,
        e: Double,
        s: Double,
        n: Double,
    ): List<LocalNoFlyZone>
}