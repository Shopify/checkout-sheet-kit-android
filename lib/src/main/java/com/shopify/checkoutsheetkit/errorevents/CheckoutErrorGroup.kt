/*
 * MIT License
 *
 * Copyright 2023-present, Shopify Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.shopify.checkoutsheetkit.errorevents

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = CheckoutErrorGroupSerializer::class)
internal enum class CheckoutErrorGroup(val value: String) {
    /** An authentication error */
    AUTHENTICATION("authentication"),

    /** A shop configuration error */
    CONFIGURATION("configuration"),

    /** A terminal checkout error which cannot be handled */
    UNRECOVERABLE("unrecoverable"),

    /** A checkout-related error, such as failure to receive a receipt or progress through checkout */
    CHECKOUT("checkout"),

    /** The checkout session has expired and is no longer available */
    EXPIRED("expired"),

    /** The error sent by checkout is unsupported */
    UNSUPPORTED("unsupported")
}

internal object CheckoutErrorGroupSerializer : KSerializer<CheckoutErrorGroup> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ErrorGroup", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: CheckoutErrorGroup) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): CheckoutErrorGroup {
        val value = decoder.decodeString()
        return CheckoutErrorGroup.entries.firstOrNull { it.value == value }
            ?: CheckoutErrorGroup.UNSUPPORTED
    }
}
