package com.manimarank.spell4wiki.utils

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

/**
 * Utility class for handling edge-to-edge display and window insets
 */
object EdgeToEdgeUtils {

    /**
     * Enable edge-to-edge display for the activity
     */
    fun Activity.enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    /**
     * Apply window insets to a view with padding
     */
    fun View.applyWindowInsets(
        applyLeft: Boolean = false,
        applyTop: Boolean = true,
        applyRight: Boolean = false,
        applyBottom: Boolean = true
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            view.updatePadding(
                left = if (applyLeft) insets.left else view.paddingLeft,
                top = if (applyTop) insets.top else view.paddingTop,
                right = if (applyRight) insets.right else view.paddingRight,
                bottom = if (applyBottom) insets.bottom else view.paddingBottom
            )
            
            windowInsets
        }
    }

    /**
     * Apply window insets to a view with margins
     */
    fun View.applyWindowInsetsWithMargin(
        applyLeft: Boolean = false,
        applyTop: Boolean = true,
        applyRight: Boolean = false,
        applyBottom: Boolean = true
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = if (applyLeft) insets.left else leftMargin
                topMargin = if (applyTop) insets.top else topMargin
                rightMargin = if (applyRight) insets.right else rightMargin
                bottomMargin = if (applyBottom) insets.bottom else bottomMargin
            }
            
            windowInsets
        }
    }

    /**
     * Apply window insets to toolbar/app bar
     */
    fun View.applyTopWindowInsets() {
        applyWindowInsets(applyTop = true, applyBottom = false)
    }

    /**
     * Apply window insets to bottom navigation or floating action buttons
     */
    fun View.applyBottomWindowInsets() {
        applyWindowInsets(applyTop = false, applyBottom = true)
    }

    /**
     * Apply window insets to content that should avoid system bars
     */
    fun View.applySystemBarInsets() {
        applyWindowInsets(applyTop = true, applyBottom = true)
    }

    /**
     * Apply window insets specifically for WebView containers
     * Only applies bottom insets, assuming toolbar handles top insets
     */
    fun View.applyWebViewInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // For WebView with toolbar, don't apply top padding as toolbar handles it
            view.updatePadding(
                top = 0, // Toolbar handles top spacing
                bottom = insets.bottom 
            )

            windowInsets
        }
    }

    /**
     * Apply window insets for activities with toolbar
     */
    fun Activity.setupEdgeToEdgeWithToolbar(
        rootView: View,
        toolbar: View? = null
    ) {
        enableEdgeToEdge()

        // If toolbar is provided, apply top insets to it
        if (toolbar != null) {
            toolbar.applyTopWindowInsets()
        } else {
            // If no toolbar, apply top insets to root view to avoid status bar overlap
            rootView.applyTopWindowInsets()
        }
    }

    /**
     * Setup proper status bar handling without edge-to-edge
     * Use this for activities that don't need edge-to-edge but need proper status bar spacing
     */
    fun Activity.setupStatusBarHandling(rootView: View) {
        // Don't enable edge-to-edge, just ensure proper status bar handling
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Only apply top padding to avoid status bar overlap
            view.updatePadding(
                top = insets.top
            )

            windowInsets
        }
    }

    /**
     * Setup minimal status bar spacing for activities with default action bar
     * Use this when the default action bar handles most spacing but content needs slight adjustment
     */
    fun Activity.setupMinimalStatusBarSpacing(rootView: View, additionalTopPadding: Int = 0) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Apply minimal top padding if needed
            if (additionalTopPadding > 0) {
                view.updatePadding(
                    top = view.paddingTop + additionalTopPadding
                )
            }

            windowInsets
        }
    }

    /**
     * Apply window insets for WebView activities
     */
    fun Activity.setupEdgeToEdgeForWebView(
        rootView: View,
        toolbar: View? = null,
        webViewContainer: View? = null
    ) {
        enableEdgeToEdge()

        if (toolbar != null) {
            // If there's a toolbar, apply top insets to it
            toolbar.applyTopWindowInsets()
            // WebView container doesn't need top padding as toolbar handles it
            webViewContainer?.applyWebViewInsets()
        } else {
            // If no toolbar, apply top insets to root view or WebView container
            if (webViewContainer != null) {
                webViewContainer.applyTopWindowInsets()
            } else {
                rootView.applySystemBarInsets()
            }
        }
    }
    /**
     * Apply window insets to a view, adding them to the initial margins
     * This is useful when you have a view with existing margins that needs to be pushed away from system bars
     */
    fun View.applyWindowInsetsWithMarginAddition(
        applyLeft: Boolean = false,
        applyTop: Boolean = false,
        applyRight: Boolean = false,
        applyBottom: Boolean = false
    ) {
        val initialLeft = (layoutParams as? ViewGroup.MarginLayoutParams)?.leftMargin ?: 0
        val initialTop = (layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin ?: 0
        val initialRight = (layoutParams as? ViewGroup.MarginLayoutParams)?.rightMargin ?: 0
        val initialBottom = (layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0

        ViewCompat.setOnApplyWindowInsetsListener(this) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                if (applyLeft) leftMargin = initialLeft + insets.left
                if (applyTop) topMargin = initialTop + insets.top
                if (applyRight) rightMargin = initialRight + insets.right
                if (applyBottom) bottomMargin = initialBottom + insets.bottom
            }
            
            windowInsets
        }
        
        // Request insets to be applied immediately
        if (isAttachedToWindow) {
            ViewCompat.requestApplyInsets(this)
        } else {
            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    v.removeOnAttachStateChangeListener(this)
                    ViewCompat.requestApplyInsets(v)
                }
                override fun onViewDetachedFromWindow(v: View) {}
            })
        }
    }
}
