package com.liuguanyi.liumusic.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object LiuMusicNetworkAvailability {
    fun isOnline(context: Context): Boolean {
        val manager = context.applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return true
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
