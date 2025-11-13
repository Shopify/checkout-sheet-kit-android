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
package com.shopify.checkout_kit_mobile_buy_integration_sample.settings.authentication

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * WebView used to display the login page and intercept authorization code param redirects
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginWebView(
    url: String,
    modifier: Modifier = Modifier,
    customerAccountApiRedirectUri: String,
    onCodeParamIntercepted: (String) -> Unit = {},
) {
    AndroidView(
        factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.apply { javaScriptEnabled = true }
                webViewClient = AuthenticationWebViewClient(customerAccountApiRedirectUri, onCodeParamIntercepted)
            }
        },
        update = { it.loadUrl(url) },
        modifier = modifier,
    )
}

/**
 * Override URL loading when redirected to the
 * [redirect_uri](https://shopify.dev/docs/api/customer#authorization-propertydetail-redirecturi)
 * with a [code](https://shopify.dev/docs/api/customer#step-code) query parameter.
 */
class AuthenticationWebViewClient(
    private val customerAccountApiRedirectUri: String,
    private val onCodeParamIntercepted: (String) -> Unit
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        if ("${request.url.scheme}://${request.url.host}" != customerAccountApiRedirectUri) {
            return super.shouldOverrideUrlLoading(view, request)
        }

        val codeQueryParam = request.url.getQueryParameter("code") ?: return super.shouldOverrideUrlLoading(view, request)

        onCodeParamIntercepted(codeQueryParam)
        return true
    }
}
