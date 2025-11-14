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

import com.shopify.checkoutsheetkit.events.CheckoutStartEvent
import com.shopify.checkoutsheetkit.events.Money
import com.shopify.checkoutsheetkit.events.parser.CheckoutMessageParser
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CheckoutStartEventTest {

    private val mockLogWrapper = mock<LogWrapper>()

    private val parser = CheckoutMessageParser(
        json = Json { ignoreUnknownKeys = true },
        log = mockLogWrapper
    )

    @Test
    fun `should decode cart details`() {
        val result = parseCheckoutStartEvent(EXAMPLE_EVENT)

        assertThat(result.cart.id).isEqualTo("gid://shopify/Cart/Z2NwLXVzLWVhc3QxOjAxSktTQ0U1MzJTUzg5UEhZQUVQQzdZUFlS")
        assertThat(result.cart.lines).hasSize(1)
        assertThat(result.cart.cost.totalAmount).isEqualTo(Money(amount = "29.95", currencyCode = "USD"))
    }

    @Test
    fun `should decode cart line details`() {
        val result = parseCheckoutStartEvent(EXAMPLE_EVENT)
        val line = result.cart.lines.single()

        val expectedId = "gid://shopify/CartLine/01JKSCE532SS89PHYAEPC7YPYR?cart=Z2NwLXVzLWVhc3QxOjAxSktTQ0U1MzJTUzg5UEhZQUVQQzdZUFlS"
        assertThat(line.id).isEqualTo(expectedId)
        assertThat(line.quantity).isEqualTo(1)
        assertThat(line.merchandise.title).isEqualTo("The Collection Snowboard: Hydrogen")
        assertThat(line.merchandise.product.title).isEqualTo("The Collection Snowboard: Liquid")
        assertThat(line.cost.totalAmount).isEqualTo(Money(amount = "749.95", currencyCode = "USD"))
    }

    @Test
    fun `should decode cart buyer identity`() {
        val result = parseCheckoutStartEvent(EXAMPLE_EVENT)
        val buyerIdentity = result.cart.buyerIdentity

        assertThat(buyerIdentity.email).isNull()
        assertThat(buyerIdentity.phone).isNull()
        assertThat(buyerIdentity.countryCode).isEqualTo("US")
    }

    @Test
    fun `should decode cart delivery details`() {
        val result = parseCheckoutStartEvent(EXAMPLE_EVENT)
        val deliveryAddress = result.cart.delivery.addresses.single().address

        assertThat(deliveryAddress.countryCode).isEqualTo("US")
        assertThat(deliveryAddress.firstName).isNull()
        assertThat(deliveryAddress.address1).isNull()
    }

    @Test
    fun `should return null on decode failure`() {
        val result = parser.parse("""{"jsonrpc":"2.0","method":"checkout.start","params":{"invalid":"json"}}""")

        assertThat(result).isNull()
    }

    @Test
    fun `should accept valid money amounts`() {
        // Valid decimal amounts should not throw
        Money(amount = "10.00", currencyCode = "USD")
        Money(amount = "0", currencyCode = "GBP")
        Money(amount = "99.99", currencyCode = "CAD")
        Money(amount = "1234.567", currencyCode = "EUR")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should reject invalid money amount`() {
        Money(amount = "not-a-number", currencyCode = "USD")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should reject blank currency code`() {
        Money(amount = "10.00", currencyCode = "")
    }

    private fun parseCheckoutStartEvent(params: String): CheckoutStartEvent {
        val message = parser.parse(params.toJsonRpcEnvelope())
        return (message as CheckoutMessageParser.JSONRPCMessage.Started).event
    }

    private fun String.toJsonRpcEnvelope(): String {
        return """{"jsonrpc":"2.0","method":"checkout.start","params":$this}"""
    }

    companion object {
        private val EXAMPLE_EVENT = """
            {
              "cart": {
                "id": "gid://shopify/Cart/Z2NwLXVzLWVhc3QxOjAxSktTQ0U1MzJTUzg5UEhZQUVQQzdZUFlS",
                "lines": [
                  {
                    "id": "gid://shopify/CartLine/01JKSCE532SS89PHYAEPC7YPYR?cart=Z2NwLXVzLWVhc3QxOjAxSktTQ0U1MzJTUzg5UEhZQUVQQzdZUFlS",
                    "quantity": 1,
                    "merchandise": {
                      "id": "gid://shopify/ProductVariant/44324338090262",
                      "title": "The Collection Snowboard: Hydrogen",
                      "product": {
                        "id": "gid://shopify/Product/8013997834262",
                        "title": "The Collection Snowboard: Liquid"
                      },
                      "selectedOptions": []
                    },
                    "cost": {
                      "amountPerQuantity": {
                        "amount": "749.95",
                        "currencyCode": "USD"
                      },
                      "subtotalAmount": {
                        "amount": "749.95",
                        "currencyCode": "USD"
                      },
                      "totalAmount": {
                        "amount": "749.95",
                        "currencyCode": "USD"
                      }
                    },
                    "discountAllocations": []
                  }
                ],
                "cost": {
                  "subtotalAmount": {
                    "amount": "749.95",
                    "currencyCode": "USD"
                  },
                  "totalAmount": {
                    "amount": "29.95",
                    "currencyCode": "USD"
                  }
                },
                "buyerIdentity": {
                  "countryCode": "US"
                },
                "deliveryGroups": [],
                "discountCodes": [],
                "appliedGiftCards": [],
                "discountAllocations": [],
                "delivery": {
                  "addresses": [
                    {
                      "address": {
                        "countryCode": "US"
                      }
                    }
                  ]
                }
              }
            }
        """.trimIndent()
    }
}
