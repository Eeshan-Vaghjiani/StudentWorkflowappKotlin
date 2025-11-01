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
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Observes network connectivity changes and provides a Flow of connection status. Used to trigger
 * automatic retry of failed messages when network is restored.
 */
class NetworkConnectivityObserver(private val context: Context) {

    private val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    companion object {
        private const val TAG = "NetworkConnectivity"
    }

    /**
     * Observe network connectivity changes. Emits true when network is available, false when
     * unavailable.
     */
    fun observe(): Flow<Boolean> =
            callbackFlow {
                        val callback =
                                object : ConnectivityManager.NetworkCallback() {
                                    private val networks = mutableSetOf<Network>()

                                    override fun onAvailable(network: Network) {
                                        networks.add(network)
                                        Log.d(
                                                TAG,
                                                "Network available: $network (total: ${networks.size})"
                                        )
                                        trySend(true)
                                    }

                                    override fun onLost(network: Network) {
                                        networks.remove(network)
                                        Log.d(
                                                TAG,
                                                "Network lost: $network (remaining: ${networks.size})"
                                        )
                                        trySend(networks.isNotEmpty())
                                    }

                                    override fun onCapabilitiesChanged(
                                            network: Network,
                                            networkCapabilities: NetworkCapabilities
                                    ) {
                                        val hasInternet =
                                                networkCapabilities.hasCapability(
                                                        NetworkCapabilities.NET_CAPABILITY_INTERNET
                                                )
                                        val isValidated =
                                                networkCapabilities.hasCapability(
                                                        NetworkCapabilities.NET_CAPABILITY_VALIDATED
                                                )

                                        Log.d(
                                                TAG,
                                                "Network capabilities changed: hasInternet=$hasInternet, validated=$isValidated"
                                        )

                                        if (hasInternet && isValidated) {
                                            trySend(true)
                                        }
                                    }
                                }

                        val request =
                                NetworkRequest.Builder()
                                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                                        .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                                        .build()

                        connectivityManager.registerNetworkCallback(request, callback)

                        // Send initial state
                        trySend(isNetworkAvailable())

                        awaitClose {
                            Log.d(TAG, "Unregistering network callback")
                            connectivityManager.unregisterNetworkCallback(callback)
                        }
                    }
                    .distinctUntilChanged()

    /** Check if network is currently available */
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /** Check if network is connected (may not have internet access) */
    fun isNetworkConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}
