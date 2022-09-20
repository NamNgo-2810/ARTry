package com.namnv.artry.ar

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.namnv.artry.models.Poster
import com.namnv.artry.models.Vertex
import com.namnv.artry.utils.SearchHelper
import java.lang.Math.pow
import java.lang.Math.toRadians
import kotlin.math.*
import kotlin.math.cos as cos1


public class ArImageAndNavigation: AppCompatActivity(), Scene.OnUpdateListener {
    private val TAG = ArImageAndNavigation::class.java.simpleName
    private val MIN_OPENGL_VERSION = 3.0
    private lateinit var arFragment: ArFragment
    private lateinit var arrowRenderable: ModelRenderable
    private lateinit var fitToScanView: ImageView

    private var vertices: ArrayList<Vertex> = ArrayList()
    private lateinit var poster: Poster

    private val dist: ArrayList<Float> = ArrayList()
    private val direction: ArrayList<Pair<Int, Int>> = ArrayList()

    private lateinit var poseList: ArrayList<Pose>
    private val augmentedImageMap: Map<AugmentedImage, AugmentedImageNode> = HashMap()
    private lateinit var startPosition: Vertex
    private lateinit var targetPosition: Vertex
    private lateinit var startingImage: AugmentedImage

    private var pathFound = false
    private var planeFound = false
    private val destFound = false

    private var countLeft = 0
    private var countRight = 0

    override fun onUpdate(frameTime: FrameTime) {
        val frame: Frame = arFragment.arSceneView.arFrame as Frame
        assert(frame != null)

        if (!pathFound) {
            doSearch()
            pathFound = true
        }
        else if (!planeFound) {
            arFragment.arSceneView.planeRenderer.isEnabled = true
            arFragment.planeDiscoveryController.show()

            for (plane in frame.getUpdatedTrackables(
                Plane::class.java
            )) {
                if (plane.trackingState == TrackingState.TRACKING) {
                    arFragment.planeDiscoveryController.hide()
                    arFragment.arSceneView.planeRenderer.isEnabled = false

                    planeFound = true

                    poseList = createPose()

                    val anchorNodeList: ArrayList<AnchorNode> = ArrayList()
                    val nodeList: ArrayList<Node> = ArrayList()

                    for (i in 0..poseList.size) {
                        val modelRenderable: ModelRenderable = arrowRenderable
                        anchorNodeList.add(i, AnchorNode(arFragment.arSceneView.session?.createAnchor(poseList[i])))
                        anchorNodeList[i].setParent(arFragment.arSceneView.scene)

                        nodeList.add(i, Node())
                        nodeList[i].setParent(anchorNodeList[i])
                        nodeList[i].renderable = modelRenderable
                    }
                }
            }
        }
        else if (!destFound) {

        }
        else return

    }

    private fun doSearch() {
        vertices = SearchHelper.getCoordinateAndRotation(startPosition, targetPosition)

    }

