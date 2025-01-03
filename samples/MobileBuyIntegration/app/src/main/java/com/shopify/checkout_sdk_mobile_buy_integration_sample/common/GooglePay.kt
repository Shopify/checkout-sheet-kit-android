package com.shopify.checkout_sdk_mobile_buy_integration_sample.common

import android.content.Context
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONArray
import org.json.JSONObject

object GooglePay {

    fun createPaymentsClient(context: Context, environment: Int = WalletConstants.ENVIRONMENT_TEST): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(environment)
            .build()

        return Wallet.getPaymentsClient(context, walletOptions)
    }

    fun isReadyToPayRequest(): JSONObject? = baseRequest.put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod()))

    fun allowedPaymentMethods(): JSONArray {
        return JSONArray().put(cardPaymentMethod)
    }

    fun getPaymentDataRequest(priceLabel: String, countryCode: String, currencyCode: String): JSONObject =
        baseRequest
            .put("allowedPaymentMethods", allowedPaymentMethods())
            .put("transactionInfo", getTransactionInfo(priceLabel, countryCode, currencyCode))
            .put("merchantInfo", merchantInfo())
            .put("shippingAddressRequired", true)
            .put(
                "shippingAddressParameters", JSONObject()
                    .put("phoneNumberRequired", false)
                    .put("allowedCountryCodes", JSONArray(listOf("US"))) // TODO
            )

    private fun merchantInfo(): JSONObject = JSONObject().put("merchantName", "Example Merchant")


    private fun getTransactionInfo(price: String, countryCode: String, currencyCode: String): JSONObject =
        JSONObject()
            .put("totalPrice", price)
            .put("totalPriceStatus", "ESTIMATED")
            .put("countryCode", countryCode)
            .put("currencyCode", currencyCode)

    private val baseRequest = JSONObject()
        .put("apiVersion", 2)
        .put("apiVersionMinor", 0)

    // Merchant ID, allowed card networks, and allowed auth method values taken from <your-shop.myshopify.com>/payments/config.json
    private fun gatewayTokenizationSpecification(): JSONObject {
        return JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put(
                "parameters", JSONObject(
                    mapOf(
                        "gateway" to "shopify",
                        "gatewayMerchantId" to "63357649154"
                    )
                )
            )
        }
    }

    private val allowedCardNetworks = JSONArray(listOf("VISA", "MASTERCARD", "AMEX"))
    private val allowedCardAuthMethods = JSONArray(listOf("PAN_ONLY", "CRYPTOGRAM_3DS"))

    private fun baseCardPaymentMethod(): JSONObject =
        JSONObject()
            .put("type", "CARD")
            .put(
                "parameters", JSONObject()
                    .put("allowedAuthMethods", allowedCardAuthMethods)
                    .put("allowedCardNetworks", allowedCardNetworks)
                    .put("billingAddressRequired", true)
                    .put(
                        "billingAddressParameters", JSONObject()
                            .put("format", "FULL")
                    )
            )

    private val cardPaymentMethod: JSONObject = baseCardPaymentMethod()
        .put("tokenizationSpecification", gatewayTokenizationSpecification())

}
