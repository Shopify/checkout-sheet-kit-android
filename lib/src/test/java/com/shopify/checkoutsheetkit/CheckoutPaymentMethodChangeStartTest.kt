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

import com.shopify.checkoutsheetkit.CheckoutAssertions.assertThat
import com.shopify.checkoutsheetkit.lifecycleevents.CardBrand
import com.shopify.checkoutsheetkit.lifecycleevents.Cart
import com.shopify.checkoutsheetkit.lifecycleevents.CartAddress
import com.shopify.checkoutsheetkit.lifecycleevents.CartBuyerIdentity
import com.shopify.checkoutsheetkit.lifecycleevents.CartCost
import com.shopify.checkoutsheetkit.lifecycleevents.CartCredential
import com.shopify.checkoutsheetkit.lifecycleevents.CartDelivery
import com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryGroup
import com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryGroupType
import com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryMethodType
import com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryOption
import com.shopify.checkoutsheetkit.lifecycleevents.CartLine
import com.shopify.checkoutsheetkit.lifecycleevents.CartLineCost
import com.shopify.checkoutsheetkit.lifecycleevents.CartLineMerchandise
import com.shopify.checkoutsheetkit.lifecycleevents.CartPayment
import com.shopify.checkoutsheetkit.lifecycleevents.CartPaymentInstrument
import com.shopify.checkoutsheetkit.lifecycleevents.CartPaymentMethod
import com.shopify.checkoutsheetkit.lifecycleevents.CartSelectableAddress
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutPaymentMethodChangeStartEvent
import com.shopify.checkoutsheetkit.lifecycleevents.MailingAddress
import com.shopify.checkoutsheetkit.lifecycleevents.MerchandiseImage
import com.shopify.checkoutsheetkit.lifecycleevents.Money
import com.shopify.checkoutsheetkit.lifecycleevents.RemoteTokenPaymentCredential
import com.shopify.checkoutsheetkit.lifecycleevents.ResponseError
import com.shopify.checkoutsheetkit.lifecycleevents.SelectedOption
import com.shopify.checkoutsheetkit.rpc.RPCRequestRegistry
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Suppress("LargeClass")
class CheckoutPaymentMethodChangeStartTest {

