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

internal class AuthenticationTracker {
    private var sentToken: String? = null
    private var pendingToken: String? = null

    /**
     * Determines if the token should be sent in the URL and marks it as pending.
     * Call this before loading a checkout to decide whether to include the token.
     *
     * @param authentication The authentication to potentially send
     * @return true if the token should be included in the URL
     */
    fun shouldSendToken(authentication: Authentication): Boolean {
        val token = when (authentication) {
            is Authentication.Token -> authentication.value
            is Authentication.None -> null
        }

        if (token == null) {
            reset()
            return false
        }

        val needsToSend = token != sentToken
        pendingToken = if (needsToSend) token else null
        return needsToSend
    }

    /**
     * Checks if the token should be retained in the URL during navigation without modifying state.
     * Use this during subsequent navigations to determine if the token is still required.
     *
     * @param authentication The authentication to check
     * @return true if the token should be retained in the URL
     */
    fun shouldRetainToken(authentication: Authentication): Boolean {
        val token = when (authentication) {
            is Authentication.Token -> authentication.value
            is Authentication.None -> null
        }

        if (token == null) {
            return false
        }
        return token != sentToken
    }

    /**
     * Confirms that the pending token was successfully sent.
     * Call this after the page finishes loading successfully.
     */
    fun confirmTokenSent() {
        pendingToken?.let {
            sentToken = it
        }
        pendingToken = null
    }

    fun reset() {
        sentToken = null
        pendingToken = null
    }
}
