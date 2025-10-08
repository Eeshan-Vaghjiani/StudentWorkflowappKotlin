package com.example.loginandregistration.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Monitor network connectivity changes */
class ConnectionMonitor(private val context: Context) {

    companion object {
        private const val TAG = "ConnectionMonitor"
    }

    private val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /** Flow that emits true when connected, false when disconnected */
    val isConnected: Flow<Boolean> = callbackFlow {
        val callback =
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        Log.d(TAG, "Network available")
                        trySend(true)
                    }

                    override fun onLost(network: Network) {
                        Log.d(TAG, "Network lost")
                        trySend(false)
                    }

                    override fun onCapabilitiesChanged(
                            network: Network,
                            networkCapabilities: NetworkCapabilities
                    ) {
                        val hasInternet =
                                networkCapabilities.hasCapability(
                                        NetworkCapabilities.NET_CAPABILITY_INTERNET
                                )
                        Log.d(TAG, "Network capabilities changed: hasInternet=$hasInternet")
                        trySend(hasInternet)
                    }
                }

        val networkRequest =
                NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build()

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        // Send initial state
        trySend(isCurrentlyConnected())

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }

    /** Check if currently connected to internet */
    fun isCurrentlyConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
