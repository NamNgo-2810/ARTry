package com.namnv.artry.models

import android.os.Parcel
import android.os.Parcelable

class Destination : Parcelable {

    private var lat: Double
    private var lng: Double

    constructor(lat: Double, lng: Double) {
        this.lat = lat
        this.lng = lng
    }

    public fun getLat(): Double {
        return this.lat
    }

    public fun getLng(): Double {
        return this.lng
    }

    constructor(parcel: Parcel) {
        this.lat = parcel.readDouble()
        this.lng = parcel.readDouble()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Destination> {
        override fun createFromParcel(parcel: Parcel): Destination {
            return Destination(parcel)
        }

        override fun newArray(size: Int): Array<Destination?> {
            return arrayOfNulls(size)
        }
    }

}