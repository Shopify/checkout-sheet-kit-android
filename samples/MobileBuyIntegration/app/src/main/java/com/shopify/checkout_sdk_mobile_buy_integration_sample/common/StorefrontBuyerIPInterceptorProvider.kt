package com.shopify.checkout_sdk_mobile_buy_integration_sample.common

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import timber.log.Timber

/**
 * Fetches an IP to pass in with Storefront API requests via the Shopify-Storefront-Buyer-IP header
 */
class StorefrontBuyerIPInterceptorProvider(
    private val url: String,
    private val client: OkHttpClient,
) {
    private var ipAddress: String? = null

    fun interceptor(chain: Interceptor.Chain): Response {
        val ip = ipAddress()
        Timber.i("Setting Shopify-Storefront-Buyer-IP = $ip on request")
        val original = chain.request()
        val builder = original
            .newBuilder()
            .method(original.method, original.body)
            .header("Shopify-Storefront-Buyer-IP", ip ?: "")
        return chain.proceed(builder.build())
    }

    private fun ipAddress(): String? {
        if (ipAddress != null) {
            Timber.i("Returning already fetched IP $ipAddress")
            return ipAddress
        }

        Timber.i("Fetching IP address from remote service")
        client.newCall(
            Request.Builder()
                .get()
                .url(url)
                .build()
        ).execute().use { response ->
            response.body.use { body: ResponseBody? ->
                this.ipAddress = body?.string()
                Timber.i("Received ${this.ipAddress}")
                return this.ipAddress
            }
        }
    }
}
