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

/**
 * Interface for fragments that handle checkout address change requests.
 * 
 * Fragments implementing this interface will automatically receive the
 * [CheckoutAddressChangeIntentEvent] when used with the checkout controller DSL.
 * 
 * Example usage:
 * ```kotlin
 * class AddressSelectionFragment : Fragment(), CheckoutAddressRequestReceiver {
 *     private var addressEvent: CheckoutAddressChangeIntentEvent? = null
 *     
 *     override fun onAddressChangeRequest(event: CheckoutAddressChangeIntentEvent) {
 *         this.addressEvent = event
 *         // Configure UI to call event.respondWith() or event.cancel()
 *     }
 * }
 * ```
 */
public interface CheckoutAddressRequestReceiver {
    /**
     * Called when the checkout requests address selection from the user.
     * 
     * @param event The address change request containing address type information
     *              and methods to respond or cancel the operation
     */
    public fun onAddressChangeRequest(event: CheckoutAddressChangeIntentEvent)
}
