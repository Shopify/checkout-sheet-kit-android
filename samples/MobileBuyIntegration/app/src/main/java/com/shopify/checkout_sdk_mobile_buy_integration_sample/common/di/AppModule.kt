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
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.IPAddressDetails
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.client.StorefrontApiRequestExecutor
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.LogDatabase
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.Logger
import com.shopify.checkout_sdk_mobile_buy_integration_sample.common.logs.MIGRATION_1_2
import com.shopify.checkout_sdk_mobile_buy_integration_sample.home.HomeViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.logs.LogsViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.ProductsViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection.CollectionViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection.data.CollectionRepository
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.collection.data.source.network.CollectionsStorefrontApiClient
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.ProductViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.ProductRepository
import com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data.source.network.ProductsStorefrontApiClient
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.PreferencesManager
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.SettingsViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.login.LoginViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.login.data.TokenRepository
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.login.data.source.local.TokenStore
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.login.data.source.network.CustomerAccountsApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
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
    single {
        IPAddressDetails(
            get()
        )
    }

    singleOf(::PreferencesManager)
    singleOf(::CartStorefrontApiClient)
    singleOf(::IPAddressDetails)
    single {
        val maxCacheEntries = 100
        val ipAddressDetails: IPAddressDetails = get()

        StorefrontApiRequestExecutor(
            lruCache = LruCache<String, GraphCallResult.Success<Storefront.QueryRoot>>(maxCacheEntries),
            client = GraphClient.build(
                context = get(),
                accessToken = BuildConfig.storefrontAccessToken,
                shopDomain = BuildConfig.storefrontDomain
            ) {
                // Modify HTTP Client to allow adding an IP header
                this.httpClient = this.httpClient.newBuilder()
                    .addInterceptor { chain ->
                        val original = chain.request()
                        ipAddressDetails.ipAddress()?.let { ipAddress ->
                            val builder = original
                                .newBuilder()
                                .method(original.method, original.body)
                                .header("Shopify-Storefront-Buyer-IP", ipAddress)
                            chain.proceed(builder.build())
                        }
                        chain.proceed(chain.request())
                    }
                    .build()
            }
        )
    }

    single {
        CustomerAccountsApiClient(
            client = OkHttpClient(),
            json = Json { ignoreUnknownKeys = true },
            restBaseUrl = "https://shopify.com/authentication/${BuildConfig.shopId}",
            graphQLBaseUrl = "https://shopify.com/${BuildConfig.shopId}/account/customer/api/2024-07/graphql",
            redirectUri = BuildConfig.customerAccountsApiRedirectUri,
            clientId = BuildConfig.customerAccountsApiClientId
        )
    }

    singleOf(::CollectionRepository)
    singleOf(::CollectionsStorefrontApiClient)
    singleOf(::ProductRepository)
    singleOf(::ProductsStorefrontApiClient)
    singleOf(::CartRepository)
    singleOf(::CartStorefrontApiClient)
    singleOf(::TokenRepository)
    single {
        TokenStore(
            appContext = androidApplication().applicationContext,
            scope = CoroutineScope(Dispatchers.Default),
        )
    }

    single {
        // singleton instance of shared cart view model
        CartViewModel(
            get(),
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
    viewModelOf(::CollectionViewModel)
    viewModelOf(::ProductViewModel)
    viewModelOf(::ProductsViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::LogsViewModel)
    viewModelOf(::LoginViewModel)
}
