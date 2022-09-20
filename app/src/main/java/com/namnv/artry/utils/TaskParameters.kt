package com.namnv.artry.utils

import android.graphics.Bitmap
import android.widget.ImageView


class TaskParameters(a: Array<IntArray>, b: IntArray, c: IntArray, d: ImageView, e: Bitmap) {

    var MapMatrix: Array<IntArray> = a
    var current: IntArray = b
    var destination: IntArray = c
    var temp: Bitmap = e
    var map: ImageView = d

    companion object {
        fun setStoreMap(storeMap: Bitmap?) {
            if (storeMap != null) {
                TaskParameters.storeMap = storeMap
            }
        }

        fun getStoreMap(): Bitmap = storeMap

        private lateinit var storeMap: Bitmap

    }

    fun getStoreMap(): Bitmap = storeMap

}