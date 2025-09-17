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

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

/**
 * DSL marker to prevent external scopes from being used within the checkout controller DSL.
 */
@DslMarker
public annotation class CheckoutControllerDsl

/**
 * Create a [ShopifyCheckoutController] using Android-idiomatic DSL syntax.
 * 
 * Example usage:
 * ```kotlin
 * val controller = checkoutController(checkoutUrl, eventProcessor) {
 *     addressScreen {
 *         fragment(AddressSelectionFragment()) {
 *             titleRes = R.string.address_selection_title
 *         }
 *     }
 * }
 * 
 * controller.present(this)
 * ```
 * 
 * @param checkoutUrl The checkout URL to load
 * @param eventProcessor The event processor for handling checkout events
 * @param configuration DSL block for configuring the controller
 * @return A configured [ShopifyCheckoutController] instance
 */
public fun checkoutController(
    checkoutUrl: String,
    eventProcessor: CheckoutEventProcessor,
    configuration: CheckoutControllerConfiguration.() -> Unit
): ShopifyCheckoutController {
    require(checkoutUrl.isNotBlank()) { "Checkout URL cannot be blank" }
    
    val config = CheckoutControllerConfiguration().apply(configuration)
    
    return ShopifyCheckoutController.Builder(checkoutUrl, eventProcessor)
        .apply {
            config.addressScreenProvider?.let { provider ->
                setAddressScreenProvider(provider)
            }
        }
        .build()
}

/**
 * Configuration DSL scope for [ShopifyCheckoutController].
 */
@CheckoutControllerDsl
public class CheckoutControllerConfiguration {
    public var addressScreenProvider: ((CheckoutAddressChangeIntentEvent) -> CheckoutScreen)? = null
    
    /**
     * Configure the screen to show when address selection is required.
     * 
     * @param configuration Function that creates a [CheckoutScreen] for address selection
     */
    public fun addressScreen(configuration: AddressScreenScope.() -> CheckoutScreen) {
        addressScreenProvider = { event ->
            AddressScreenScope(event).configuration()
        }
    }
}

/**
 * DSL scope for address screen configuration.
 */
@CheckoutControllerDsl
public class AddressScreenScope(private val event: CheckoutAddressChangeIntentEvent) {
    
    /**
     * Create a fragment-based screen with a pre-configured fragment instance.
     * 
     * If the fragment implements [CheckoutAddressRequestReceiver], the address change request will be
     * automatically provided via [CheckoutAddressRequestReceiver.onAddressChangeRequest].
     * 
     * @param fragment The fragment instance to use
     * @param configuration DSL block for configuring the screen
     * @return A configured [CheckoutScreen.Fragment]
     */
    public fun fragment(
        fragment: Fragment,
        configuration: FragmentScreenConfiguration.() -> Unit = {}
    ): CheckoutScreen.Fragment {
        val config = FragmentScreenConfiguration().apply(configuration)
        
        val screenConfig = when {
            config.titleRes != null -> CheckoutScreenConfig.withTitle(config.titleRes!!)
            config.title != null -> CheckoutScreenConfig.withTitle(config.title!!)
            else -> CheckoutScreenConfig()
        }
        
        // Automatically provide address change request if fragment implements the interface
        if (fragment is CheckoutAddressRequestReceiver) {
            fragment.onAddressChangeRequest(event)
        }
        
        return CheckoutScreen.Fragment(view = fragment, config = screenConfig)
    }
    
    /**
     * Access the current address change event for manual fragment configuration.
     */
    public val addressEvent: CheckoutAddressChangeIntentEvent get() = event
}


/**
 * DSL configuration for fragment screens.
 */
@CheckoutControllerDsl
public class FragmentScreenConfiguration {
    /**
     * Title to display in the toolbar.
     */
    public var title: String? = null
    
    /**
     * String resource ID for the title to display in the toolbar.
     * Takes precedence over [title] if both are set.
     */
    @StringRes
    public var titleRes: Int? = null
}