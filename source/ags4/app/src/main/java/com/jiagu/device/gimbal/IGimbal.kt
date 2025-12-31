package com.jiagu.device.gimbal

import java.io.File

interface IGimbal {

    companion object {
        const val CAP_CENTER = 0 //云台居中
        const val CAP_DOWN = 1 //垂直下视
        const val CAP_ZOOM = 2 //变焦
        const val CAP_TAKE_PHOTO = 3 //拍照
        const val CAP_RECORD = 4 //录像
        const val CAP_SWITCH_SOURCE = 5 //切换镜头
        const val CAP_MOVE_POINT = 6 //指点移动
        const val CAP_TARGET_TRACKING = 7 //目标跟踪
        const val CAP_LASER_DIST = 8 //激光测距
        const val CAP_TARGET_FOLLOW = 9 //开始跟随
        const val CAP_MODE_FOLLOW = 10 //跟随模式
        const val CAP_MODE_LOCK = 11 //锁定模式
        const val CAP_RECV_PHOTO = 12 //接收拍照

        const val CTL_CENTER = 0
        const val CTL_DOWN = 1
        const val CTL_STOP = 2
    }

    class VideoSource(val name: String, val url: String, width: Int, height: Int, fps: Int)
    fun Capabilities(): IntArray

    fun getSources(): Array<VideoSource>
    fun toggleSource() {}

    // center/down
    fun gimbalCtrl(cmd: Int) {}
    // unit: degree/s
    fun gimbalMove(yaw: Float, pitch: Float, roll: Float) {}
    fun zoomCtrl(zoom: Int, param: Int) {}
    fun gimbalMovePoint(x: Float, y: Float) {}

    fun startTracking(x1: Float, y1: Float, x2: Float, y2: Float) {}
    fun stopTracking() {}

    fun takePhoto() {}
    fun getPhoto(file: File, done: (File) -> Unit) {}
    fun recording(start: Boolean) {}
}
