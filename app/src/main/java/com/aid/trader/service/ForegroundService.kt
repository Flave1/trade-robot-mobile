package com.aid.trader.service

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.AudioManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.aid.trader.R
import com.aid.trader.data.payload.LLMForexChartResponse
import com.aid.trader.helper.Uten
import com.aid.trader.presentation.activity.overlay.FullScreenNotificationView
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ForegroundService : Service() {

    private var fullScreenNotificationView: FullScreenNotificationView? = null
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private lateinit var sharedPreferences: SharedPreferences
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var grab: Int = 0
    private var previousVolume = -1
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var imageReaderHandler: Handler? = null
    private var imageReaderHandlerThread: HandlerThread? = null
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object: Runnable {
        override fun run() {
            handler.postDelayed(this, 2000)
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        registerVolumeReceiver()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyForeground()
        } else {
            startForeground(1, Notification())
        }
//        startHandler()
        fullScreenNotificationView = FullScreenNotificationView(applicationContext)
        fullScreenNotificationView?.open()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra("resultCode", Activity.RESULT_CANCELED)
        val data = intent?.getParcelableExtra<Intent>("data")
        if (resultCode == Activity.RESULT_OK && data != null) {
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
            startMediaProjection()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startMediaProjection() {
        try {
            mediaProjection?.let { it ->
                val displayMetrics = resources.displayMetrics
                val density = displayMetrics.densityDpi
                val width = displayMetrics.widthPixels
                val height = displayMetrics.heightPixels

                imageReaderHandlerThread = HandlerThread("ImageReader")
                imageReaderHandlerThread?.start()
                imageReaderHandler = Handler(imageReaderHandlerThread?.looper!!)
                imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
                virtualDisplay = it.createVirtualDisplay(
                    "ScreenCapture",
                    width, height, density,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader?.surface, null, null
                )

                imageReader?.setOnImageAvailableListener({ reader ->
                    val image = reader.acquireLatestImage()
                    image?.let {
                        val planes = it.planes
                        val buffer = planes[0].buffer
                        val pixelStride = planes[0].pixelStride
                        val rowStride = planes[0].rowStride
                        val rowPadding = rowStride - pixelStride * width

                        val bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
                        bitmap.copyPixelsFromBuffer(buffer)

                        saveBitmap(bitmap)
                        it.close()
                    }
                }, imageReaderHandler)
            }
        } catch (ex: Exception) {
            Log.e("ForegroundService", "startMediaProjection: ${ex.message}", ex)
        }
    }

    private fun saveBitmap(bitmap: Bitmap): Bitmap {
        if (grab != 0) {
            val path = getExternalFilesDir(null)?.absolutePath + "/chart.png"
            var file = File(path)
            var fileOutputStream: FileOutputStream? = null
            try {
                fileOutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                submitChart(file, "GBPUSD", "ANY", "ANY")
                Log.d("Screenshot", "Screenshot saved: $path")
            } catch (e: IOException) {
                e.printStackTrace()
            }
            grab = 0
            return bitmap
        } else {
            return bitmap
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fullScreenNotificationView?.removeOverLay()
        stopHandler()
        coroutineScope.cancel()
        mediaProjection?.stop()
        imageReader?.close()
        imageReaderHandlerThread?.quitSafely()
        virtualDisplay?.release()
        unregisterReceiver(volumeReceiver)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyForeground() {
        val channel = NotificationChannel(
            CHANNEL_NAME,
            "Full Screen",
            NotificationManager.IMPORTANCE_MIN
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_NAME)
            .setContentTitle("AID TRADER")
            .setSmallIcon(R.drawable.logo)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        startForeground(2, notification)
    }

    private fun startHandler() {
        handler.post(runnable)
    }

    private fun stopHandler() {
        handler.removeCallbacks(runnable)
    }

    private val volumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (previousVolume != -1) {
                if (currentVolume > previousVolume) {
                    grab = 1
                    Log.d("ForegroundService", "Volume increased to $currentVolume")
                } else if (currentVolume < previousVolume) {
                    grab = 0
                    Log.d("ForegroundService", "Volume decreased to $currentVolume")
                    fullScreenNotificationView?.clearText()
                } else {
                    grab = 0
                }
            }
            previousVolume = currentVolume
        }
    }

    private fun registerVolumeReceiver() {
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        registerReceiver(volumeReceiver, filter)
    }

    private fun sendChartAnalysisRequest(path: String) {
        val intent = Intent("com.aid.trader.CUSTOM_BROADCAST")
        intent.putExtra("chart_image", path)
        sendBroadcast(intent)
    }

    private fun submitChart(
        imageFile: File,
        currencyPairs: String,
        timeFrame: String,
        tradingStrategy: String) {
        val MEDIA_TYPE_PNG = "image/jpeg".toMediaTypeOrNull()
        val imgBody = MultipartBody.Part.createFormData("chart_image", imageFile.name, RequestBody.create(MEDIA_TYPE_PNG, imageFile))
        val currencyPairBody = currencyPairs.toRequestBody("text/plain".toMediaTypeOrNull())
        val tradingStrategyBody = tradingStrategy.toRequestBody("text/plain".toMediaTypeOrNull())
        val timeFrameBody = timeFrame.toRequestBody("text/plain".toMediaTypeOrNull())

        val call: Call<LLMForexChartResponse> = Uten.FetchServerData().submitChart(currencyPairBody, tradingStrategyBody, timeFrameBody, imgBody)
        call.enqueue(object : Callback<LLMForexChartResponse> {

            override fun onResponse(call: Call<LLMForexChartResponse>, response: Response<LLMForexChartResponse>) {
                var data = response.body()
                if (data != null) {
                    fullScreenNotificationView?.setOverlayText(data.pointers)
                }
            }

            override fun onFailure(call: Call<LLMForexChartResponse>, t: Throwable) {
                Log.d("onFailure", t.toString())
            }
        })
    }

    companion object {
        private const val CHANNEL_NAME = "Full_Screen_Demo_channel"
    }
}
