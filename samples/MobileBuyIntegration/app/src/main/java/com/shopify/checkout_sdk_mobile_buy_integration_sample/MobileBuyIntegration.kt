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
package com.shopify.checkout_sdk_mobile_buy_integration_sample

import android.app.Application
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.CookiePurger
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.di.setupDI
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.Logger
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.withCustomCloseIcon
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.PreferencesManager
import com.shopify.checkoutsheetkit.CheckoutException
import com.shopify.checkoutsheetkit.ErrorRecovery
import com.shopify.checkoutsheetkit.HttpException
import com.shopify.checkoutsheetkit.LogLevel
import com.shopify.checkoutsheetkit.ShopifyCheckoutSheetKit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

class MobileBuyIntegration : Application() {

    private val applicationScope = CoroutineScope(Job())

    override fun onCreate() {
        super.onCreate()
        setupDI(application = this)
        initCheckoutSdk(preferencesManager = get())
    }

    /**
     * Configures the Checkout SDK with the most recent user preferences.
     */
    private fun initCheckoutSdk(preferencesManager: PreferencesManager) {
        applicationScope.launch {
            val settings = preferencesManager.userPreferencesFlow.first()
            ShopifyCheckoutSheetKit.configure {
                it.logLevel = LogLevel.DEBUG
                it.colorScheme = settings.colorScheme.withCustomCloseIcon()
                it.preloading = settings.preloading
                it.errorRecovery = object : ErrorRecovery {
                    val logger: Logger by inject()

                    override fun preRecoveryActions(exception: CheckoutException, checkoutUrl: String) {
                        logger.log("Falling back")
                        if (exception is HttpException) {
                            CookiePurger.purge(checkoutUrl)
                        }
                    }
                }
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }
}