    private fun createPose(): ArrayList<Pose> {
        val postList: ArrayList<Pose> = ArrayList()

        if (dist.size == direction.size) {
            var v0 = startingImage.centerPose.tx()
            val v1 = startingImage.centerPose.ty() - 1.5f
            var v2 = startingImage.centerPose.tz() + dist[0]

            val calCoord = calibration(v0, v2)
            v0 = calCoord.first
            v2 = calCoord.second

            val initDirec: Pair<String, String> = Pair("v2", "+")
            postList.add(0, Pose.makeTranslation(v0, v1, v2.toFloat()).compose(Pose.makeRotation(
                cos1(0F/2), 0F, sin(0F / 2), 0F)))

            var previousVector = initDirec.first
            var previousMove = initDirec.second

            var currDir: Pair<Int, Int> = direction[0]
            var nextDir: Pair<Int, Int>

            for (i in 1..dist.size) {
                nextDir = direction[i]
                if (i == 1) {
                    val secondDirect = setDirection(poster, vertices[0], vertices[1])
                    if (secondDirect != null) {
                        previousVector = secondDirect.first
                        previousMove = secondDirect.second
                    }
                }

                when (check(currDir, nextDir)) {
                    "S" -> if (previousVector == "v2") {
                        if (previousMove == "-") {
                            v2 -= dist[i] // rotate about y axis for 180 degree
                            postList.add(
                                i,
                                Pose.makeTranslation(v0, v1, v2).compose(
                                    Pose.makeRotation(
                                        cos1(PI / 2).toFloat(), 0f, sin(PI / 2).toFloat(), 0f
                                    )
                                )
                            )
                        } else {
                            v2 += dist[i] // rotate about y axis for 0 degree
                            postList.add(
                                i,
                                Pose.makeTranslation(v0, v1, v2).compose(
                                    Pose.makeRotation(
                                        cos1(0F / 2), 0f, sin(0F / 2), 0f
                                    )
                                )
                            )
                        }
                    } else {
                        if (previousMove == "-") {
                            v0 -= dist[i] // rotate about y axis for 90 degree
                            postList.add(
                                i,
                                Pose.makeTranslation(v0, v1, v2).compose(
                                    Pose.makeRotation(
                                        cos1(PI / 2 / 2).toFloat(), 0f, sin(PI / 2 / 2).toFloat(), 0f
                                    )
                                )
                            )
                        } else {
                            v0 += dist[i] // rotate about y axis for 270 degree
                            postList.add(
                                i,
                                Pose.makeTranslation(v0, v1, v2).compose(
                                    Pose.makeRotation(
                                        cos1(PI * 3 / 2 / 2).toFloat(),
                                        0f,
                                        sin(PI * 3 / 2 / 2).toFloat(),
                                        0f
                                    )
                                )
                            )
                        }
                    }
                    "L" -> {
                        countLeft++
                        if (previousVector == "v2") {
                            if (previousMove == "-") {
                                v0 -= dist[i] // rotate about y axis for 90 degree
                                postList.add(
                                    i,
                                    Pose.makeTranslation(v0, v1, v2).compose(
                                        Pose.makeRotation(
                                            cos1(PI / 2 / 2).toFloat(),
                                            0f,
                                            sin(PI / 2 / 2).toFloat(),
                                            0f
                                        )
                                    )
                                )
                                previousVector = "v0"
                                previousMove = "-"
                            } else {
                                v0 += dist[i] // rotate about y axis for 270 degree
                                postList.add(
                                    i,
                                    Pose.makeTranslation(v0, v1, v2).compose(
                                        Pose.makeRotation(
                                            cos1(PI * 3 / 2 / 2).toFloat(),
                                            0f,
                                            sin(PI * 3 / 2 / 2).toFloat(),
                                            0f
                                        )
                                    )
                                )
                                previousVector = "v0"
                                previousMove = "+"
                            }
                        } else {
                            if (previousMove == "-") {
                                v2 += dist[i] // rotate about y axis for 0 degree
                                postList.add(
                                    i,
                                    Pose.makeTranslation(v0, v1, v2).compose(
                                        Pose.makeRotation(
                                            cos1(0F / 2), 0f, sin(0F / 2), 0f
                                        )
                                    )
                                )
                                previousVector = "v2"
                                previousMove = "+"
                            } else {
                                v2 -= dist[i] // rotate about y axis for 180 degree
                                postList.add(
                                    i,
                                    Pose.makeTranslation(v0, v1, v2).compose(
                                        Pose.makeRotation(
                                            cos1(PI / 2).toFloat(), 0f, sin(PI / 2).toFloat(), 0f
                                        )
                                    )
                                )
                                previousVector = "v2"
                                previousMove = "-"
                            }
                        }
                    }
                    "R" -> {
                        countRight++
                        if (previousVector == "v2") {
                            if (previousMove == "-") {
                                v0 += dist[i] // rotate about y axis for 270 degree
                                postList.add(
                                    i,
                                    Pose.makeTranslation(v0, v1, v2).compose(
                                        Pose.makeRotation(
                                            cos1(PI * 3 / 2 / 2).toFloat(),
                                            0f,
                                            sin(PI * 3 / 2 / 2).toFloat(),
                                            0f
                                        )
                                    )
                                )
                                previousVector = "v0"
                                previousMove = "+"
                            } else {
                                v0 -= dist[i] // rotate about y axis for 90 degree
                                postList.add(
                                    i,
                                    Pose.makeTranslation(v0, v1, v2).compose(
                                        Pose.makeRotation(
                                            cos1(PI / 2 / 2).toFloat(),
                                            0f,
                                            sin(PI / 2 / 2).toFloat(),
                                            0f
                                        )
                                    )
                                )
                                previousVector = "v0"
                                previousMove = "-"
                            }
                        } else {
                            if (previousMove == "-") {
                                v2 -= dist[i] // rotate about y axis for 180 degree
                                postList.add(
                                    i,
                                    Pose.makeTranslation(v0, v1, v2).compose(
                                        Pose.makeRotation(
                                            cos1(PI / 2).toFloat(), 0f, sin(PI / 2).toFloat(), 0f
                                        )
                                    )
                                )
                                previousVector = "v2"
                                previousMove = "-"
                            } else {
                                v2 += dist[i] // rotate about y axis for 0 degree
                                postList.add(
                                    i,
                                    Pose.makeTranslation(v0, v1, v2).compose(
                                        Pose.makeRotation(
                                            cos1(0F / 2), 0f, sin(0F / 2), 0f
                                        )
                                    )
                                )
                                previousVector = "v2"
                                previousMove = "+"
                            }
                        }
                    }
                }

                currDir = nextDir
            }
        }

        return postList
    }

