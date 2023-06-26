package com.example.wearablerobotics

import android.Manifest
import android.annotation.SuppressLint
import android.content.AsyncQueryHandler
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback

class MainActivity : AppCompatActivity() {

    lateinit var capReq: CaptureRequest.Builder
    lateinit var handler: Handler
    lateinit var handlerThread: HandlerThread
    lateinit var cameraManager:CameraManager
    lateinit var textureView: TextureView
    lateinit var cameraCaptureSession: CameraCaptureSession
    lateinit var cameraDevice: CameraDevice
    lateinit var captureRequest: CaptureRequest



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        get_permissions()

        textureView=findViewById(R.id.textureView)
        cameraManager=getSystemService(Context.CAMERA_SERVICE) as CameraManager

        handlerThread= HandlerThread("videoThread")
        handlerThread.start()
        handler= Handler((handlerThread).looper)

        textureView.surfaceTextureListener= object : TextureView.SurfaceTextureListener{

            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                open_camera()
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {

            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {

            }
        }




    }
    @SuppressLint("MissingPermission")
    fun open_camera(){
        cameraManager.openCamera(cameraManager.cameraIdList[0],

            object:CameraDevice.StateCallback(){
                override fun onOpened(p0: CameraDevice) {
                    cameraDevice=p0
                    capReq=cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    var surface= Surface(textureView.surfaceTexture)
                    capReq.addTarget(surface)

                    cameraDevice.createCaptureSession(listOf(surface),object : CameraCaptureSession.StateCallback(){
                        override fun onConfigured(p0: CameraCaptureSession) {
                            cameraCaptureSession=p0
                            cameraCaptureSession.setRepeatingRequest(capReq.build(),null,null)
                        }

                        override fun onConfigureFailed(p0: CameraCaptureSession) {

                        }
                    },handler)


                }

                override fun onDisconnected(p0: CameraDevice) {

                }

                override fun onError(p0: CameraDevice, p1: Int) {

                }
            },handler)
    }

    fun get_permissions(){
        var permissionsList= mutableListOf<String>()

        if(checkSelfPermission(android.Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)
            permissionsList.add(android.Manifest.permission.CAMERA)

        if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
            permissionsList.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)

        if(checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
            permissionsList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)


        if(permissionsList.size>0){
            requestPermissions(permissionsList.toTypedArray(),101)
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        grantResults.forEach {

            if(it != PackageManager.PERMISSION_GRANTED) {
                get_permissions()
            }
        }
    }

    fun FunctionClicked(view: View) {

        var diesplayText:TextView= findViewById(R.id.text)
        diesplayText.setText("Bluetooth sends")
    }


}