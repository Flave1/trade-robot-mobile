package com.aid.trader.presentation.activity.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import com.aid.trader.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class FullScreenNotificationView(private val context: Context) : View(context) {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var params: WindowManager.LayoutParams? = null
    private lateinit var overlayTextView: TextView
    private lateinit var deviceView: RelativeLayout
//    private val overlayViewModel: OverlayViewModel by viewModels()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    init {
        showOverlay()
    }

    private fun showOverlay() {
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = layoutInflater.inflate(R.layout.full_screen_notification_layout, null)
        overlayTextView = overlayView?.findViewById(R.id.textView)!!
        deviceView = overlayView?.findViewById(R.id.device_view2)!!

        params?.gravity = Gravity.START or Gravity.TOP
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager?.addView(overlayView, params)
        setOverlayText("")
//        observeDataChange()
//        registerChartRequestReceiver()
    }

    fun open() {
        try {
            if (overlayView?.windowToken == null) {
                windowManager?.addView(overlayView, params)
            }
        } catch (e: Exception) {
            Log.d("window open", e.toString())
        }
    }

    fun clearText() {
        overlayTextView.setBackgroundColor(Color.TRANSPARENT)
        overlayTextView.text = ""
    }

    private fun setText(text: String) {
        overlayTextView.setBackgroundColor(Color.parseColor("#88000000"))
        overlayTextView.text = text
//        coroutineScope.launch {
//            while (isActive) {
//                delay(2000)
//
//                coroutineScope.cancel()
//            }
//        }
    }

    fun setOverlayText(text: String) {
        if(text.isNotEmpty()){
//            animateView()
            setText(text)
        }else{
            clearText()
        }
    }

    private  fun setAidView(){

    }



//    private fun registerChartRequestReceiver() {
//        val filter = IntentFilter("com.aid.trader.CUSTOM_BROADCAST")
//        registerReceiver(chartAnalyserReceiver, filter)
//    }

//    private val chartAnalyserReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            val chartUri = intent?.getStringExtra("chart_image")
//            chartUri?.let { uri ->
//                val chartFile = File(uri)
//                overlayViewModel.submitChart(chartFile, "GBP/USD", "24hrs", "Any Strategy")
//            }
//        }
//    }

//    private fun observeDataChange(){
//        overlayViewModel.response.observe(this, Observer { result ->
//            when (result) {
//                is NetworkCallResult.Success -> {
//                    val response = result.data
//                    Toast.makeText(this, response.pointers, Toast.LENGTH_LONG).show()
//                }
//                is NetworkCallResult.Error -> {
//                    Log.d("NetworkCallResult.Error", result.exception.toString())
//                    Toast.makeText(this, "Error: ${result.exception.message}", Toast.LENGTH_LONG).show()
//                }
//            }
//        })
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        unregisterReceiver(chartAnalyserReceiver)
//    }

//    private fun animateView(){
//        val loadingView = overlayView?.findViewById<View>(R.id.loading)
//        val deviceHeight = deviceView.height
//
//        val animator = ObjectAnimator.ofFloat(loadingView, "translationY", 0f, deviceHeight.toFloat())
//        animator.duration = 2000 // Duration of the animation in milliseconds
//        loadingView?.visibility = VISIBLE
//        animator.start()
//        coroutineScope.launch {
//            while (isActive) {
//                delay(2000)
//                loadingView?.visibility = VISIBLE
//            }
//        }
//    }

    fun removeOverLay() {
        try {
            overlayView?.let {
                windowManager?.removeView(it)
                overlayView = null
                params = null
            }

        } catch (e: Exception) {
            Log.d("window close", e.toString())
        }
    }
}
