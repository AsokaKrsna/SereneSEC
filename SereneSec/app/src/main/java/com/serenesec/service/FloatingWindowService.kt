package com.serenesec.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.serenesec.MainActivity
import com.serenesec.R

/**
 * Floating window service for reading articles in an overlay
 */
class FloatingWindowService : Service() {
    
    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var webView: WebView? = null
    
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    
    companion object {
        const val CHANNEL_ID = "floating_window_channel"
        const val NOTIFICATION_ID = 2001
        
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
        
        const val ACTION_SHOW = "com.serenesec.SHOW_FLOATING"
        const val ACTION_CLOSE = "com.serenesec.CLOSE_FLOATING"
        
        fun startFloating(context: Context, url: String, title: String) {
            val intent = Intent(context, FloatingWindowService::class.java).apply {
                action = ACTION_SHOW
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_TITLE, title)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopFloating(context: Context) {
            val intent = Intent(context, FloatingWindowService::class.java).apply {
                action = ACTION_CLOSE
            }
            context.startService(intent)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW -> {
                val url = intent.getStringExtra(EXTRA_URL) ?: return START_NOT_STICKY
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Article"
                showFloatingWindow(url, title)
            }
            ACTION_CLOSE -> {
                closeFloatingWindow()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }
    
    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    private fun showFloatingWindow(url: String, title: String) {
        if (floatingView != null) {
            // Update existing window
            webView?.loadUrl(url)
            return
        }
        
        // Start as foreground service
        startForeground(NOTIFICATION_ID, createNotification(title))
        
        // Inflate floating layout
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_window, null)
        
        // Setup window params
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            (resources.displayMetrics.heightPixels * 0.5).toInt(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 100
        }
        
        // Setup views
        val titleView = floatingView?.findViewById<TextView>(R.id.floating_title)
        val closeButton = floatingView?.findViewById<ImageButton>(R.id.btn_close)
        val expandButton = floatingView?.findViewById<ImageButton>(R.id.btn_expand)
        val dragHandle = floatingView?.findViewById<View>(R.id.drag_handle)
        webView = floatingView?.findViewById(R.id.floating_webview)
        
        titleView?.text = title
        
        // Setup WebView
        webView?.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            webViewClient = WebViewClient()
            loadUrl(url)
        }
        
        // Close button
        closeButton?.setOnClickListener {
            closeFloatingWindow()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        
        // Expand button - open in main app
        expandButton?.setOnClickListener {
            val mainIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("open_url", url)
            }
            startActivity(mainIntent)
            closeFloatingWindow()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        
        // Drag functionality
        dragHandle?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingView, layoutParams)
                    true
                }
                else -> false
            }
        }
        
        // Add to window manager
        windowManager.addView(floatingView, layoutParams)
    }
    
    private fun closeFloatingWindow() {
        floatingView?.let {
            webView?.destroy()
            webView = null
            windowManager.removeView(it)
            floatingView = null
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Floating Reader",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when reading in floating window"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(title: String): Notification {
        val closeIntent = Intent(this, FloatingWindowService::class.java).apply {
            action = ACTION_CLOSE
        }
        val closePending = PendingIntent.getService(
            this, 0, closeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Reading: $title")
            .setContentText("Tap to close floating window")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .addAction(0, "Close", closePending)
            .build()
    }
    
    override fun onDestroy() {
        closeFloatingWindow()
        super.onDestroy()
    }
}
