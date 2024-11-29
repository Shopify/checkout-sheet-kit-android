package com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.shopify.checkout_sdk_mobile_buy_integration_sample.BuildConfig
import timber.log.Timber

/**
 * WebView used to display the login and logout pages and intercept authorization code param redirects
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AuthenticationWebView(
    url: String,
    modifier: Modifier = Modifier,
    onCodeParamIntercepted: (String) -> Unit = {},
    onPageComplete: (String) -> Unit = {},
) {
    AndroidView(
        factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.apply {
                    javaScriptEnabled = true
                }
                webViewClient = AuthenticationWebViewClient(
                    onCodeParamIntercepted,
                    onPageComplete
                )
            }
        },
        update = { it.loadUrl(url) },
        modifier = modifier,
    )
}

class AuthenticationWebViewClient(
    private val onCodeParamIntercepted: (String) -> Unit,
    private val onPageComplete: (String) -> Unit,
) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        if ("${request.url.scheme}://${request.url.host}" != BuildConfig.customerAccountsApiRedirectUri) {
            return super.shouldOverrideUrlLoading(view, request)
        }

        val codeQueryParam = request.url.getQueryParameter("code") ?: return super.shouldOverrideUrlLoading(view, request)

        onCodeParamIntercepted(codeQueryParam)
        return true
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        Timber.i("url finished loading $url")
        onPageComplete(url)
    }

    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        super.onReceivedError(view, request, error)
        Timber.e("Error when loading ${request.url}, $error")
    }
}
