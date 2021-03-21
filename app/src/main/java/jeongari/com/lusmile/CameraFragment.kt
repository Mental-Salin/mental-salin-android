package jeongari.com.lusmile

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.*
import jeongari.com.camera.Camera2BasicFragment


class CameraFragment : Camera2BasicFragment() {

    private var byteArray: ByteArray? = null

    private var ltViewHappy: LottieAnimationView? = null

    private val metadata: FirebaseVisionImageMetadata by lazy {
        FirebaseVisionImageMetadata.Builder()
            .setWidth(textureView!!.width) // 480x360 is typically sufficient for
            .setHeight(textureView!!.height) // image recognition
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12)
            .setRotation(0)
            .build()

    }
    private val realTimeOpts: FirebaseVisionFaceDetectorOptions by lazy {
        FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
            .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .build()
    }
    private val detector: FirebaseVisionFaceDetector by lazy {
        FirebaseVision.getInstance()
            .getVisionFaceDetector(realTimeOpts)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View? {
        val view = inflateFragment(R.id.layoutFrame, inflater, container)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ltViewHappy = view.findViewById(R.id.ltViewHappy)
        ltViewHappy?.apply {
            this.visibility = View.INVISIBLE
            this.speed = 5.0f
        }
    }

    override fun detectFace() {
        val bitmap = textureView?.getBitmap(textureView!!.width, textureView!!.height)
        if (bitmap != null) {
            byteArray = getYV12ByteArray(textureView!!.width, textureView!!.height, bitmap)
            bitmap.recycle()

            val image = FirebaseVisionImage.fromByteArray(byteArray!!, metadata)

            detector.detectInImage(image)
                .addOnCompleteListener {
                }
                .addOnSuccessListener { faces ->
                    if (faces.isEmpty()){
                        showTextview("No Face deteced")
                    }
                    else{
                        showPoints(faces)
                        showLottieAnimation(faces)
                    }
                }
                .addOnCanceledListener {
                    showTextview("Task for detecting Face image canceled.")
                }
                .addOnFailureListener(
                    object : OnFailureListener {
                        override fun onFailure(e: Exception) {
                            showTextview("Task for detecting Face image failed.")
                            Log.e(TAG, e.toString())
                        }
                    }
                )

        }
    }
    private fun showLottieAnimation(faces: List<FirebaseVisionFace>) {
        for (face in faces) {
            val bounds = face.boundingBox
            val boundWidth = (bounds.right - bounds.left)
            if (face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                val smileProb = face.smilingProbability
                if (smileProb > 0.7f) {
                    activity?.runOnUiThread {
                        ltViewHappy?.visibility = View.VISIBLE
                        ltViewHappy?.layoutParams?.width = boundWidth
                        ltViewHappy?.layoutParams?.height = boundWidth
                        ltViewHappy?.x = bounds.left.toFloat()
                        ltViewHappy?.y = bounds.top.toFloat() - boundWidth

                        ltViewHappy?.requestLayout()
                    }
                    if (ltViewHappy?.isAnimating != true)
                        ltViewHappy?.playAnimation()
                    showImageview(resources.getDrawable(R.drawable.ic_calm))

                } else {
                    activity?.runOnUiThread {
                        ltViewHappy?.visibility = View.INVISIBLE
                    }
                    if (ltViewHappy!!.isAnimating) {
                        ltViewHappy?.cancelAnimation()
                    }
                    showImageview(resources.getDrawable(R.drawable.ic_sad))
                }
                showTextview("Smiling Probability Estimation : " + (smileProb * 100) + " %")
            }
        }
    }

    private fun showPoints(faces: List<FirebaseVisionFace>) {
        activity?.runOnUiThread {
            drawView?.setImgSize(textureView!!.width, textureView!!.height)
        }
        for (face in faces) {
            val bounds = face.boundingBox

            val rotY = face.headEulerAngleY // Head is rotated to the right rotY degrees
            val rotZ = face.headEulerAngleZ // Head is tilted sideways rotZ degrees

            val all = face.getContour(FirebaseVisionFaceContour.ALL_POINTS).points
            var p_all : ArrayList<PointF> = ArrayList<PointF>()
            for (each in all){
                p_all.add(PointF(each.x, each.y))
            }
//            drawView!!.setDrawPoint(RectF(bounds), p_all!! , 1f)
//            showTextview(bounds.toShortString())

            drawView!!.setDrawPoint(RectF(bounds), p_all , 1f)
            showTextview(bounds.toShortString())
        }
        drawView?.invalidate()
    }

    private fun getYV12ByteArray(inputWidth: Int, inputHeight: Int, bitmap: Bitmap): ByteArray {
        val start_time = System.currentTimeMillis()
        val argb = IntArray(inputWidth * inputHeight)
        bitmap.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight)
        val yuv = ByteArray(inputWidth * inputHeight * 3 / 2)
        encodeYV12(yuv, argb, inputWidth, inputHeight)
        bitmap.recycle()
        val end_time = System.currentTimeMillis()
        Log.d("RGBA to YV12", (end_time - start_time).toString() + " ms")
        return yuv
    }

    companion object {

        val TAG = "CameraFragment"

        fun newInstance(): CameraFragment {
            return CameraFragment()
        }
    }
}
