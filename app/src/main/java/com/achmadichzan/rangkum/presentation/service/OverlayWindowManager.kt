package com.achmadichzan.rangkum.presentation.service

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class OverlayWindowManager(private val context: Context) {
    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    lateinit var params: WindowManager.LayoutParams
        private set

    private var lastWidth = 0
    private var lastHeight = 0
    private var screenWidth = 0
    private var screenHeight = 0
    private var density = 0f
    var isCollapsed by mutableStateOf(false)
        private set

    init {
        updateScreenDimensions()
    }

    fun createParams(): WindowManager.LayoutParams {
        val metrics = context.resources.displayMetrics
        val initialWidth = (300 * metrics.density).toInt()
        val initialHeight = (400 * metrics.density).toInt()

        lastWidth = initialWidth
        lastHeight = initialHeight

        params = WindowManager.LayoutParams(
            initialWidth,
            initialHeight,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.y = 200
        return params
    }

    fun addView(view: View) {
        try {
            if (view.parent == null) {
                windowManager.addView(view, params)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeView(view: View) {
        try {
            if (view.parent != null) {
                windowManager.removeView(view)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updatePosition(view: View, deltaX: Float, deltaY: Float) {
        val metrics = context.resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels

        val newX = params.x + deltaX.toInt()
        val newY = params.y + deltaY.toInt()

        val maxX = screenWidth - 100
        val maxY = screenHeight - 100

        params.x = newX.coerceIn(-50, maxX)
        params.y = newY.coerceIn(-50, maxY)

        updateViewLayout(view)
    }

    fun updateScreenDimensions() {
        val metrics = context.resources.displayMetrics
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        density = metrics.density
    }

    fun resize(view: View, deltaWidth: Float, deltaHeight: Float) {
        if (isCollapsed) return

        val minWidth = (200 * density).toInt()
        val minHeight = (150 * density).toInt()

        val targetWidth = params.width + deltaWidth.toInt()
        val targetHeight = params.height + deltaHeight.toInt()

        val effectiveX = params.x.coerceAtLeast(0)
        val effectiveY = params.y.coerceAtLeast(0)

        val maxWidthAllowed = screenWidth - effectiveX
        val maxHeightAllowed = screenHeight - effectiveY

        params.width = targetWidth.coerceIn(minWidth, maxWidthAllowed)
        params.height = targetHeight.coerceIn(minHeight, maxHeightAllowed)

        lastWidth = params.width
        lastHeight = params.height

        updateViewLayout(view)
    }

    fun adjustToScreenSize(view: View) {
        val metrics = context.resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels

        if (params.width > screenWidth) {
            params.width = screenWidth
        }

        if (params.height > screenHeight) {
            params.height = screenHeight
        }

        if (params.x + params.width > screenWidth) {
            params.x = screenWidth - params.width
        }
        if (params.x < 0) params.x = 0

        if (params.y + params.height > screenHeight) {
            params.y = screenHeight - params.height
        }
        if (params.y < 0) params.y = 0

        lastWidth = params.width
        lastHeight = params.height

        updateViewLayout(view)
    }

    fun toggleCollapse(view: View) {
        val metrics = context.resources.displayMetrics
        val bubbleSize = (60 * metrics.density).toInt()

        if (isCollapsed) {
            params.width = lastWidth
            params.height = lastHeight
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
            isCollapsed = false
        } else {
            lastWidth = params.width
            lastHeight = params.height
            params.width = bubbleSize
            params.height = bubbleSize
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            isCollapsed = true
        }
        updateViewLayout(view)
    }

    fun setOpacity(view: View, alpha: Float) {
        params.alpha = alpha
        updateViewLayout(view)
    }

    private fun updateViewLayout(view: View) {
        try {
            windowManager.updateViewLayout(view, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}