    @Test
    fun `test decode CheckoutPaymentMethodChangeStart with cart`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "test-123",
                "method": "checkout.paymentMethodChangeStart",
                "params": {
                    "cart": {
                        "id": "cart-123",
                        "lines": [],
                        "cost": {
                            "subtotalAmount": {"amount": "100.00", "currencyCode": "USD"},
                            "totalAmount": {"amount": "100.00", "currencyCode": "USD"}
                        },
                        "buyerIdentity": {},
                        "deliveryGroups": [],
                        "discountCodes": [],
                        "appliedGiftCards": [],
                        "discountAllocations": [],
                        "delivery": {"addresses": []},
                        "payment": {
                            "methods": [{
                                "instruments": [
                                    {"externalReferenceId": "instrument-1"},
                                    {"externalReferenceId": "instrument-2"}
                                ]
                            }]
                        }
                    }
                }
            }
        """.trimIndent()

        val decoded = CheckoutPaymentMethodChangeStartEvent.Companion.decodeErased(json)

        assertThat(decoded).isNotNull().isInstanceOf(CheckoutPaymentMethodChangeStartEvent::class.java)

        val request = decoded as CheckoutPaymentMethodChangeStartEvent
        assertThat(request.id).isEqualTo("test-123")
        assertThat(request.cart.id).isEqualTo("cart-123")
        assertThat(request.cart.payment?.methods).hasSize(1)
        assertThat(request.cart.payment?.methods?.get(0)?.instruments).hasSize(2)
        assertThat(request.cart.payment?.methods?.get(0)?.instruments?.get(0)?.externalReferenceId).isEqualTo("instrument-1")
        assertThat(request.cart.payment?.methods?.get(0)?.instruments?.get(1)?.externalReferenceId).isEqualTo("instrument-2")
    }

    @Test
    fun `test decode CheckoutPaymentMethodChangeStart with empty payment methods`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "test-456",
                "method": "checkout.paymentMethodChangeStart",
                "params": {
                    "cart": {
                        "id": "cart-456",
                        "lines": [],
                        "cost": {
                            "subtotalAmount": {"amount": "50.00", "currencyCode": "CAD"},
                            "totalAmount": {"amount": "50.00", "currencyCode": "CAD"}
                        },
                        "buyerIdentity": {},
                        "deliveryGroups": [],
                        "discountCodes": [],
                        "appliedGiftCards": [],
                        "discountAllocations": [],
                        "delivery": {"addresses": []},
                        "payment": {"methods": []}
                    }
                }
            }
        """.trimIndent()

        val decoded = CheckoutPaymentMethodChangeStartEvent.Companion.decodeErased(json)

        assertThat(decoded).isNotNull().isInstanceOf(CheckoutPaymentMethodChangeStartEvent::class.java)

        val request = decoded as CheckoutPaymentMethodChangeStartEvent
        assertThat(request.id).isEqualTo("test-456")
        assertThat(request.cart.id).isEqualTo("cart-456")
        assertThat(request.cart.payment?.methods).isEmpty()
    }

    @Test
    fun `test registry can decode CheckoutPaymentMethodChangeStart`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "test-789",
                "method": "checkout.paymentMethodChangeStart",
                "params": {
                    "cart": {
                        "id": "cart-789",
                        "lines": [],
                        "cost": {
                            "subtotalAmount": {"amount": "25.00", "currencyCode": "USD"},
                            "totalAmount": {"amount": "25.00", "currencyCode": "USD"}
                        },
                        "buyerIdentity": {},
                        "deliveryGroups": [],
                        "discountCodes": [],
                        "appliedGiftCards": [],
                        "discountAllocations": [],
                        "delivery": {"addresses": []},
                        "payment": {"methods": [{"instruments": [{"externalReferenceId": "pi-abc"}]}]}
                    }
                }
            }
        """.trimIndent()

        val decoded = RPCRequestRegistry.decode(json)

        assertThat(decoded).isNotNull().isInstanceOf(CheckoutPaymentMethodChangeStartEvent::class.java)

        val request = decoded as CheckoutPaymentMethodChangeStartEvent
        assertThat(request.id).isEqualTo("test-789")
        assertThat(request.cart.id).isEqualTo("cart-789")
        assertThat(request.cart.payment?.methods).hasSize(1)
        assertThat(request.cart.payment?.methods?.get(0)?.instruments?.get(0)?.externalReferenceId).isEqualTo("pi-abc")
    }

    @Test
    fun `test companion object provides correct method`() {
        assertThat(CheckoutPaymentMethodChangeStartEvent.method).isEqualTo("checkout.paymentMethodChangeStart")

        val cart = createTestCart()
        val request = CheckoutPaymentMethodChangeStartEvent(
            "test-id",
            CheckoutPaymentMethodChangeStartParams(cart),
            CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )
        assertThat(request.method).isEqualTo("checkout.paymentMethodChangeStart")
    }

    @Test
    fun `test onCheckoutPaymentMethodChangeStart method name is consistent`() {
        assertThat(CheckoutPaymentMethodChangeStartEvent.method).isEqualTo("checkout.paymentMethodChangeStart")
    }

    @Test
    fun `test toString includes id, method and cart`() {
        val cart = createTestCart()
        val request = CheckoutPaymentMethodChangeStartEvent(
            id = "test-123",
            params = CheckoutPaymentMethodChangeStartParams(cart = cart),
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )
        val result = request.toString()

        assertThat(result).contains("id='test-123'")
        assertThat(result).contains("method='checkout.paymentMethodChangeStart'")
        assertThat(result).contains("cart=Cart(")
    }

    @Test
    fun `test equals returns true for same id`() {
        val request1 = CheckoutPaymentMethodChangeStartEvent(
            id = "same-id",
            params = CheckoutPaymentMethodChangeStartParams(cart = createTestCart()),
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )
        val request2 = CheckoutPaymentMethodChangeStartEvent(
            id = "same-id",
            params = CheckoutPaymentMethodChangeStartParams(
                cart = createTestCart(id = "different-cart")
            ),
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )

        assertThat(request1).isEqualTo(request2)
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode())
    }

    @Test
    fun `test equals returns false for different id`() {
        val request1 = CheckoutPaymentMethodChangeStartEvent(
            id = "id-1",
            params = CheckoutPaymentMethodChangeStartParams(cart = createTestCart()),
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )
        val request2 = CheckoutPaymentMethodChangeStartEvent(
            id = "id-2",
            params = CheckoutPaymentMethodChangeStartParams(cart = createTestCart()),
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )

        assertThat(request1).isNotEqualTo(request2)
    }

    @Test
    fun `test respondWith payload with cart and payment`() {
        val cart = createTestCart()
        val eventData = CheckoutPaymentMethodChangeStartParams(cart = cart)
        val request = CheckoutPaymentMethodChangeStartEvent(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )

        val updatedCart = cart.copy(
            payment = CartPayment(
                methods = listOf(
                    CartPaymentMethod(
                        instruments = listOf(
                            CartPaymentInstrument(externalReferenceId = "new-instrument-123")
                        )
                    )
                )
            )
        )

        val payload = CheckoutPaymentMethodChangeStartResponsePayload(
            cart = updatedCart,
            errors = null
        )

        request.respondWith(payload)

        assertThat(request.cart.id).isEqualTo("cart-id-123")
    }

    @Test
    fun `test respondWith payload with errors`() {
        val cart = createTestCart()
        val eventData = CheckoutPaymentMethodChangeStartParams(cart = cart)
        val request = CheckoutPaymentMethodChangeStartEvent(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )

        val payload = CheckoutPaymentMethodChangeStartResponsePayload(
            cart = null,
            errors = listOf(
                ResponseError(
                    code = "INVALID_PAYMENT_METHOD",
                    message = "The selected payment method is not available",
                    fieldTarget = "payment"
                )
            )
        )

        request.respondWith(payload)

        assertThat(request.cart).isNotNull()
    }

    @Test
    fun `test respondWith null cart is valid`() {
        val cart = createTestCart()
        val eventData = CheckoutPaymentMethodChangeStartParams(cart = cart)
        val request = CheckoutPaymentMethodChangeStartEvent(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )

        val payload = CheckoutPaymentMethodChangeStartResponsePayload(
            cart = null,
            errors = null
        )

        request.respondWith(payload)

        assertThat(request.cart).isNotNull()
    }

    @Test
    fun `test exposes cart from params`() {
        val cart = createTestCart(
            id = "exposed-cart-id",
            totalAmount = "199.99"
        )
        val eventData = CheckoutPaymentMethodChangeStartParams(cart = cart)
        val request = CheckoutPaymentMethodChangeStartEvent(
            id = "request-id",
            params = eventData,
            responseSerializer = CheckoutPaymentMethodChangeStartResponsePayload.serializer()
        )

        assertThat(request.cart.id).isEqualTo("exposed-cart-id")
        assertThat(request.cart.cost.totalAmount.amount).isEqualTo("199.99")
    }

    @Test
    fun `test CardBrand enum values`() {
        val expectedValues = listOf(
            CardBrand.VISA to "VISA",
            CardBrand.MASTERCARD to "MASTERCARD",
            CardBrand.AMERICAN_EXPRESS to "AMERICAN_EXPRESS",
            CardBrand.DISCOVER to "DISCOVER",
            CardBrand.DINERS_CLUB to "DINERS_CLUB",
            CardBrand.JCB to "JCB",
            CardBrand.MAESTRO to "MAESTRO",
            CardBrand.UNKNOWN to "UNKNOWN"
        )

        expectedValues.forEach { (brand, expectedName) ->
            assertThat(brand.name).isEqualTo(expectedName)
        }
    }

    @Test
    fun `test CartCredential deserialization with remoteTokenPaymentCredential`() {
        val json = """
            {
                "remoteTokenPaymentCredential": {
                    "token": "tok_abc123",
                    "tokenType": "merchant.token",
                    "tokenHandler": "merchant_psp"
                }
            }
        """.trimIndent()

        val credential = Json.decodeFromString<CartCredential>(json)

        assertThat(credential.remoteTokenPaymentCredential).isNotNull()
        assertThat(credential.remoteTokenPaymentCredential?.token).isEqualTo("tok_abc123")
        assertThat(credential.remoteTokenPaymentCredential?.tokenType).isEqualTo("merchant.token")
        assertThat(credential.remoteTokenPaymentCredential?.tokenHandler).isEqualTo("merchant_psp")
    }

    @Test
    fun `test CartPaymentInstrument deserialization with credentials`() {
        val json = """
            {
                "externalReferenceId": "instrument-123",
                "credentials": [{
                    "remoteTokenPaymentCredential": {
                        "token": "tok_abc123",
                        "tokenType": "merchant.token",
                        "tokenHandler": "merchant_psp"
                    }
                }]
            }
        """.trimIndent()

        val instrument = Json.decodeFromString<CartPaymentInstrument>(json)

        assertThat(instrument.externalReferenceId).isEqualTo("instrument-123")
        assertThat(instrument.credentials).hasSize(1)
        assertThat(instrument.credentials?.get(0)?.remoteTokenPaymentCredential?.token).isEqualTo("tok_abc123")
    }

    @Test
    fun `test CardBrand deserialization values`() {
        val testCases = listOf(
            "VISA" to CardBrand.VISA,
            "MASTERCARD" to CardBrand.MASTERCARD,
            "AMERICAN_EXPRESS" to CardBrand.AMERICAN_EXPRESS,
            "DISCOVER" to CardBrand.DISCOVER,
            "DINERS_CLUB" to CardBrand.DINERS_CLUB,
            "JCB" to CardBrand.JCB,
            "MAESTRO" to CardBrand.MAESTRO,
            "UNKNOWN" to CardBrand.UNKNOWN
        )

        testCases.forEach { (jsonValue, expectedBrand) ->
            assertThat(Json.decodeFromString<CardBrand>("\"$jsonValue\""))
                .describedAs("CardBrand deserialization for $jsonValue")
                .isEqualTo(expectedBrand)
        }
    }

    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    private val testJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    @Test
    fun `test serializing CartPaymentInstrument includes all credit card display fields`() {
        val instrument = CartPaymentInstrument(
            externalReferenceId = "card-mc-5555",
            cardHolderName = "John Smith",
            lastDigits = "5555",
            month = 6,
            year = 2027,
            brand = CardBrand.MASTERCARD,
            billingAddress = MailingAddress(
                address1 = "123 Main St",
                address2 = "New York",
                city = "John",
                province = "Smith",
                country = "US",
                zip = "NY",
                firstName = "10001"
            )
        )

        val json = testJson.encodeToString(CartPaymentInstrument.serializer(), instrument)

        val expected = """
            {
              "__typename": "CreditCardPaymentInstrument",
              "externalReferenceId": "card-mc-5555",
              "cardHolderName": "John Smith",
              "lastDigits": "5555",
              "month": 6,
              "year": 2027,
              "brand": "MASTERCARD",
              "billingAddress": {
                "address1": "123 Main St",
                "address2": "New York",
                "city": "John",
                "province": "Smith",
                "country": "US",
                "zip": "NY",
                "firstName": "10001"
              }
            }
        """.trimIndent()

        assertThat(Json.parseToJsonElement(json))
            .isEqualTo(Json.parseToJsonElement(expected))
    }

    @Test
    fun `test serializing full Cart for paymentMethodChangeStart matches expected JSON format`() {
        val cart = createFullTestCartForPaymentMethodChange()
        val json = testJson.encodeToString(Cart.serializer(), cart)
        val expected = getExpectedPaymentMethodChangeCartJson()

        assertThat(Json.parseToJsonElement(json))
            .isEqualTo(Json.parseToJsonElement(expected))
    }

    @Suppress("LongMethod")
    private fun createFullTestCartForPaymentMethodChange() = Cart(
        id = "hWN6LirFdNUjAcpXvpm52A1T",
        lines = listOf(
            CartLine(
                id = "ed22744d9f67fb682fa63510629c1f44",
                quantity = 1,
                merchandise = CartLineMerchandise(
                    id = "gid://shopify/ProductVariantMerchandise/63449294405654",
                    title = "Gustave table lamp",
                    product = CartLineMerchandise.Product(
                        id = "gid://shopify/Product/14919569440790",
                        title = "Gustave table lamp"
                    ),
                    image = MerchandiseImage(
                        url = "https://cdn.shopify.com/s/files/1/0987/0986/4470/files/gustave_table_lamp.png?v=1761595805"
                    ),
                    selectedOptions = listOf(
                        SelectedOption(name = "Lens color", value = "Black")
                    )
                ),
                cost = CartLineCost(
                    totalAmount = Money(amount = "50.00", currencyCode = "USD"),
                    subtotalAmount = Money(amount = "50.00", currencyCode = "USD"),
                    amountPerQuantity = Money(amount = "50.00", currencyCode = "USD")
                ),
                discountAllocations = emptyList()
            )
        ),
        cost = CartCost(
            subtotalAmount = Money(amount = "50.00", currencyCode = "USD"),
            totalAmount = Money(amount = "50.00", currencyCode = "USD")
        ),
        buyerIdentity = CartBuyerIdentity(
            countryCode = "US",
            email = "checkout-kit@shopify.com"
        ),
        deliveryGroups = listOf(
            CartDeliveryGroup(
                selectedDeliveryOption = CartDeliveryOption(
                    title = "Economy",
                    code = "Economy",
                    description = "",
                    estimatedCost = Money(amount = "0.00", currencyCode = "USD"),
                    handle = "05ac113615eb8c229a25856a76f7dd90-8388085074acab7e91de633521be86f0",
                    deliveryMethodType = CartDeliveryMethodType.SHIPPING
                ),
                groupType = CartDeliveryGroupType.ONE_TIME_PURCHASE,
                deliveryOptions = listOf(
                    CartDeliveryOption(
                        title = "Economy",
                        code = "Economy",
                        description = "",
                        estimatedCost = Money(amount = "0.00", currencyCode = "USD"),
                        handle = "05ac113615eb8c229a25856a76f7dd90-8388085074acab7e91de633521be86f0",
                        deliveryMethodType = CartDeliveryMethodType.SHIPPING
                    ),
                    CartDeliveryOption(
                        title = "Standard",
                        code = "Standard",
                        description = "",
                        estimatedCost = Money(amount = "6.90", currencyCode = "USD"),
                        handle = "05ac113615eb8c229a25856a76f7dd90-6d5a64f58240381019fc074473bab3ab",
                        deliveryMethodType = CartDeliveryMethodType.SHIPPING
                    )
                ),
                deliveryAddress = MailingAddress(
                    phone = "+441792547555",
                    province = "CA",
                    address2 = "Haight-Ashbury",
                    country = "US",
                    address1 = "89 Haight Street",
                    firstName = "Evelyn",
                    lastName = "Hartley",
                    countryCodeV2 = "US",
                    city = "San Francisco",
                    zip = "94117"
                )
            )
        ),
        discountCodes = emptyList(),
        appliedGiftCards = emptyList(),
        discountAllocations = emptyList(),
        delivery = CartDelivery(
            addresses = listOf(
                CartSelectableAddress(
                    address = CartAddress.DeliveryAddress(
                        phone = "+441792547555",
                        zip = "94117",
                        city = "San Francisco",
                        firstName = "Evelyn",
                        provinceCode = "CA",
                        address2 = "Haight-Ashbury",
                        lastName = "Hartley",
                        address1 = "89 Haight Street",
                        countryCode = "US"
                    ),
                    oneTimeUse = false,
                    selected = true
                )
            )
        ),
        payment = CartPayment(
            methods = listOf(
                CartPaymentMethod(
                    instruments = listOf(
                        CartPaymentInstrument(
                            year = 2027,
                            externalReferenceId = "card-mc-5555",
                            month = 6,
                            brand = CardBrand.MASTERCARD,
                            billingAddress = MailingAddress(
                                country = "US",
                                address2 = "New York",
                                province = "Smith",
                                address1 = "123 Main St",
                                city = "John",
                                firstName = "10001",
                                zip = "NY"
                            ),
                            lastDigits = "5555",
                            cardHolderName = "John Smith"
                        )
                    )
                )
            )
        )
    )

    @Suppress("LongMethod")
    private fun getExpectedPaymentMethodChangeCartJson() = """
            {
              "id": "hWN6LirFdNUjAcpXvpm52A1T",
              "lines": [
                {
                  "id": "ed22744d9f67fb682fa63510629c1f44",
                  "quantity": 1,
                  "merchandise": {
                    "id": "gid://shopify/ProductVariantMerchandise/63449294405654",
                    "title": "Gustave table lamp",
                    "product": {
                      "id": "gid://shopify/Product/14919569440790",
                      "title": "Gustave table lamp"
                    },
                    "image": {
                      "url": "https://cdn.shopify.com/s/files/1/0987/0986/4470/files/gustave_table_lamp.png?v=1761595805"
                    },
                    "selectedOptions": [{ "name": "Lens color", "value": "Black" }]
                  },
                  "cost": {
                    "amountPerQuantity": { "amount": "50.00", "currencyCode": "USD" },
                    "subtotalAmount": { "amount": "50.00", "currencyCode": "USD" },
                    "totalAmount": { "amount": "50.00", "currencyCode": "USD" }
                  },
                  "discountAllocations": []
                }
              ],
              "cost": {
                "subtotalAmount": { "amount": "50.00", "currencyCode": "USD" },
                "totalAmount": { "amount": "50.00", "currencyCode": "USD" }
              },
              "buyerIdentity": {
                "email": "checkout-kit@shopify.com",
                "countryCode": "US"
              },
              "deliveryGroups": [
                {
                  "deliveryAddress": {
                    "address1": "89 Haight Street",
                    "address2": "Haight-Ashbury",
                    "city": "San Francisco",
                    "province": "CA",
                    "country": "US",
                    "countryCodeV2": "US",
                    "zip": "94117",
                    "firstName": "Evelyn",
                    "lastName": "Hartley",
                    "phone": "+441792547555"
                  },
                  "deliveryOptions": [
                    {
                      "code": "Economy",
                      "title": "Economy",
                      "description": "",
                      "handle": "05ac113615eb8c229a25856a76f7dd90-8388085074acab7e91de633521be86f0",
                      "estimatedCost": { "amount": "0.00", "currencyCode": "USD" },
                      "deliveryMethodType": "SHIPPING"
                    },
                    {
                      "code": "Standard",
                      "title": "Standard",
                      "description": "",
                      "handle": "05ac113615eb8c229a25856a76f7dd90-6d5a64f58240381019fc074473bab3ab",
                      "estimatedCost": { "amount": "6.90", "currencyCode": "USD" },
                      "deliveryMethodType": "SHIPPING"
                    }
                  ],
                  "selectedDeliveryOption": {
                    "code": "Economy",
                    "title": "Economy",
                    "description": "",
                    "handle": "05ac113615eb8c229a25856a76f7dd90-8388085074acab7e91de633521be86f0",
                    "estimatedCost": { "amount": "0.00", "currencyCode": "USD" },
                    "deliveryMethodType": "SHIPPING"
                  },
                  "groupType": "ONE_TIME_PURCHASE"
                }
              ],
              "discountCodes": [],
              "appliedGiftCards": [],
              "discountAllocations": [],
              "delivery": {
                "addresses": [
                  {
                    "address": {
                      "address1": "89 Haight Street",
                      "address2": "Haight-Ashbury",
                      "city": "San Francisco",
                      "countryCode": "US",
                      "firstName": "Evelyn",
                      "lastName": "Hartley",
                      "phone": "+441792547555",
                      "provinceCode": "CA",
                      "zip": "94117"
                    },
                    "selected": true,
                    "oneTimeUse": false
                  }
                ]
              },
              "payment": {
                "methods": [
                  {
                    "__typename": "CreditCardPaymentMethod",
                    "instruments": [
                      {
                        "__typename": "CreditCardPaymentInstrument",
                        "externalReferenceId": "card-mc-5555",
                        "cardHolderName": "John Smith",
                        "lastDigits": "5555",
                        "month": 6,
                        "year": 2027,
                        "brand": "MASTERCARD",
                        "billingAddress": {
                          "address1": "123 Main St",
                          "address2": "New York",
                          "city": "John",
                          "province": "Smith",
                          "country": "US",
                          "zip": "NY",
                          "firstName": "10001"
                        }
                      }
                    ]
                  }
                ]
              }
            }
        """.trimIndent()

}
