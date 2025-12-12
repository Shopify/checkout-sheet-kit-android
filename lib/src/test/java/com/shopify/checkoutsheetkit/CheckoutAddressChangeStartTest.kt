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

import com.shopify.checkoutsheetkit.lifecycleevents.Cart
import com.shopify.checkoutsheetkit.lifecycleevents.CartAddress
import com.shopify.checkoutsheetkit.lifecycleevents.CartBuyerIdentity
import com.shopify.checkoutsheetkit.lifecycleevents.CartCost
import com.shopify.checkoutsheetkit.lifecycleevents.CartDelivery
import com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryGroup
import com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryGroupType
import com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryMethodType
import com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryOption
import com.shopify.checkoutsheetkit.lifecycleevents.CartLine
import com.shopify.checkoutsheetkit.lifecycleevents.CartLineCost
import com.shopify.checkoutsheetkit.lifecycleevents.CartLineMerchandise
import com.shopify.checkoutsheetkit.lifecycleevents.CartPayment
import com.shopify.checkoutsheetkit.lifecycleevents.CartSelectableAddress
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutAddressChangeStartResponsePayload
import com.shopify.checkoutsheetkit.lifecycleevents.MailingAddress
import com.shopify.checkoutsheetkit.lifecycleevents.MerchandiseImage
import com.shopify.checkoutsheetkit.lifecycleevents.Money
import com.shopify.checkoutsheetkit.lifecycleevents.SelectedOption
import com.shopify.checkoutsheetkit.CheckoutAssertions.assertThat
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutEventResponseException
import com.shopify.checkoutsheetkit.rpc.RPCRequestRegistry
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutAddressChangeStartEvent
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutAddressChangeStartParams
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CheckoutAddressChangeStartTest {

    @Test
    fun `test decode CheckoutAddressChangeStart from JSON`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "test-123",
                "method": "checkout.addressChangeStart",
                "params": {
                    "addressType": "shipping",
                    "cart": {
                        "id": "gid://shopify/Cart/test-cart-123",
                        "lines": [],
                        "cost": {
                            "subtotalAmount": {"amount": "100.00", "currencyCode": "USD"},
                            "totalAmount": {"amount": "100.00", "currencyCode": "USD"}
                        },
                        "buyerIdentity": {
                            "email": "test@example.com"
                        },
                        "deliveryGroups": [],
                        "discountCodes": [],
                        "appliedGiftCards": [],
                        "discountAllocations": [],
                        "delivery": {"addresses": []},
                        "payment": {"instruments": []}
                    }
                }
            }
        """.trimIndent()

        val decoded = CheckoutAddressChangeStartEvent.Companion.decodeErased(json)

        assertThat(decoded).isNotNull()
        assertThat(decoded).isInstanceOf(CheckoutAddressChangeStartEvent::class.java)

        val request = decoded as CheckoutAddressChangeStartEvent
        assertThat(request.id).isEqualTo("test-123")
        assertThat(request.addressType).isEqualTo("shipping")
        assertThat(request.cart.id).isEqualTo("gid://shopify/Cart/test-cart-123")
    }

    @Test
    fun `test decode CheckoutAddressChangeStart for billing address`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "test-456",
                "method": "checkout.addressChangeStart",
                "params": {
                    "addressType": "billing",
                    "cart": {
                        "id": "gid://shopify/Cart/test-cart-456",
                        "lines": [],
                        "cost": {
                            "subtotalAmount": {"amount": "50.00", "currencyCode": "CAD"},
                            "totalAmount": {"amount": "50.00", "currencyCode": "CAD"}
                        },
                        "buyerIdentity": {
                            "email": "billing@example.com"
                        },
                        "deliveryGroups": [],
                        "discountCodes": [],
                        "appliedGiftCards": [],
                        "discountAllocations": [],
                        "delivery": {"addresses": []},
                        "payment": {"instruments": []}
                    }
                }
            }
        """.trimIndent()

        val decoded = CheckoutAddressChangeStartEvent.Companion.decodeErased(json)

        assertThat(decoded).isNotNull()
        assertThat(decoded).isInstanceOf(CheckoutAddressChangeStartEvent::class.java)

        val request = decoded as CheckoutAddressChangeStartEvent
        assertThat(request.id).isEqualTo("test-456")
        assertThat(request.addressType).isEqualTo("billing")
        assertThat(request.cart.id).isEqualTo("gid://shopify/Cart/test-cart-456")
    }

    @Test
    fun `test registry can decode CheckoutAddressChangeStart`() {
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "test-789",
                "method": "checkout.addressChangeStart",
                "params": {
                    "addressType": "shipping",
                    "cart": {
                        "id": "gid://shopify/Cart/test-cart-789",
                        "lines": [],
                        "cost": {
                            "subtotalAmount": {"amount": "100.00", "currencyCode": "USD"},
                            "totalAmount": {"amount": "100.00", "currencyCode": "USD"}
                        },
                        "buyerIdentity": {
                            "email": "test@example.com"
                        },
                        "deliveryGroups": [],
                        "discountCodes": [],
                        "appliedGiftCards": [],
                        "discountAllocations": [],
                        "delivery": {"addresses": []},
                        "payment": {"instruments": []}
                    }
                }
            }
        """.trimIndent()

        val decoded = RPCRequestRegistry.decode(json)

        assertThat(decoded).isNotNull()
        assertThat(decoded).isInstanceOf(CheckoutAddressChangeStartEvent::class.java)

        val request = decoded as CheckoutAddressChangeStartEvent
        assertThat(request.id).isEqualTo("test-789")
        assertThat(request.addressType).isEqualTo("shipping")
    }

    @Test
    fun `test companion object provides correct method`() {
        assertThat(CheckoutAddressChangeStartEvent.method).isEqualTo("checkout.addressChangeStart")
    }

    @Test
    fun `test respondWith payload`() {
        val cart = createTestCart()
        val eventData = CheckoutAddressChangeStartParams(
            addressType = "shipping",
            cart = cart
        )
        val request = CheckoutAddressChangeStartEvent(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutAddressChangeStartResponsePayload.serializer()
        )

        val updatedCart = cart.copy(
            delivery = CartDelivery(
                addresses = listOf(
                    CartSelectableAddress(
                        address = CartAddress.DeliveryAddress(
                            firstName = "Ada",
                            lastName = "Lovelace",
                            countryCode = "US"
                        )
                    )
                )
            )
        )
        val payload = CheckoutAddressChangeStartResponsePayload(
            cart = updatedCart
        )

        // This will fail to send since no WebView is attached, but we're testing the flow
        request.respondWith(payload)

        assertThat(request.addressType).isEqualTo("shipping")
        assertThat(request.cart.id).isEqualTo(cart.id)
    }

    @Test
    fun `test respondWith JSON string`() {
        val json = """
            {
                "cart": {
                    "id": "gid://shopify/Cart/test",
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
                    "delivery": {
                        "addresses": [
                            {
                                "address": {
                                    "firstName": "Ada",
                                    "lastName": "Lovelace",
                                    "countryCode": "US"
                                }
                            }
                        ]
                    }
                }
            }
        """.trimIndent()

        val cart = createTestCart()
        val eventData = CheckoutAddressChangeStartParams(
            addressType = "shipping",
            cart = cart
        )
        val request = CheckoutAddressChangeStartEvent(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutAddressChangeStartResponsePayload.serializer()
        )

        // This will fail to send since no WebView is attached, but we're testing the parsing
        request.respondWith(json)

        assertThat(request.addressType).isEqualTo("shipping")
    }

    @Test
    fun `test multiple respondWith calls are ignored`() {
        val cart = createTestCart()
        val eventData = CheckoutAddressChangeStartParams(
            addressType = "shipping",
            cart = cart
        )
        val request = CheckoutAddressChangeStartEvent(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutAddressChangeStartResponsePayload.serializer()
        )

        val payload1 = CheckoutAddressChangeStartResponsePayload(
            cart = cart.copy(
                delivery = CartDelivery(
                    addresses = listOf(
                        CartSelectableAddress(
                            address = CartAddress.DeliveryAddress(
                                firstName = "First",
                                lastName = "Response",
                                countryCode = "US"
                            )
                        )
                    )
                )
            )
        )

        val payload2 = CheckoutAddressChangeStartResponsePayload(
            cart = cart.copy(
                delivery = CartDelivery(
                    addresses = listOf(
                        CartSelectableAddress(
                            address = CartAddress.DeliveryAddress(
                                firstName = "Second",
                                lastName = "Response",
                                countryCode = "CA"
                            )
                        )
                    )
                )
            )
        )

        // First call should succeed (no WebView, so won't actually send, but won't throw)
        assertThatCode { request.respondWith(payload1) }
            .doesNotThrowAnyException()

        // Second call should be ignored (logged but not throw)
        assertThatCode { request.respondWith(payload2) }
            .doesNotThrowAnyException()
    }

    @Test
    fun `test exposes cart from params`() {
        val cart = createTestCart(
            id = "gid://shopify/Cart/test-cart",
            subtotalAmount = "100.00",
            totalAmount = "100.00"
        )
        val eventData = CheckoutAddressChangeStartParams(
            addressType = "billing",
            cart = cart
        )
        val request = CheckoutAddressChangeStartEvent(
            id = "test-id",
            params = eventData,
            responseSerializer = CheckoutAddressChangeStartResponsePayload.serializer()
        )

        assertThat(request.cart).isEqualTo(cart)
        assertThat(request.cart.id).isEqualTo("gid://shopify/Cart/test-cart")
    }

    @Test
    fun `respondWith JSON string throws DecodingFailed on invalid JSON`() {
        val request = createTestRequest()
        val invalidJson = "{ this is not valid JSON }"

        assertThatThrownBy { request.respondWith(invalidJson) }
            .isInstanceOf(CheckoutEventResponseException.DecodingFailed::class.java)
            .hasMessageContaining("Failed to parse JSON")
            .hasCauseInstanceOf(Exception::class.java)
    }

    @Test
    fun `respondWith JSON string throws DecodingFailed on type mismatch`() {
        val request = createTestRequest()
        val typeMismatchJson = """
            {
                "cart": "this should be an object, not a string"
            }
        """.trimIndent()

        assertThatThrownBy { request.respondWith(typeMismatchJson) }
            .isInstanceOf(CheckoutEventResponseException.DecodingFailed::class.java)
            .hasMessageContaining("Failed to decode response")
            .hasCauseInstanceOf(Exception::class.java)
    }

    @Test
    fun `test toString includes id, method, addressType and cart`() {
        val request = createTestRequest()
        val result = request.toString()

        assertThat(result).contains("id='test-id'")
        assertThat(result).contains("method='checkout.addressChangeStart'")
        assertThat(result).contains("addressType='shipping'")
        assertThat(result).contains("cart=Cart(")
    }

    @Test
    fun `test equals returns true for same id`() {
        val request1 = CheckoutAddressChangeStartEvent(
            id = "same-id",
            params = CheckoutAddressChangeStartParams(
                addressType = "shipping",
                cart = createTestCart()
            ),
            responseSerializer = CheckoutAddressChangeStartResponsePayload.serializer()
        )
        val request2 = CheckoutAddressChangeStartEvent(
            id = "same-id",
            params = CheckoutAddressChangeStartParams(
                addressType = "billing", // Different addressType
                cart = createTestCart(id = "different-cart") // Different cart
            ),
            responseSerializer = CheckoutAddressChangeStartResponsePayload.serializer()
        )

        assertThat(request1).isEqualTo(request2)
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode())
    }

    @Test
    fun `test equals returns false for different id`() {
        val request1 = createTestRequest()
        val request2 = CheckoutAddressChangeStartEvent(
            id = "different-id",
            params = CheckoutAddressChangeStartParams(
                addressType = "shipping",
                cart = createTestCart()
            ),
            responseSerializer = CheckoutAddressChangeStartResponsePayload.serializer()
        )

        assertThat(request1).isNotEqualTo(request2)
    }

    private fun createTestRequest() = CheckoutAddressChangeStartEvent(
        id = "test-id",
        params = CheckoutAddressChangeStartParams(
            addressType = "shipping",
            cart = createTestCart(
                id = "gid://shopify/Cart/test-cart",
                subtotalAmount = "100.00",
                totalAmount = "100.00"
            )
        ),
        responseSerializer = CheckoutAddressChangeStartResponsePayload.serializer()
    )

    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    private val testJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    @Test
    fun `test serializing CartSelectableAddress includes selected and oneTimeUse fields`() {
        val selectableAddress = CartSelectableAddress(
            address = CartAddress.DeliveryAddress(
                city = "San Francisco",
                address1 = "89 Haight Street",
                provinceCode = "CA",
                firstName = "Evelyn",
                address2 = "Haight-Ashbury",
                phone = "+441792547555",
                lastName = "Hartley",
                zip = "94117",
                countryCode = "US"
            ),
            selected = true,
            oneTimeUse = false
        )

        val json = testJson.encodeToString(CartSelectableAddress.serializer(), selectableAddress)

        val expected = """
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
        """.trimIndent()

        assertThat(Json.parseToJsonElement(json))
            .isEqualTo(Json.parseToJsonElement(expected))
    }

    @Test
    fun `test serializing full Cart for addressChangeStart matches expected JSON format`() {
        val cart = Cart(
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
                        handle = "05ac113615eb8c229a25856a76f7dd90-8388085074acab7e91de633521be86f0",
                        estimatedCost = Money(amount = "0.00", currencyCode = "USD"),
                        deliveryMethodType = CartDeliveryMethodType.SHIPPING,
                        description = "",
                        code = "Economy"
                    ),
                    groupType = CartDeliveryGroupType.ONE_TIME_PURCHASE,
                    deliveryAddress = MailingAddress(
                        address1 = "224 Triplett St",
                        province = "NC",
                        country = "US",
                        zip = "28642",
                        firstName = "Kieran",
                        lastName = "Osgood",
                        phone = "1-888-746-7439",
                        city = "Jonesville",
                        countryCodeV2 = "US"
                    ),
                    deliveryOptions = listOf(
                        CartDeliveryOption(
                            description = "",
                            deliveryMethodType = CartDeliveryMethodType.SHIPPING,
                            title = "Economy",
                            handle = "05ac113615eb8c229a25856a76f7dd90-8388085074acab7e91de633521be86f0",
                            code = "Economy",
                            estimatedCost = Money(amount = "0.00", currencyCode = "USD")
                        ),
                        CartDeliveryOption(
                            deliveryMethodType = CartDeliveryMethodType.SHIPPING,
                            code = "Standard",
                            description = "",
                            title = "Standard",
                            handle = "05ac113615eb8c229a25856a76f7dd90-6d5a64f58240381019fc074473bab3ab",
                            estimatedCost = Money(amount = "6.90", currencyCode = "USD")
                        )
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
                            city = "San Francisco",
                            address1 = "89 Haight Street",
                            provinceCode = "CA",
                            firstName = "Evelyn",
                            address2 = "Haight-Ashbury",
                            phone = "+441792547555",
                            lastName = "Hartley",
                            zip = "94117",
                            countryCode = "US"
                        ),
                        selected = true,
                        oneTimeUse = false
                    )
                )
            ),
            payment = CartPayment(methods = emptyList())
        )

        val json = testJson.encodeToString(Cart.serializer(), cart)

        val expected = """
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
                    "address1": "224 Triplett St",
                    "city": "Jonesville",
                    "province": "NC",
                    "country": "US",
                    "countryCodeV2": "US",
                    "zip": "28642",
                    "firstName": "Kieran",
                    "lastName": "Osgood",
                    "phone": "1-888-746-7439"
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
              "payment": { "methods": [] }
            }
        """.trimIndent()

        assertThat(Json.parseToJsonElement(json))
            .isEqualTo(Json.parseToJsonElement(expected))
    }

}
