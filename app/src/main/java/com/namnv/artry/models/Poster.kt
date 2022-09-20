package com.namnv.artry.models

import android.os.Parcel
import android.os.Parcelable

class Poster() : Parcelable {
    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private lateinit var rotation: Pair<Int, Int>
    private lateinit var direction: String

    constructor(parcel: Parcel) : this() {
        this.lat = parcel.readDouble()
        this.lng = parcel.readDouble()

        rotation = Pair(parcel.readInt(), parcel.readInt())
    }

    constructor(lat: Double, lng: Double, rotation: Pair<Int, Int>) : this() {
        this.lat = lat
        this.lng = lng
        this.rotation = rotation
        this.direction = setDirection(rotation)
    }

    fun getLat(): Double {
        return this.lat
    }

    fun getLng(): Double {
        return this.lng
    }

    fun getRotation(): Pair<Int, Int> {
        return this.rotation
    }

    fun getDirection(): String {
        return this.direction
    }

    fun setDirection(rotation: Pair<Int, Int>): String {
        return if (rotation.first != 0) {
            if (rotation.first > 0) "E" else "W"
        } else {
            if (rotation.second > 0) "N" else "S"
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Poster> {
        override fun createFromParcel(parcel: Parcel): Poster {
            return Poster(parcel)
        }

        override fun newArray(size: Int): Array<Poster?> {
            return arrayOfNulls(size)
        }
    }
}