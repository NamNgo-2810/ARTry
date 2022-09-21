package com.namnv.artry.ar

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import java.io.IOException


class AugmentedImageFragment: ArFragment() {
    companion object {
        private val TAG = "AugmentedImageFragment"
        private val USE_SINGLE_IMAGE = false
        private val DEFAULT_IMAGE_NAME = "default.jpg"
        private val SAMPLE_IMAGE_DATABASE = "arImgDB/myimages123.imgdb"
        private val MIN_OPENGL_VERSION = 3.0
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = true

        return view
    }

    override fun getSessionConfiguration(session: Session?): Config {
        val config = super.getSessionConfiguration(session)
        if (!setupAugmentedImageDatabase(config, session)) {

        }
        return config
    }

    private fun setupAugmentedImageDatabase(config: Config, session: Session?): Boolean {
        var augmentedImageDatabase: AugmentedImageDatabase
        val assetManager = if (context != null) requireContext().assets else null
        if (assetManager == null) {
            return false
        }

        if (USE_SINGLE_IMAGE) {
            val augmentedImageBitmap: Bitmap? = loadAugmentedImageBitmap(assetManager)
            if (augmentedImageBitmap == null) {
                return false
            }

            augmentedImageDatabase = AugmentedImageDatabase(session)
            augmentedImageDatabase.addImage(DEFAULT_IMAGE_NAME, augmentedImageBitmap)
        }

        else {
            try {
                requireContext().assets.open(SAMPLE_IMAGE_DATABASE).use { `is` ->
                    augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, `is`)
                }
            } catch (e: IOException) {
                Log.e(TAG, "IO exception loading augmented image database.", e)
                return false
            }
        }

        config.augmentedImageDatabase = augmentedImageDatabase
        return true
    }

    private fun loadAugmentedImageBitmap(assetManager: AssetManager): Bitmap? {
        try {
            assetManager.open(DEFAULT_IMAGE_NAME)
                .use { `is` -> return BitmapFactory.decodeStream(`is`) }
        } catch (e: IOException) {
            Log.e(TAG, "IO exception loading augmented image bitmap.", e)
        }
        return null
    }
}