package com.jiagu.ags4.repo.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "param")
data class LocalParam(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var _id: Long,

    @ColumnInfo(name = "type") var type: Int,
    @ColumnInfo(name = "param_name") var paramName: String,
    @ColumnInfo(name = "param") var param: String,
){

    @ColumnInfo(name = "is_local") var isLocal: Boolean = true
    @ColumnInfo(name = "update_time") var updateTime: Long = System.currentTimeMillis() / 1000
    @ColumnInfo(name = "param_id") var paramId: Long = 0L
    @ColumnInfo(name = "user_id") var userId: Long = 0L
    @ColumnInfo(name = "is_update") var isUpload: Boolean = false
    @ColumnInfo(name = "is_delete") var isDelete: Boolean = false
}

@Dao
interface LocalParamDao {
    //获取所有参数
    @Query("SELECT * FROM param")
    fun getParams(): List<LocalParam>

    //获取所有本地参数
    @Query("SELECT * FROM param WHERE is_local = 1")
    fun getLocalParams(): List<LocalParam>

    //获取所有需要上传的参数
    @Query("SELECT * FROM param WHERE is_update = 1")
    fun getNeedUploadParams(): List<LocalParam>

    //获取所有需要删除的参数
    @Query("SELECT * FROM param WHERE is_delete = 1")
    fun getNeedDeleteParams(): List<LocalParam>

    //获取所有需要上传的参数
    @Query("SELECT * FROM param WHERE is_update = 1 AND is_local = 1")
    fun getNeedUploadLocalParams(): List<LocalParam>

    //获取所有需要删除的参数
    @Query("SELECT * FROM param WHERE is_delete = 1 AND is_local = 1")
    fun getNeedDeleteLocalParams(): List<LocalParam>

    //获取所有需要上传的参数
    @Query("SELECT * FROM param WHERE is_update = 1 AND is_local = 0")
    fun getNeedUploadRemoteParams(): List<LocalParam>

    //获取所有需要删除的参数
    @Query("SELECT * FROM param WHERE is_delete = 1 AND is_local = 0")
    fun getNeedDeleteRemoteParams(): List<LocalParam>

    //获取所有需要上传的参数
    @Query("SELECT * FROM param WHERE is_update = 1 AND is_local = 1 AND type = :type")
    fun getNeedUploadLocalParamsByType(type: Int): List<LocalParam>

    //获取所有需要删除的参数
    @Query("SELECT * FROM param WHERE is_delete = 1 AND is_local = 1 AND type = :type")
    fun getNeedDeleteLocalParamsByType(type: Int): List<LocalParam>

    //获取所有需要上传的参数
    @Query("SELECT * FROM param WHERE is_update = 1 AND is_local = 0 AND type = :type")
    fun getNeedUploadRemoteParamsByType(type: Int): List<LocalParam>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(param: LocalParam): Long

    @Query("DELETE FROM param WHERE _id = :id")
    fun removeParam(id: Long)

    @Query("select * from param where type = :type and user_id = :userId")
    fun getParamsByType(type: Int, userId: Long): List<LocalParam>

    @Query("select * from param where _id = :id")
    fun getParamById(id: Long): LocalParam

    @Query("select * from param where param_id = :id")
    fun getParamByParamId(id: Long): LocalParam

    @Query("delete from param where type = :type and is_local = 0 and user_id = :userId")
    fun deleteRemoteParamsByType(type: Int, userId: Long )

    @Query("update param set param=:param, param_name=:paramName, is_delete = :isDelete, is_update = :isUpdate, is_local = :isLocal where _id = :id")
    fun updateParamById(id: Long, paramName: String, param: String, isLocal: Boolean, isUpdate: Boolean, isDelete: Boolean)
}