    private fun calibration(v0: Float, v2: Float): Pair<Float, Float> {
        val d = startingImage.centerPose.tz()
        val dx = startingImage.centerPose.tx()

        val drad = atan(dx/d)

        val x = (cos1(drad) * v0 - sin(drad) * v2)
        val y = (sin(drad) * v0 + cos1(drad) * v2)

        return Pair(x, y)
    }

    private fun setDirection(poster: Poster, vt0: Vertex, vt1: Vertex): Pair<String, String>? {
        val dlat: Double = vt1.getLat() - vt0.getLat()
        val dlng: Double = vt1.getLng() - vt0.getLng()

        when (poster.getDirection()) {
            "N" -> return if (abs(dlat) > abs(dlng)) {
                if (dlat < 0) {
                    Pair("v2", "+")
                } else if (dlng > 0) {
                    Pair("v0", "+")
                } else Pair("v0", "-")
            } else Pair("v0", if (dlng > 0) "+" else "-")

            "S" -> return if (abs(dlat) > abs(dlng)) {
                if (dlat > 0) {
                    Pair("v2", "+")
                } else if (dlng > 0) {
                    Pair("v0", "-")
                } else Pair("v0", "+")
            } else Pair("v0", if (dlng > 0) "-" else "+")

            "E" -> return if (abs(dlng) > abs(dlat)) {
                if (dlng < 0) {
                    Pair("v2", "+")
                } else if (dlat > 0) {
                    Pair("v0", "-")
                } else Pair("v0", "+")
            } else Pair("v0", if (dlat > 0) "-" else "+")

            "W" -> return if (abs(dlng) > abs(dlat)) {
                if (dlng > 0) {
                    Pair("v2", "+")
                } else if (dlat > 0) {
                    Pair("v0", "+")
                } else Pair("v0", "-")
            } else Pair("v0", if (dlat > 0) "+" else "-")
        }

        return null
    }

    private fun check(currDir: Pair<Int, Int>, nextDir: Pair<Int, Int>): String {
        return if (abs(currDir.first) > abs(nextDir.second)) {
            if (currDir.first > 0) {
                if (abs(nextDir.first) > abs(nextDir.second)) {
                    if (nextDir.first > 0) "S"
                    else "T"
                } else {
                    if (nextDir.second > 0) "R"
                    else "L"
                }
            }
            else {
                if (abs(nextDir.first) > abs(nextDir.second)) {
                    if (nextDir.first > 0) "T"
                    else "S"
                } else {
                    if (nextDir.second > 0) "L"
                    else "R"
                }
            }
        }
        else {
            if (abs(nextDir.first) > abs(nextDir.second)) {
                if (nextDir.first > 0) "R"
                else "L"
            } else {
                if(nextDir.second > 0) "T"
                else "S"
            }
        }
    }

    private fun translation() {
        for (i in 1..vertices.size) {
            val lat1: Double = vertices[i - 1].getLat()
            val lng1: Double = vertices[i - 1].getLng()
            val lat2: Double = vertices[i].getLat()
            val lng2: Double = vertices[i].getLng()

            dist.add(greatCircle(lat1, lng1, lat2, lng2) * 0.99990853739f)
        }
    }

    private fun greatCircle(lat1: Double, long1: Double, lat2: Double, long2: Double): Float {
        // Convert the latitudes and longitudes
        // from degree to radians.
        var lat1 = lat1
        var long1 = long1
        var lat2 = lat2
        var long2 = long2
        lat1 = toRadians(lat1)
        long1 = toRadians(long1)
        lat2 = toRadians(lat2)
        long2 = toRadians(long2)

        // Haversine Formula
        val dlong = long2 - long1
        val dlat = lat2 - lat1
        var ans = pow(sin(dlat / 2), 2.0) + cos1(lat1) * cos1(lat2) * pow(sin(dlong / 2), 2.0)
        ans = 2 * asin(sqrt(ans))

        // Radius of Earth in
        // Kilometers, R = 6371
        // Use R = 3956 for miles
        val R = 6371.0

        // Calculate the result
        ans *= R

        // km to m, double to float
        return (ans * 1000).toFloat()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun loadRenderable() {
        ModelRenderable.builder()
            .setSource(this, Uri.parse("model.sfb"))
            .build()
            .thenAcceptAsync { renderable: ModelRenderable ->
                arrowRenderable = renderable
            }
            .exceptionally { throwable: Throwable? ->
                val toast =
                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
                null
            }
    }

    override fun onResume() {
        super.onResume()
        if (augmentedImageMap.isEmpty()) {
            fitToScanView.visibility = View.VISIBLE
        }
    }

//    fun networkChecking() {
//        if (!networkThreadBusy) {
//            networkThreadBusy = true
//            Thread {
//                if (!SearchHelper.checkActiveInternetConnection()) showError("No network present")
//                networkThreadBusy = false
//            }.start()
//        }
//    }

    fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later")
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        val openGlVersionString =
            (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .deviceConfigurationInfo
                .glEsVersion
        if (openGlVersionString.toDouble() < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later")
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        return true
    }

}