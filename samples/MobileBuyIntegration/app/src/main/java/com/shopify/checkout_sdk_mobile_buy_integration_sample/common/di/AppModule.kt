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
package com.shopify.checkout_sdk_mobile_buy_integration_sample.common.di

import android.app.Application
import android.util.LruCache
import androidx.room.Room
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.GraphClient
import com.shopify.buy3.Storefront
import com.shopify.checkout_sdk_mobile_buy_integration_sample.BuildConfig
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.CartViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.data.CartRepository
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.data.source.network.CartStorefrontApiClient
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.client.StorefrontApiRequestExecutor
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.LogDatabase
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.Logger
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.MIGRATION_1_2
import com.shopify.checkout_sdk_mobile_buy_integration_sample.home.HomeViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.logs.LogsViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.ProductsViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection.ProductCollectionViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection.data.ProductCollectionRepository
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection.data.source.network.ProductCollectionsStorefrontApiClient
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.ProductViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.ProductRepository
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.source.network.ProductsStorefrontApiClient
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
    singleOf(::CartStorefrontApiClient)
    single {
        val maxCacheEntries = 100

        StorefrontApiRequestExecutor(
            lruCache = LruCache<String, GraphCallResult.Success<Storefront.QueryRoot>>(maxCacheEntries),
            client = GraphClient.build(
                context = get(),
                accessToken = BuildConfig.storefrontAccessToken,
                shopDomain = BuildConfig.storefrontDomain
            )
        )
    }

    singleOf(::ProductCollectionRepository)
    singleOf(::ProductCollectionsStorefrontApiClient)
    singleOf(::ProductRepository)
    singleOf(::ProductsStorefrontApiClient)
    singleOf(::CartRepository)
    singleOf(::CartStorefrontApiClient)

    single {
        // singleton instance of shared cart view model
        CartViewModel(
            get(),
            get(),
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
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ProductCollectionViewModel)
    viewModelOf(::ProductViewModel)
    viewModelOf(::ProductsViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::LogsViewModel)
}
