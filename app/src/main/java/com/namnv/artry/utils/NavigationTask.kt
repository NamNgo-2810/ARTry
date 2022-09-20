package com.namnv.artry.utils

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import android.widget.ImageView
import androidx.annotation.RequiresApi
import java.util.*

class NavigationTask :
    AsyncTask<TaskParameters, Node, String>() {
    private var temp: Bitmap? = null
    private var paint: Paint? = null
    private var canvas: Canvas? = null
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("ResourceAsColor")
    protected override fun doInBackground(vararg taskParameters: TaskParameters): String? {
        Log.d("NavigationTask", "doInBackground")
        val mapMatrix = taskParameters[0].MapMatrix
        map = taskParameters[0].map
        val current = taskParameters[0].current
        val destination = taskParameters[0].destination
        temp = TaskParameters.getStoreMap()
        for (i in 0 until temp!!.width) {
            for (j in 0 until temp!!.height) {
                temp!!.setPixel(i, j, 0)
            }
        }
        val aStar = PathFinding(mapMatrix, current, destination)
        val result: Boolean = aStar.search()
        println(result)
        var path: Deque<Node> = ArrayDeque()
        if (result) path = aStar.findPath()
        try {
            Thread.sleep(500)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        paint = Paint(Paint.ANTI_ALIAS_FLAG) // 画笔抗锯齿
        val color = Color.parseColor("#008B00")
        paint!!.color = color
        if (canvas == null) {
            Log.d("canvas", "newCanvas")
            canvas = Canvas(temp!!)
        }
        canvas!!.drawCircle(current[0].toFloat(), current[1].toFloat(), 2.1f, paint!!)
        while (!path.isEmpty()) {
            try {
                Thread.sleep(1)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            val curretloc = path.pollLast()
            publishProgress(curretloc)
        }
        canvas!!.drawCircle(destination[0].toFloat(), destination[1].toFloat(), 2.1f, paint!!)
        return null
    }

    protected override fun onProgressUpdate(vararg nodes: Node) {
        Log.d("NavigationTask", "ProgressUpdate")
        //bitmap should be mutable
        //location画圆
        canvas!!.drawCircle(
            nodes[0].getRow().toFloat(), nodes[0].getCol().toFloat(), 1.3f,
            paint!!
        )
        map!!.setImageBitmap(temp)
        //super.onProgressUpdate(values);
    }

    override fun onPostExecute(s: String?) {
        Log.d("NavigationTask", "PostExecute")
        map!!.setImageBitmap(temp)
        //You cannot recycle the Bitmap while using it on in the UI, the Bitmap has to be kept in memory.
        // Android will in most cases handle recycling just fine,
        // but if you need to recycle yourself you need to make sure to not use the Bitmap instance afterwards
        // (as in this case where the Bitmap instance will be rendered later on).
        //imageview持有bitmap引用
//        if (!temp.isRecycled()) {
//            temp.recycle();
//        }
        super.onPostExecute(s)
    }

    override fun onCancelled() {
        Log.d("NavigationTask", "onCancelled")
        super.onCancelled()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var map: ImageView? = null
    }
}
