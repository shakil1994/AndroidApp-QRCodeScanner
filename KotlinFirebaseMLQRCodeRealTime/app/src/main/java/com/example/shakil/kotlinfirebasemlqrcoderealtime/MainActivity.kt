package com.example.shakil.kotlinfirebasemlqrcoderealtime

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.frame.Frame
import com.otaliastudios.cameraview.frame.FrameProcessor
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    internal var isDetected = false

    lateinit var options: FirebaseVisionBarcodeDetectorOptions
    lateinit var detector: FirebaseVisionBarcodeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Dexter.withActivity(this)
            .withPermissions(*arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    setupCamera()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {

                }
            }).check()
    }

    private fun setupCamera() {
        btn_again.setEnabled(isDetected)
        btn_again.setOnClickListener(View.OnClickListener {
            isDetected = !isDetected
            btn_again.setEnabled(isDetected)
        })

        cameraView.setLifecycleOwner(this)
        cameraView.addFrameProcessor(FrameProcessor { frame -> processImage(getVisionImageFromFrame(frame)) })

        options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
            .build()

        detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)
    }

    private fun processImage(image: FirebaseVisionImage) {
        if (!isDetected) {
            detector.detectInImage(image)
                .addOnSuccessListener(OnSuccessListener<List<FirebaseVisionBarcode>> { firebaseVisionBarcodes ->
                    processResult(
                        firebaseVisionBarcodes
                    )
                }).addOnFailureListener(
                    OnFailureListener { e ->
                        Toast.makeText(this@MainActivity, "" + e.message, Toast.LENGTH_SHORT).show()
                    })
        }
    }

    private fun processResult(firebaseVisionBarcodes: List<FirebaseVisionBarcode>) {
        if (firebaseVisionBarcodes.size > 0) {
            isDetected = true

            btn_again.setEnabled(isDetected)
            for (item in firebaseVisionBarcodes) {
                val value_type = item.valueType
                when (value_type) {
                    FirebaseVisionBarcode.TYPE_TEXT -> {
                        createDialog(item.rawValue)
                    }

                    FirebaseVisionBarcode.TYPE_URL -> {
                        //Start Browser Intent
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.rawValue))
                        startActivity(intent)
                    }

                    FirebaseVisionBarcode.TYPE_CONTACT_INFO -> {
                        val info = StringBuilder("Name: ")
                            .append(item.contactInfo!!.name!!.formattedName)
                            .append("\n")
                            .append("Address: ")
                            .append(item.contactInfo!!.addresses[0].addressLines[0])
                            .append("\n")
                            .append("Email: ")
                            .append(item.contactInfo!!.emails[0].address)
                            .toString()
                        createDialog(info)
                    }

                    else -> {
                    }
                }
            }
        }
    }

    private fun createDialog(text: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(text)
            .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.show()
    }

    private fun getVisionImageFromFrame(frame: Frame): FirebaseVisionImage {
        val data = frame.data
        val metadata = FirebaseVisionImageMetadata.Builder()
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setHeight(frame.size.height)
            .setWidth(frame.size.width)
            //.setRotation(frame.getRotation()) // Only use it if you want to work on land scape mode - for portrait don't use it
            .build()
        return FirebaseVisionImage.fromByteArray(data, metadata)
    }
}
