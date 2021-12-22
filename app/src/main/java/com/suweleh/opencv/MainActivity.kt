package com.suweleh.opencv

import android.R.attr
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import androidx.core.app.ActivityCompat.startActivityForResult
import kotlin.random.Random
import org.opencv.core.Mat

import android.R.attr.y

import android.R.attr.x





class MainActivity : AppCompatActivity() {
    // Define the pic id
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    handleCameraImage(result.data)
                }
            }
        val pickImagesLauncher = registerForActivityResult(ActivityResultContracts.GetContent()){ uri: Uri? ->
            uri?.let { it ->
                // The image was saved into the given Uri -> do something with it
                    MediaStore.Images.Media.getBitmap(this.contentResolver, it)?.let {
                        detectCircle(it)
                    }

            }
        }
        btnTakePicture.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            resultLauncher.launch(cameraIntent)
        }
        btnBrowsePicture.setOnClickListener {
            pickImagesLauncher.launch("image/*")
        }
    }

    private fun handleCameraImage(intent: Intent?) {
        val bitmap = intent?.extras?.get("data") as Bitmap
        detectCircle(bitmap)
    }

    private fun detectCircle(bitmap: Bitmap) {
        val src = Mat(
            bitmap.width, bitmap.height,
            CvType.CV_8UC1
        )
        val gray = Mat(
            bitmap.width, bitmap.height,
            CvType.CV_8UC1
        )

        Utils.bitmapToMat(bitmap, src)
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)
        addToLayout(gray)

        val blur = Mat(src.rows(), src.cols(), src.type())
        Imgproc.GaussianBlur(gray, blur, Size(15.0, 15.0), 0.0)
        addToLayout(blur)

        val binary = Mat(src.rows(), src.cols(), src.type(), Scalar(0.0))
        Imgproc.threshold(gray, binary, 90.0, 255.0, Imgproc.THRESH_BINARY_INV)

        addToLayout(binary)

        val contours = arrayListOf<MatOfPoint>()
        val mat = Mat(
            bitmap.width, bitmap.height,
            CvType.CV_8UC1
        )
        Imgproc.findContours(
            binary,
            contours,
            mat,
            Imgproc.RETR_TREE,
            Imgproc.CHAIN_APPROX_SIMPLE
        )

        var count = 0
        contours.map {
            if (it.rows() in 100 until 400) {
                Imgproc.fitEllipse(MatOfPoint2f(*it.toArray()))
            } else {
                RotatedRect()
            }
        }.forEachIndexed { index, contour ->
            if (contour.size.width > 0 && contour.size.height > 0) {
                count++
            }
            Imgproc.ellipse(src, contour, Scalar(0.0, 200.0, 0.0), 5)
        }

        tvWoodSum.text = "Total wood log ${count}"

        addToLayout(src)

        addToLayout(cropImage(src, 50, 50, src.cols(), src.rows()))
    }

    private fun cropImage(unCropped: Mat, x: Int, y: Int, width: Int, height: Int ): Mat {
        val roi = Rect(x, y, width-x, height-y)
        return Mat(unCropped, roi)
    }

    private fun addToLayout(mat: Mat) {
        val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.RGB_565)
        Utils.matToBitmap(mat, bitmap)
        val ivImageSource = ImageView(this)
        ivImageSource.setImageBitmap(bitmap)
        contentLayout.addView(ivImageSource)
    }
}