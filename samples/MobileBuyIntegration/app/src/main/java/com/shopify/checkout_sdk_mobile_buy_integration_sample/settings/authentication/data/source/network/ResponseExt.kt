package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.network

import okhttp3.Response
import java.io.IOException

fun Response.bodyOrThrow() = this.body.use {
    if (it == null) {
        throw IOException("Unexpected empty response body")
    }
    it.string()
}
