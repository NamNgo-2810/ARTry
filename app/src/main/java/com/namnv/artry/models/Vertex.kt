package com.namnv.artry.models

import android.os.Parcel
import android.os.Parcelable

class Vertex() : Parcelable {

    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private lateinit var rotation: Pair<Int, Int>

    constructor(parcel: Parcel) : this() {
        this.lat = parcel.readDouble()
        this.lng = parcel.readDouble()
        rotation = Pair(parcel.readInt(), parcel.readInt())
    }

    constructor(lat: Double, lng: Double, rotation: Pair<Int, Int>) : this() {
        this.lat = lat
        this.lng = lng
        this.rotation = rotation
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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(this.lat)
        parcel.writeDouble(this.lng)
        parcel.writeInt(rotation.first)
        parcel.writeInt(rotation.second)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Vertex> {
        override fun createFromParcel(parcel: Parcel): Vertex {
            return Vertex(parcel)
        }

        override fun newArray(size: Int): Array<Vertex?> {
            return arrayOfNulls(size)
        }
    }
}