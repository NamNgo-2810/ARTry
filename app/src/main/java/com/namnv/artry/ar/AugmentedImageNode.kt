package com.namnv.artry.ar

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import java.util.concurrent.CompletableFuture

class AugmentedImageNode @RequiresApi(Build.VERSION_CODES.N) constructor(context: Context) :
    AnchorNode() {
    companion object {
        private val TAG: String = "AugmentedImageNode"
        private var ulCorner: CompletableFuture<ModelRenderable>? = null
        private var urCorner: CompletableFuture<ModelRenderable>? = null
        private var lrCorner: CompletableFuture<ModelRenderable>? = null
        private var llCorner: CompletableFuture<ModelRenderable>? = null
    }

    private lateinit var image: AugmentedImage

    init {
        if (ulCorner == null) {
            ulCorner = ModelRenderable.builder().setSource(context, Uri.parse("/")).build()
            urCorner = ModelRenderable.builder().setSource(context, Uri.parse("/")).build()
            lrCorner = ModelRenderable.builder().setSource(context, Uri.parse("/")).build()
            llCorner = ModelRenderable.builder().setSource(context, Uri.parse("/")).build()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public fun setImage(image: AugmentedImage) {
        this.image = image

        if (!ulCorner!!.isDone || !urCorner!!.isDone || !llCorner!!.isDone || !lrCorner!!.isDone) {
            CompletableFuture.allOf(ulCorner, urCorner, llCorner, lrCorner)
                .thenAccept {
                    setImage(
                        image
                    )
                }
                .exceptionally { throwable: Throwable? ->
                    Log.e(TAG, "Exception loading", throwable)
                    null
                }
        }

        anchor = image.createAnchor(image.centerPose)

        val localPosition: Vector3 = Vector3()

        localPosition.set(-0.5f * image.extentX, 0.0f, -0.5f * image.extentZ)
        var cornerNode: Node = Node()
        cornerNode.setParent(this)
        cornerNode.localPosition = localPosition
        cornerNode.renderable = ulCorner!!.getNow(null)

        localPosition[0.5f * image.extentX, 0.0f] = -0.5f * image.extentZ
        cornerNode = Node()
        cornerNode.setParent(this)
        cornerNode.localPosition = localPosition
        cornerNode.renderable = urCorner!!.getNow(null)

        // Lower right corner.

        // Lower right corner.
        localPosition[0.5f * image.extentX, 0.0f] = 0.5f * image.extentZ
        cornerNode = Node()
        cornerNode.setParent(this)
        cornerNode.localPosition = localPosition
        cornerNode.renderable = lrCorner!!.getNow(null)

        // Lower left corner.

        // Lower left corner.
        localPosition[-0.5f * image.extentX, 0.0f] = 0.5f * image.extentZ
        cornerNode = Node()
        cornerNode.setParent(this)
        cornerNode.localPosition = localPosition
        cornerNode.renderable = llCorner!!.getNow(null)

    }

    public fun getImage(): AugmentedImage {
        return this.image
    }
}