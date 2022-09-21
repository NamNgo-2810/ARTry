package com.namnv.artry.activities

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.namnv.artry.R
import com.namnv.artry.ar.ArImageAndNavigation
import com.namnv.artry.models.Vertex
import com.namnv.artry.utils.NavigationService

class Launcher : AppCompatActivity() {

    val locationMap = HashMap<Int, IntArray>()
    private var locations = ArrayList<String>()
    private var destinations = ArrayList<String>()
    var current: IntArray? = intArrayOf(0, 0)
    lateinit var map: ImageView
    lateinit var button: Button
    var mService: NavigationService? = null
    var isBound = false

    var vertices: ArrayList<Vertex> = ArrayList()

    private val connection: ServiceConnection = object : ServiceConnection {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            Log.d("Test", "Connected")
            val mBinder = p1 as NavigationService.PathFindBinder
            mService = mBinder.getService()
            mService!!.imageToArray(map)
            isBound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBound = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        initLocation()
        val intent = Intent(this, NavigationService::class.java)

        map = findViewById(R.id.mapTop)
        button = findViewById(R.id.button)
        bindService(intent, connection, BIND_AUTO_CREATE)
        startService(intent)
        initSpinner()

        button.setOnClickListener {
            val intent = Intent(this, ArImageAndNavigation::class.java).apply {
                putExtra("vertices", vertices)
            }
            startActivity(intent)
        }
    }

    private fun initSpinner() {
        val spinner: Spinner = findViewById(R.id.currentLocationSpinner)
        val spinnerDes: Spinner = findViewById(R.id.destinationSpinner)
        val adapter: ArrayAdapter<String> = ArrayAdapter(this, R.layout.spinner_item, locations)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter

        val adapterDes: ArrayAdapter<String> = ArrayAdapter(this,
            R.layout.spinner_item, destinations)
        adapterDes.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerDes.adapter = adapterDes

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                current = locationMap[p2 - 1]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        spinnerDes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                map.setImageBitmap(null)
                val destination: IntArray? = locationMap[p2 - 1]
                if (destination != null) {
                    current?.let {
                        vertices = mService?.findPath(map, it, destination)!!
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }
    }

    private fun initLocation() {
        val set: LinkedHashSet<String> = LinkedHashSet()
        set.add("Nam Ngo")
        set.add("Hung")

        locations.add("What is your current location?")
        locations.addAll(set)

        destinations.add("What is your destination?")
        destinations.addAll(set)

        locationMap[0] = intArrayOf(393, 492)
        locationMap[1] = intArrayOf(350, 470)

    }

    override fun onDestroy() {
        unbindService(connection)
        stopService(intent)
        super.onDestroy()
    }
}