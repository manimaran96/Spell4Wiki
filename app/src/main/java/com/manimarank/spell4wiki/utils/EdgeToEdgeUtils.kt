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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // For older versions, use WindowCompat
            WindowCompat.setDecorFitsSystemWindows(window, false)
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
            // Only handle bottom insets if needed
            view.updatePadding(
                top = 0, // Toolbar handles top spacing
                bottom = 0 // Let WebView handle bottom insets naturally
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
}
