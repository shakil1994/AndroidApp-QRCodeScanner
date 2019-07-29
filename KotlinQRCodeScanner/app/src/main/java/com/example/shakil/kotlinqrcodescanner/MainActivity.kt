package com.example.shakil.kotlinqrcodescanner

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import github.nisrulz.qreader.QRDataListener
import github.nisrulz.qreader.QREader

class MainActivity : AppCompatActivity() {

    private var txt_result: TextView? = null
    private var surfaceView: SurfaceView? = null
    private var qrEader: QREader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Request Permission
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    setupCamera()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    Toast.makeText(this@MainActivity, "You must enable this permission", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {

                }
            }).check()
    }

    private fun setupCamera() {
        txt_result = findViewById(R.id.code_info)
        val btn_on_off = findViewById<ToggleButton>(R.id.btn_enable_disable)
        btn_on_off.setOnClickListener {
            if (qrEader!!.isCameraRunning()) {
                btn_on_off.isChecked = false
                qrEader!!.stop()
            } else {
                btn_on_off.isChecked = true
                qrEader!!.start()
            }
        }

        surfaceView = findViewById(R.id.camera_view)
        setupQREader()
    }

    private fun setupQREader() {
        qrEader = QREader.Builder(this, surfaceView,
            QRDataListener { data -> txt_result!!.post(Runnable { txt_result!!.setText(data) }) }).facing(QREader.BACK_CAM)
            .enableAutofocus(true)
            .height(surfaceView!!.getHeight())
            .width(surfaceView!!.getWidth())
            .build()
    }

    override fun onResume() {
        super.onResume()
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    if (qrEader != null) {
                        qrEader!!.initAndStart(surfaceView)
                    }
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    Toast.makeText(this@MainActivity, "You must enable this permission", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {

                }
            }).check()
    }

    override fun onPause() {
        super.onPause()
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    if (qrEader != null) {
                        qrEader!!.releaseAndCleanup()
                    }
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    Toast.makeText(this@MainActivity, "You must enable this permission", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {

                }
            }).check()
    }
}
