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
package com.shopify.checkoutsheetkit.rpc

import android.webkit.WebView
import java.lang.ref.WeakReference

/**
 * Base interface for all RPC request implementations.
 * Mirrors the Swift RPCRequest protocol with type parameters for request params and response payload.
 *
 * @param P The type of parameters for this request
 * @param R The type of response payload this request expects
 */
public interface RPCRequest<P : Any, R : Any> {
    /**
     * The JSON-RPC version. Must be exactly "2.0".
     */
    public val jsonrpc: String

    /**
     * An identifier established by the client. If null, this is a notification.
     */
    public val id: String?

    /**
     * The parameters from the JSON-RPC request.
     */
    public val params: P

    /**
     * The RPC method name for this request type.
     */
    public val method: String

    /**
     * Whether this is a notification (no response expected).
     */
    public val isNotification: Boolean

    /**
     * Weak reference to the WebView for sending responses.
     */
    public var webView: WeakReference<WebView>?

    /**
     * Respond to this request with the specified payload.
     *
     * @param payload The response payload
     */
    public fun respondWith(payload: R)

    /**
     * Respond to this request with a JSON string.
     * Useful for language bindings (e.g., React Native).
     *
     * @param json A JSON string representing the response payload
     */
    public fun respondWith(json: String)

    /**
     * Respond to this request with an error.
     *
     * @param error The error message
     */
    public fun respondWithError(error: String)

    /**
     * Validate the response payload before sending.
     * Default implementation does nothing - subclasses can override.
     *
     * @param payload The payload to validate
     * @throws Exception if validation fails
     */
    public fun validate(payload: R)
}