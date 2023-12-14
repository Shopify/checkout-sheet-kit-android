package com.shopify.checkoutkit.messages

import kotlinx.serialization.Serializable

internal enum class SDKToWebMessageType(val key: String) {
    PRESENTED("presented")
}

@Serializable
internal data class SDKToWebEvent<T>(
    val detail: T
)

@Serializable
internal data class WebToSDKMessage(
    val name: String,
    val body: String = ""
)

@Serializable
internal data class InstrumentationPayload(
    val name: String,
    val value: Long,
    val type: InstrumentationType,
    val tags: Map<String, String>
)

@Suppress("EnumEntryName", "EnumNaming")
@Serializable
internal enum class InstrumentationType {
    histogram, incrementCounter
}
