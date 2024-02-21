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

import com.shopify.checkoutsheetkit.completedevent.Address
import com.shopify.checkoutsheetkit.completedevent.CartLine
import com.shopify.checkoutsheetkit.completedevent.CartLineImage
import com.shopify.checkoutsheetkit.completedevent.CheckoutCompletedEventDecoder
import com.shopify.checkoutsheetkit.completedevent.DeliveryDetails
import com.shopify.checkoutsheetkit.completedevent.DeliveryInfo
import com.shopify.checkoutsheetkit.completedevent.PaymentMethod
import com.shopify.checkoutsheetkit.completedevent.Price
import com.shopify.checkoutsheetkit.pixelevents.MoneyV2
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CheckoutCompletedEventDecoderTest {

    private val mockLogWrapper = mock<LogWrapper>()

    private val decoder = CheckoutCompletedEventDecoder(
        decoder = Json { ignoreUnknownKeys = true},
        log = mockLogWrapper
    )

    @Test
    fun `should decode completion event order id`() {
        val result = decoder.decode(EXAMPLE_EVENT.toWebToSdkEvent())
        val orderDetails = result.orderDetails

        assertThat(orderDetails?.id).isEqualTo("gid://shopify/OrderIdentity/9697125302294")
    }

    @Test
    fun `should decode completion event order cart lines`() {
        val result = decoder.decode(EXAMPLE_EVENT.toWebToSdkEvent())
        val orderDetails = result.orderDetails

        assertThat(orderDetails?.cart?.lines).isEqualTo(
            listOf(
                CartLine(
                    image = CartLineImage(
                        sm = "https://cdn.shopify.com/s/files/1/0692/3996/3670/files/41bc5767-d56f-432c-ac5f-6b9eeee3ba0e.truncated...",
                        md = "https://cdn.shopify.com/s/files/1/0692/3996/3670/files/41bc5767-d56f-432c-ac5f-6b9eeee3ba0e.truncated...",
                        lg = "https://cdn.shopify.com/s/files/1/0692/3996/3670/files/41bc5767-d56f-432c-ac5f-6b9eeee3ba0e.truncated...",
                        altText = null,
                    ),
                    quantity = 1,
                    title = "The Box: How the Shipping Container Made the World Smaller and the World Economy Bigger",
                    price = MoneyV2(
                        amount = 8.0,
                        currencyCode = "GBP",
                    ),
                    merchandiseId = "gid://shopify/ProductVariant/43835075002390",
                    productId = "gid://shopify/Product/8013997834262"
                )
            )
        )
    }

    @Test
    fun `should decode completion event order price`() {
        val result = decoder.decode(EXAMPLE_EVENT.toWebToSdkEvent())
        val orderDetails = result.orderDetails

        assertThat(orderDetails?.cart?.price).isEqualTo(
            Price(
                total = MoneyV2(
                    amount = 13.99,
                    currencyCode = "GBP",
                ),
                subtotal = MoneyV2(
                    amount = 8.0,
                    currencyCode = "GBP",
                ),
                taxes = MoneyV2(
                    amount = 0.0,
                    currencyCode = "GBP",
                ),
                shipping = MoneyV2(
                    amount = 5.99,
                    currencyCode = "GBP",
                ),
                discounts = emptyList(),
            )
        )
    }

    @Test
    fun `should decode completion event email`() {
        val result = decoder.decode(EXAMPLE_EVENT.toWebToSdkEvent())
        val orderDetails = result.orderDetails

        assertThat(orderDetails?.email).isEqualTo("a.user@shopify.com")
    }

    @Test
    fun `should decode completion event billing address`() {
        val result = decoder.decode(EXAMPLE_EVENT.toWebToSdkEvent())
        val orderDetails = result.orderDetails

        assertThat(orderDetails?.billingAddress).isEqualTo(
            Address(
                city = "Swansea",
                countryCode = "GB",
                postalCode = "SA1 1AB",
                address1 = "100 Street Avenue",
                firstName = "Andrew",
                lastName = "Person",
                zoneCode = "WLS",
                phone = "+447915123456",
            )
        )
    }

    @Test
    fun `should decode completion event payment methods`() {
        val result = decoder.decode(EXAMPLE_EVENT.toWebToSdkEvent())
        val orderDetails = result.orderDetails

        assertThat(orderDetails?.paymentMethods).isEqualTo(
            listOf(
                PaymentMethod(
                    type = "wallet",
                    details = mapOf(
                        "amount" to "13.99",
                        "currency" to "GBP",
                        "name" to "SHOP_PAY",
                    )
                )
            )
        )
    }

    @Test
    fun `should decode completion event deliveries`() {
        val result = decoder.decode(EXAMPLE_EVENT.toWebToSdkEvent())
        val orderDetails = result.orderDetails

        assertThat(orderDetails?.deliveries).isEqualTo(
            listOf(
                DeliveryInfo(
                    method = "SHIPPING",
                    details = DeliveryDetails(
                        location = Address(
                            city = "Swansea",
                            countryCode = "GB",
                            postalCode = "SA1 1AB",
                            address1 = "100 Street Avenue",
                            name = "Andrew",
                            firstName = "Andrew",
                            lastName = "Person",
                            zoneCode = "WLS",
                            phone = "+447915123456",
                        )
                    )
                )
            )
        )
    }

    private fun String.toWebToSdkEvent(): WebToSdkEvent {
        return WebToSdkEvent(
            name = "completed",
            body = this,
        )
    }

    companion object {
        private val EXAMPLE_EVENT = """
            {
              "flowType": "regular",
              "orderDetails": {
                "id": "gid://shopify/OrderIdentity/9697125302294",
                "cart": {
                  "token": "123",
                  "lines": [
                    {
                      "image": {
                        "sm": "https://cdn.shopify.com/s/files/1/0692/3996/3670/files/41bc5767-d56f-432c-ac5f-6b9eeee3ba0e.truncated...",
                        "md": "https://cdn.shopify.com/s/files/1/0692/3996/3670/files/41bc5767-d56f-432c-ac5f-6b9eeee3ba0e.truncated...",
                        "lg": "https://cdn.shopify.com/s/files/1/0692/3996/3670/files/41bc5767-d56f-432c-ac5f-6b9eeee3ba0e.truncated..."
                      },
                      "quantity": 1,
                      "title": "The Box: How the Shipping Container Made the World Smaller and the World Economy Bigger",
                      "price": {
                        "amount": 8,
                        "currencyCode": "GBP"
                      },
                      "merchandiseId": "gid://shopify/ProductVariant/43835075002390",
                      "productId": "gid://shopify/Product/8013997834262"
                    }
                  ],
                  "price": {
                    "total": {
                      "amount": 13.99,
                      "currencyCode": "GBP"
                    },
                    "subtotal": {
                      "amount": 8,
                      "currencyCode": "GBP"
                    },
                    "taxes": {
                      "amount": 0,
                      "currencyCode": "GBP"
                    },
                    "shipping": {
                      "amount": 5.99,
                      "currencyCode": "GBP"
                    }
                  }
                },
                "email": "a.user@shopify.com",
                "shippingAddress": {
                  "city": "Swansea",
                  "countryCode": "GB",
                  "postalCode": "SA1 1AB",
                  "address1": "100 Street Avenue",
                  "firstName": "Andrew",
                  "lastName": "Person",
                  "name": "Andrew",
                  "zoneCode": "WLS",
                  "phone": "+447915123456",
                  "coordinates": {
                    "latitude": 54.5936785,
                    "longitude": -3.013167399999999
                  }
                },
                "billingAddress": {
                  "city": "Swansea",
                  "countryCode": "GB",
                  "postalCode": "SA1 1AB",
                  "address1": "100 Street Avenue",
                  "firstName": "Andrew",
                  "lastName": "Person",
                  "zoneCode": "WLS",
                  "phone": "+447915123456"
                },
                "paymentMethods": [
                  {
                    "type": "wallet",
                    "details": {
                      "amount": "13.99",
                      "currency": "GBP",
                      "name": "SHOP_PAY"
                    }
                  }
                ],
                "deliveries": [
                  {
                    "method": "SHIPPING",
                    "details": {
                      "location": {
                        "city": "Swansea",
                        "countryCode": "GB",
                        "postalCode": "SA1 1AB",
                        "address1": "100 Street Avenue",
                        "firstName": "Andrew",
                        "lastName": "Person",
                        "name": "Andrew",
                        "zoneCode": "WLS",
                        "phone": "+447915123456",
                        "coordinates": {
                          "latitude": 54.5936785,
                          "longitude": -3.013167399999999
                        }
                      }
                    }
                  }
                ]
              },
              "orderId": "gid://shopify/OrderIdentity/9697125302294",
              "cart": {
                "lines": [
                  {
                    "image": {
                      "sm": "https://cdn.shopify.com/s/files/1/0692/3996/3670/files/41bc5767-d56f-432c-ac5f-6b9eeee3ba0e.__CR0_0_300_300_PT0_SX300_V1_64x64.jpg?v=1689689735",
                      "md": "https://cdn.shopify.com/s/files/1/0692/3996/3670/files/41bc5767-d56f-432c-ac5f-6b9eeee3ba0e.__CR0_0_300_300_PT0_SX300_V1_128x128.jpg?v=1689689735",
                      "lg": "https://cdn.shopify.com/s/files/1/0692/3996/3670/files/41bc5767-d56f-432c-ac5f-6b9eeee3ba0e.__CR0_0_300_300_PT0_SX300_V1_256x256.jpg?v=1689689735"
                    },
                    "quantity": 1,
                    "title": "The Box: How the Shipping Container Made the World Smaller and the World Economy Bigger",
                    "price": {
                      "amount": 8,
                      "currencyCode": "GBP"
                    },
                    "merchandiseId": "gid://shopify/ProductVariant/43835075002390",
                    "productId": "gid://shopify/Product/8013997834262"
                  }
                ],
                "price": {
                  "total": {
                    "amount": 13.99,
                    "currencyCode": "GBP"
                  },
                  "subtotal": {
                    "amount": 8,
                    "currencyCode": "GBP"
                  },
                  "taxes": {
                    "amount": 0,
                    "currencyCode": "CAD"
                  },
                  "shipping": {
                    "amount": 5.99,
                    "currencyCode": "GBP"
                  }
                }
              }
            }
        """.trimIndent()
    }
}
