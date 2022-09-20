package com.namnv.artry.utils

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.ImageView
import android.graphics.Bitmap.Config.RGBA_F16
import android.os.Build
import androidx.annotation.RequiresApi

//
//import android.app.Service
//import android.content.Intent
//import android.graphics.Bitmap
//import android.graphics.Bitmap.Config.RGBA_F16
//import android.graphics.drawable.BitmapDrawable
//import android.os.Binder
//import android.os.Build
//import android.os.IBinder
//import android.widget.ImageView
//import androidx.annotation.RequiresApi
//
//
//class NavigationService() : Service() {
//
//
//    private lateinit var bitmap: Bitmap
//    private lateinit var temp: Bitmap
//    private lateinit var MapMatrix: Array<IntArray>
//    private lateinit var navigationTask: NavigationTask
//    private val mBinder = PathFindBinder()
//
//    override fun onBind(p0: Intent?): IBinder? {
//        return mBinder
//    }
//
//    override fun onDestroy() {
//        navigationTask.cancel(true)
//        super.onDestroy()
//    }
//
//    override fun onUnbind(intent: Intent?): Boolean {
//        return super.onUnbind(intent)
//    }
//
//    fun convertBitmap(bitmap: Bitmap): Bitmap {
//        return Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    fun imageToArray(map: ImageView) {
////        bitmap = convertBitmap(BitmapFactory.decodeResource(resources, com.namnv.artry.R.drawable.rabimap))
//        bitmap = (map.drawable as BitmapDrawable).bitmap
//        temp = bitmap.copy(RGBA_F16, true)
//
//        val height = bitmap.height
//        val width = bitmap.width
////        Log.i("width", width.toString())
////        Log.i("height", height.toString())
//        MapMatrix = Array(width) { IntArray(height) }
//        var count = 0
//        for (i in 0 until width) {
//            for (j in 0 until height) {
//                val pixel: Int = bitmap.getPixel(i, j)
//                count++
//                if (pixel == -1) {
//                    MapMatrix[i][j] = 0
//                }
//                else MapMatrix[i][j] = 1
//                temp.setPixel(i, j, 0)
//            }
//        }
//        map.setImageBitmap(temp)
//        TaskParameters.setStoreMap(temp)
//    }
//
//    fun findPath(map: ImageView, current: IntArray, destination: IntArray) {
//        val navigationTask = NavigationTask()
//        val taskParameters = TaskParameters(MapMatrix, current, destination, map, TaskParameters.getStoreMap())
//        navigationTask.execute(taskParameters)
//    }
//
//    inner class PathFindBinder: Binder() {
//
//        fun getService(): NavigationService {
//            return this@NavigationService
//        }
//
//    }
//}


class NavigationService : Service() {
    lateinit var MapMatrix: Array<IntArray>
    lateinit var bitmap: Bitmap
    lateinit var temp: Bitmap
    var taskParameters: TaskParameters? = null
    var navigationTask: NavigationTask? = null
    private val mBinder: PathFindBinder = PathFindBinder()
    override fun onCreate() {
        super.onCreate()
        Log.d("NavigationService", "Start")
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        Log.d("NavigationService", "onbind")
        return mBinder
    }

    override fun onDestroy() {
        Log.d("NavigationService", "Destroy")
        if (navigationTask != null) navigationTask!!.cancel(true)
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d("NavigationService", "unbind")
        return super.onUnbind(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun imageToArray(map: ImageView) {
        bitmap = (map.drawable as BitmapDrawable).bitmap
        temp = bitmap.copy(RGBA_F16, true)
        val height = bitmap.height
        val width = bitmap.width

        Log.i("width", width.toString())
        Log.i("height", height.toString())
        MapMatrix = Array(width) { IntArray(height) }
        var count = 0
        for (i in 0 until width) {
            for (j in 0 until height) {
                val pixel = bitmap.getPixel(i, j)
                count++
                if (pixel == -1) {
                    MapMatrix[i][j] = 0
                } else MapMatrix[i][j] = 1
                temp.setPixel(i, j, 0)
            }
        }
        map.setImageBitmap(temp)
        TaskParameters.setStoreMap(temp)
    }

    fun findPath(map: ImageView, current: IntArray, destination: IntArray) {
        navigationTask = NavigationTask()
        taskParameters = TaskParameters(
            MapMatrix, current,
            destination, map, TaskParameters.getStoreMap()
        )
        navigationTask!!.execute(taskParameters)
    }

    internal inner class PathFindBinder : Binder() {
        fun getService(): NavigationService {
            return this@NavigationService
        }
    }
}