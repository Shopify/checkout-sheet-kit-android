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
package com.shopify.checkoutsheetkit

import kotlinx.serialization.Serializable

/**
 * Generic RPC response structure matching the JSON-RPC 2.0 specification.
 * Mirrors the Swift RPCResponse class.
 *
 * @param R The type of the result payload
 */
@Serializable
public data class RPCResponse<R>(
    /**
     * A String specifying the version of the JSON-RPC protocol. MUST be exactly "2.0".
     */
    val jsonrpc: String = "2.0",

    /**
     * This member is REQUIRED.
     * It MUST be the same as the value of the id member in the Request Object.
     * If there was an error in detecting the id in the Request object (e.g. Parse error/Invalid Request), it MUST be Null.
     */
    val id: String? = null,

    /**
     * This member is REQUIRED on success.
     * This member MUST NOT exist if there was an error invoking the method.
     * The value of this member is determined by the method invoked on the Server.
     */
    val result: R? = null,

    /**
     * This member is REQUIRED on error.
     * This member MUST NOT exist if there was no error triggered during invocation.
     */
    val error: String? = null
) {
    init {
        require((result != null) xor (error != null)) {
            "RPCResponse must have either a result or an error, but not both or neither"
        }
    }
}