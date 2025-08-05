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
 * Configuration for Shopify Checkout Sheet Kit.
 *
 * Allows:
 * - Enabling/disabling preloading,
 * - Specifying the colorScheme that should be used for checkout,
 * - Setting customer privacy consent preferences.
 */
public data class Configuration internal constructor(
    var colorScheme: ColorScheme = ColorScheme.Automatic(),
    var preloading: Preloading = Preloading(),
    var errorRecovery: ErrorRecovery = object : ErrorRecovery {},
    var platform: Platform? = null,
    var logLevel: LogLevel = LogLevel.WARN,
    var privacyConsent: PrivacyConsent? = null,
)

/**
 * Configuration related to preloading.
 *
 * Initially allows toggling the preloading feature.
 */
public data class Preloading(
    val enabled: Boolean = true
)

public enum class LogLevel {
    DEBUG, WARN, ERROR
}

public interface ErrorRecovery {
    public fun preRecoveryActions(exception: CheckoutException, checkoutUrl: String) {
        // logging or pre-recovery cleanup can be added here
    }

    public fun shouldRecoverFromError(checkoutException: CheckoutException): Boolean {
        return checkoutException.isRecoverable
    }
}

public enum class Platform(public val displayName: String) {
    REACT_NATIVE("ReactNative")
}

/**
 * Customer privacy consent preferences for checkout.
 *
 * Represents the customer's consent for different types of data processing and tracking.
 * Each consent type can be independently enabled or disabled based on customer preferences.
 */
public data class PrivacyConsent(
    val marketing: Boolean = false,
    val analytics: Boolean = false,
    val preferences: Boolean = false,
    val saleOfData: Boolean = false,
) {
    public companion object {
        /**
         * No consents granted - all privacy settings disabled.
         */
        @JvmStatic
        public val none: PrivacyConsent = PrivacyConsent()

        /**
         * All consents granted - all privacy settings enabled.
         */
        @JvmStatic
        public val all: PrivacyConsent = PrivacyConsent(
            marketing = true,
            analytics = true,
            preferences = true,
            saleOfData = true
        )
    }

    /**
     * Creates a new PrivacyConsent instance with the specified consent types enabled.
     *
     * @param consents Variable number of ConsentType values to enable
     * @return New PrivacyConsent instance with specified consents enabled
     */
    public fun withConsents(vararg consents: ConsentType): PrivacyConsent {
        return copy(
            marketing = consents.contains(ConsentType.MARKETING) || marketing,
            analytics = consents.contains(ConsentType.ANALYTICS) || analytics,
            preferences = consents.contains(ConsentType.PREFERENCES) || preferences,
            saleOfData = consents.contains(ConsentType.SALE_OF_DATA) || saleOfData
        )
    }

    /**
     * Checks if a specific consent type is granted.
     *
     * @param consentType The consent type to check
     * @return true if the consent is granted, false otherwise
     */
    public fun hasConsent(consentType: ConsentType): Boolean {
        return when (consentType) {
            ConsentType.MARKETING -> marketing
            ConsentType.ANALYTICS -> analytics
            ConsentType.PREFERENCES -> preferences
            ConsentType.SALE_OF_DATA -> saleOfData
        }
    }

    /**
     * Individual consent types that can be granted or revoked.
     */
    public enum class ConsentType {
        /** Consent for marketing communications and promotional content */
        MARKETING,
        /** Consent for analytics and usage tracking */
        ANALYTICS,
        /** Consent for storing user preferences and personalization */
        PREFERENCES,
        /** Consent for sale of personal data to third parties */
        SALE_OF_DATA
    }
}
