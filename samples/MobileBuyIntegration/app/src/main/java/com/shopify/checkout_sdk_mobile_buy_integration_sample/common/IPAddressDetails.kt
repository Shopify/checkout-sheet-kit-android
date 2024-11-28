package com.shopify.checkout_sdk_mobile_buy_integration_sample.common

import okhttp3.OkHttpClient
import okhttp3.Request

class IPAddressDetails(
    private val okHttpClient: OkHttpClient,
) {
    private val ipAddress: String? = null

    fun ipAddress(): String? {
        if (ipAddress != null) {
            return ipAddress
        }

        val request = Request.Builder().get().url("https://api64.ipify.org/").build()
        return okHttpClient.newCall(request).execute().use { response ->
            response.body?.string()
        }
    }
}
