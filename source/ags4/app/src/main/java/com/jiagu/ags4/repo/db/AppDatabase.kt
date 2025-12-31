package com.jiagu.ags4.repo.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LocalGroup::class,
        LocalBlock::class,
        LocalParam::class,
        LocalPlan::class,
        LocalSortie::class,
        LocalNoFlyZone::class,
        LocalBlockBreakpoint::class
    ],
    version = 7
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groupDao(): LocalGroupDao
    abstract fun blockDao(): LocalBlockDao
    abstract fun paramDao(): LocalParamDao
    abstract fun planDao(): LocalPlanDao
    abstract fun sortieDao(): LocalSortieDao
    abstract fun noFlyZoneDao(): LocalNoFlyZoneDao
    abstract fun blockBreakpointDao(): LocalBlockBreakpointDao
}