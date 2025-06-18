package com.example.project

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var textureView: TextureView
    private lateinit var btnRecord: Button
    private lateinit var mediaRecorder: MediaRecorder
    private var cameraDevice: CameraDevice? = null
    private var cameraSession: CameraCaptureSession? = null
    private var recording = false
    private lateinit var videoFile: File

    companion object {
        private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.INTERNET
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
            )
        }

        private const val REQUEST_CODE_PERMISSIONS = 1001
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        textureView = findViewById(R.id.textureView)
        btnRecord = findViewById(R.id.btnRecord)

        btnRecord.setOnClickListener {
            if (recording) stopRecording() else startRecording()
        }

        if (hasPermissions()) {
            textureView.surfaceTextureListener = surfaceTextureListener
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
        val btnViewFrames = findViewById<Button>(R.id.btnViewFrames)
        btnViewFrames.setOnClickListener {
            val intent = Intent(this, FrameListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun hasPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                textureView.surfaceTextureListener = surfaceTextureListener
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            facing == CameraCharacteristics.LENS_FACING_BACK
        } ?: cameraManager.cameraIdList[0]

        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                startPreview()
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
                cameraDevice = null
            }

            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
                cameraDevice = null
                Toast.makeText(this@MainActivity, "Record session failed", Toast.LENGTH_SHORT).show()
            }
        }, null)
    }

    private fun startPreview() {
        val texture = textureView.surfaceTexture ?: return
        texture.setDefaultBufferSize(1920, 1080)
        val previewSurface = Surface(texture)

        val captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW) ?: return
        captureRequestBuilder.addTarget(previewSurface)

        cameraDevice?.createCaptureSession(listOf(previewSurface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                cameraSession = session
                session.setRepeatingRequest(captureRequestBuilder.build(), null, null)
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                Toast.makeText(this@MainActivity, "Preview failed", Toast.LENGTH_SHORT).show()
            }
        }, null)
    }

    private fun startRecording() {
        if (cameraDevice == null || !textureView.isAvailable) return

        mediaRecorder = MediaRecorder()
        videoFile = createVideoFile()

        mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(videoFile.absolutePath)
            setVideoEncodingBitRate(10_000_000)
            setVideoFrameRate(30)
            setVideoSize(1920, 1080)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            prepare()
        }

        val texture = textureView.surfaceTexture ?: return
        texture.setDefaultBufferSize(1920, 1080)
        val previewSurface = Surface(texture)
        val recordSurface = mediaRecorder.surface

        val captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_RECORD) ?: return
        captureRequestBuilder.addTarget(previewSurface)
        captureRequestBuilder.addTarget(recordSurface)

        cameraDevice?.createCaptureSession(listOf(previewSurface, recordSurface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    cameraSession = session
                    session.setRepeatingRequest(captureRequestBuilder.build(), null, null)
                    mediaRecorder.start()
                    recording = true
                    btnRecord.text = "Stop Recording"
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Toast.makeText(this@MainActivity, "Record session failed", Toast.LENGTH_SHORT).show()
                }
            }, null)
    }

    private fun stopRecording() {
        if (!recording) return

        try {
            mediaRecorder.stop()
            mediaRecorder.reset()
            Toast.makeText(this, "Record complete: ${videoFile.absolutePath}", Toast.LENGTH_LONG).show()

            uploadVideo(videoFile)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Record session failed: ${e.message}", Toast.LENGTH_LONG).show()
        }

        recording = false
        btnRecord.text = "Start recording"
        startPreview()
    }

    private fun uploadVideo(videoFile: File) {
        Thread {
            try {
                val client = OkHttpClient()

                val mediaType = "video/mp4".toMediaTypeOrNull()
                val fileBody = videoFile.asRequestBody(mediaType)

                val multipartBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", videoFile.name, fileBody)
                    .build()

                val request = Request.Builder()
                    // Todo: Change to the current IPv4 address
                    .url("http://172.31.52.183:8123/upload/")
                    .post(multipartBody)
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()

                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this, "Upload success", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Upload failed: $body", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Upload error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun createVideoFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        return File(storageDir, "VID_$timeStamp.mp4")
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraDevice?.close()
        cameraDevice = null
        if (::mediaRecorder.isInitialized) {
            mediaRecorder.release()
        }
    }
}