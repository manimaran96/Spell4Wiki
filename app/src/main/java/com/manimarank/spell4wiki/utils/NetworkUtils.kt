package com.manimarank.spell4wiki.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong

/**
 * Utility class for Network/Internet related functions
 */
object NetworkUtils {

    fun isConnected(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false

            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

            // Check if the network has internet capability and is actually validated
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
            (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
             networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
             networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        } catch (_: Exception) {
            // If there's any exception, assume no connection
            false
        }
    }

    /**
     * Check if device has internet connectivity and show appropriate error message if not
     * @param context Application context
     * @param rootView Optional root view for showing snackbar
     * @return true if connected, false otherwise
     */
    fun checkConnectivityWithFeedback(context: Context, rootView: android.view.View? = null): Boolean {
        val isConnected = isConnected(context)
        if (!isConnected) {
            val errorMessage = context.getString(R.string.check_internet)
            if (rootView != null) {
                showLong(rootView, errorMessage)
            } else {
                ToastUtils.showLong(errorMessage)
            }
        }
        return isConnected
    }

    /**
     * Validate network operation before execution
     * @param context Application context
     * @param rootView Optional root view for error display
     * @param operation Lambda to execute if network is available
     */
    fun executeWithNetworkCheck(
        context: Context,
        rootView: android.view.View? = null,
        operation: () -> Unit
    ) {
        if (checkConnectivityWithFeedback(context, rootView)) {
            operation()
        }
    }


}