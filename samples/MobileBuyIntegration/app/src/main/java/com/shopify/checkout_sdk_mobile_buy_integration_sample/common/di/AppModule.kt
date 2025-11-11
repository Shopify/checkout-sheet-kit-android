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
import com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.data.CheckoutAppAuthenticationService
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
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.account.AccountViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.LoginViewModel
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.CustomerRepository
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.local.CustomerAccessTokenStore
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.network.CustomerAccountsApiGraphQLClient
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.data.source.network.CustomerAccountsApiRestClient
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.authentication.utils.CustomerAuthenticationHelper
import com.shopify.checkout_sdk_mobile_buy_integration_sample.settings.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
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

    // Serialization
    single { Json { ignoreUnknownKeys = true } }

    // Storage for customer access tokens
    single {
        CustomerAccessTokenStore(
            appContext = androidApplication().applicationContext,
        )
    }

    // Logs
    single { Logger(logDb = get(), coroutineScope = CoroutineScope(Dispatchers.IO)) }
    single {
        Room.databaseBuilder(get(), LogDatabase::class.java, "log-db")
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    // API Clients
    singleOf(::CartStorefrontApiClient)
    singleOf(::ProductsStorefrontApiClient)
    singleOf(::ProductCollectionsStorefrontApiClient)
    single {
        CustomerAccountsApiRestClient(
            client = OkHttpClient(),
            json = get(),
            helper = get(),
            redirectUri = BuildConfig.customerAccountApiRedirectUri,
            clientId = BuildConfig.customerAccountApiClientId
        )
    }
    single {
        CustomerAccountsApiGraphQLClient(
            client = OkHttpClient(),
            json = get(),
            baseUrl = BuildConfig.customerAccountApiGraphQLBaseUrl,
        )
    }
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

    single {
        CustomerAuthenticationHelper(
            baseUrl = BuildConfig.customerAccountApiAuthBaseUrl,
            redirectUri = BuildConfig.customerAccountApiRedirectUri,
            clientId = BuildConfig.customerAccountApiClientId
        )
    }

    single {
        CheckoutAppAuthenticationService(
            client = OkHttpClient(),
            json = get(),
            authEndpoint = BuildConfig.checkoutAuthEndpoint,
            clientId = BuildConfig.checkoutAuthClientId,
            clientSecret = BuildConfig.checkoutAuthClientSecret,
        )
    }

    // Repositories
    singleOf(::CartRepository)
    singleOf(::ProductRepository)
    singleOf(::ProductCollectionRepository)
    singleOf(::CustomerRepository)
    singleOf(::SettingsRepository)

    // Compose view models
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ProductCollectionViewModel)
    viewModelOf(::ProductViewModel)
    viewModelOf(::ProductsViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::LogsViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::AccountViewModel)
    single {
        // singleton instance of shared cart view model
        CartViewModel(get(), get(), get(), get())
    }
}
