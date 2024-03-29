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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.di

import android.app.Application
import androidx.room.Room
import com.shopify.buy3.GraphClient
import com.shopify.checkout_sdk_mobile_buy_integration_sample.BuildConfig
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.CartViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.client.StorefrontClient
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.LogDatabase
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.Logger
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.MIGRATION_1_2
import com.shopify.checkout_sdk_mobile_buy_integration_sample.logs.LogsViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.product.ProductViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.PreferencesManager
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun setupDI(application: Application) {
    startKoin {
        androidLogger()
        androidContext(application)
        modules(appModules)
    }
}

val appModules = module {
    // App-wide components
    singleOf(::PreferencesManager)
    singleOf(::StorefrontClient)
    single {
        GraphClient.build(
            context = get(),
            accessToken = BuildConfig.storefrontAccessToken,
            shopDomain = BuildConfig.storefrontDomain
        )
    }

    single {
        Room.databaseBuilder(
            get(),
            LogDatabase::class.java,
            "log-db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    single {
        Logger(
            logDb = get(),
            coroutineScope = CoroutineScope(Dispatchers.IO),
        )
    }

    // Compose view models
    viewModelOf(::CartViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ProductViewModel)
    viewModelOf(::LogsViewModel)
}
