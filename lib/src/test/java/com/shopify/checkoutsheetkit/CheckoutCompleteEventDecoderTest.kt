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

import com.shopify.checkoutsheetkit.lifecycleevents.CartAddress
import com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryGroupType
import com.shopify.checkoutsheetkit.lifecycleevents.CartDeliveryMethodType
import com.shopify.checkoutsheetkit.lifecycleevents.CartDiscountCode
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompleteEventDecoder
import com.shopify.checkoutsheetkit.lifecycleevents.DiscountValue
import com.shopify.checkoutsheetkit.lifecycleevents.Money
import com.shopify.checkoutsheetkit.lifecycleevents.PricingPercentageValue
import com.shopify.checkoutsheetkit.lifecycleevents.SelectedOption
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CheckoutCompleteEventDecoderTest {

    private val mockLogWrapper = mock<LogWrapper>()

    private val decoder = CheckoutCompleteEventDecoder(
        decoder = Json { ignoreUnknownKeys = true },
        log = mockLogWrapper
    )

    @Test
    fun `should decode order confirmation details`() {
        val result = decoder.decode(EXAMPLE_EVENT.toWebToSdkEvent())
        val orderConfirmation = result.orderConfirmation

        assertThat(orderConfirmation.order.id).isEqualTo("gid://shopify/Order/9697125302294")
        assertThat(orderConfirmation.number).isEqualTo("1001")
        assertThat(orderConfirmation.isFirstOrder).isTrue()
        assertThat(orderConfirmation.url).isEqualTo("https://shopify.com/order-confirmation/9697125302294")
    }

    @Test
    fun `should decode cart line details`() {
        val result = decoder.decode(EXAMPLE_EVENT.toWebToSdkEvent())
        val line = result.cart.lines.single()

        assertThat(line.id).isEqualTo("gid://shopify/CartLine/1")
        assertThat(line.quantity).isEqualTo(1)
        assertThat(line.merchandise.title).isEqualTo(
            "The Box: How the Shipping Container Made the World Smaller and the World Economy Bigger"
        )
        assertThat(line.merchandise.product.title).isEqualTo("The Box")
        assertThat(line.merchandise.image?.url).isEqualTo(
            "https://cdn.shopify.com/s/files/1/0692/3996/3670/files/product-image_256x256.jpg"
        )
        assertThat(line.merchandise.selectedOptions).containsExactly(
            SelectedOption(name = "Format", value = "Hardcover")
        )

        val discountAllocation = line.discountAllocations.single()
        assertThat(discountAllocation.discountedAmount).isEqualTo(Money(amount = "1.00", currencyCode = "GBP"))
        assertThat(discountAllocation.discountApplication.value).isInstanceOf(DiscountValue.PercentageValue::class.java)
        assertThat((discountAllocation.discountApplication.value as DiscountValue.PercentageValue).percentage)
            .isEqualTo(PricingPercentageValue(percentage = 10.0))
    }

    @Test
    fun `should decode cart level cost and discounts`() {
        val result = decoder.decode(EXAMPLE_EVENT.toWebToSdkEvent())
        val cart = result.cart

        assertThat(cart.cost.subtotalAmount).isEqualTo(Money(amount = "8.00", currencyCode = "GBP"))
        assertThat(cart.cost.totalAmount).isEqualTo(Money(amount = "13.99", currencyCode = "GBP"))
        assertThat(cart.discountCodes).containsExactly(
            CartDiscountCode(code = "SUMMER", applicable = true)
        )
        val giftCard = cart.appliedGiftCards.single()
        assertThat(giftCard.amountUsed).isEqualTo(Money(amount = "10.00", currencyCode = "GBP"))
        assertThat(giftCard.balance).isEqualTo(Money(amount = "15.00", currencyCode = "GBP"))
        assertThat(giftCard.lastCharacters).isEqualTo("ABCD")

        val allocation = cart.discountAllocations.single()
        assertThat(allocation.discountApplication.value).isInstanceOf(DiscountValue.MoneyValue::class.java)
        assertThat((allocation.discountApplication.value as DiscountValue.MoneyValue).money)
            .isEqualTo(Money(amount = "2.00", currencyCode = "GBP"))
    }

    @Test
    fun `should decode cart buyer identity and delivery details`() {
        val result = decoder.decode(EXAMPLE_EVENT.toWebToSdkEvent())
        val cart = result.cart

        assertThat(cart.buyerIdentity.email).isEqualTo("a.user@shopify.com")
        assertThat(cart.buyerIdentity.customer?.firstName).isEqualTo("Andrew")
        assertThat(cart.buyerIdentity.countryCode).isEqualTo("GB")

        val deliveryGroup = cart.deliveryGroups.single()
        assertThat(deliveryGroup.groupType).isEqualTo(CartDeliveryGroupType.ONE_TIME_PURCHASE)
        assertThat(deliveryGroup.deliveryAddress.city).isEqualTo("Swansea")
        assertThat(deliveryGroup.deliveryOptions.single().deliveryMethodType)
            .isEqualTo(CartDeliveryMethodType.SHIPPING)
        assertThat(deliveryGroup.selectedDeliveryOption?.handle).isEqualTo("standard-shipping")

        val deliveryAddress = cart.delivery.addresses.single().address
        require(deliveryAddress is CartAddress.DeliveryAddress)
        assertThat(deliveryAddress.address1).isEqualTo("100 Street Avenue")
        assertThat(deliveryAddress.provinceCode).isEqualTo("WLS")
        assertThat(deliveryAddress.zip).isEqualTo("SA1 1AB")
    }

    @Test
    fun `should fall back to empty event on decode failure`() {
        val result = decoder.decode("not-json".toWebToSdkEvent())

        assertThat(result.orderConfirmation.order.id).isEmpty()
        assertThat(result.cart.lines).isEmpty()
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

    @Test
    fun `should fall back to empty event when money validation fails`() {
        val invalidMoneyJson = """
            {
              "orderConfirmation": {
                "order": { "id": "gid://shopify/Order/123" },
                "isFirstOrder": true
              },
              "cart": {
                "id": "gid://shopify/Cart/123",
                "cost": {
                  "subtotalAmount": { "amount": "invalid", "currencyCode": "USD" },
                  "totalAmount": { "amount": "10.00", "currencyCode": "USD" }
                },
                "buyerIdentity": {},
                "deliveryGroups": [],
                "delivery": { "addresses": [] }
              }
            }
        """.trimIndent()

        val result = decoder.decode(invalidMoneyJson.toWebToSdkEvent())

        assertThat(result.orderConfirmation.order.id).isEmpty()
        assertThat(result.cart.lines).isEmpty()
    }

    @Test
    fun `should fall back to empty event when discount value is not an object`() {
        val invalidDiscountJson = """
            {
              "orderConfirmation": {
                "order": { "id": "gid://shopify/Order/123" },
                "isFirstOrder": true
              },
              "cart": {
                "id": "gid://shopify/Cart/123",
                "cost": {
                  "subtotalAmount": { "amount": "10.00", "currencyCode": "USD" },
                  "totalAmount": { "amount": "10.00", "currencyCode": "USD" }
                },
                "buyerIdentity": {},
                "deliveryGroups": [],
                "discountAllocations": [
                  {
                    "discountedAmount": { "amount": "2.00", "currencyCode": "USD" },
                    "discountApplication": {
                      "allocationMethod": "ACROSS",
                      "targetSelection": "ALL",
                      "targetType": "LINE_ITEM",
                      "value": "not-an-object"
                    },
                    "targetType": "LINE_ITEM"
                  }
                ],
                "delivery": { "addresses": [] }
              }
            }
        """.trimIndent()

        val result = decoder.decode(invalidDiscountJson.toWebToSdkEvent())

        assertThat(result.orderConfirmation.order.id).isEmpty()
        assertThat(result.cart.lines).isEmpty()
    }

    private fun String.toWebToSdkEvent(): WebToSdkEvent {
        return WebToSdkEvent(
            name = CheckoutMessageContract.METHOD_COMPLETE,
            body = this,
        )
    }

    companion object {
        private val EXAMPLE_EVENT = """
            {
              "orderConfirmation": {
                "url": "https://shopify.com/order-confirmation/9697125302294",
                "order": {
                  "id": "gid://shopify/Order/9697125302294"
                },
                "number": "1001",
                "isFirstOrder": true
              },
              "cart": {
                "id": "gid://shopify/Cart/123",
                "lines": [
                  {
                    "id": "gid://shopify/CartLine/1",
                    "quantity": 1,
                    "merchandise": {
                      "id": "gid://shopify/ProductVariant/43835075002390",
                      "title": "The Box: How the Shipping Container Made the World Smaller and the World Economy Bigger",
                      "product": {
                        "id": "gid://shopify/Product/8013997834262",
                        "title": "The Box"
                      },
                      "image": {
                        "url": "https://cdn.shopify.com/s/files/1/0692/3996/3670/files/product-image_256x256.jpg",
                        "altText": "Front cover"
                      },
                      "selectedOptions": [
                        {
                          "name": "Format",
                          "value": "Hardcover"
                        }
                      ]
                    },
                    "cost": {
                      "amountPerQuantity": {
                        "amount": "8.00",
                        "currencyCode": "GBP"
                      },
                      "subtotalAmount": {
                        "amount": "8.00",
                        "currencyCode": "GBP"
                      },
                      "totalAmount": {
                        "amount": "8.00",
                        "currencyCode": "GBP"
                      }
                    },
                    "discountAllocations": [
                      {
                        "discountedAmount": {
                          "amount": "1.00",
                          "currencyCode": "GBP"
                        },
                        "discountApplication": {
                          "allocationMethod": "ACROSS",
                          "targetSelection": "ALL",
                          "targetType": "LINE_ITEM",
                          "value": {
                            "percentage": 10.0
                          }
                        },
                        "targetType": "LINE_ITEM"
                      }
                    ]
                  }
                ],
                "cost": {
                  "subtotalAmount": {
                    "amount": "8.00",
                    "currencyCode": "GBP"
                  },
                  "totalAmount": {
                    "amount": "13.99",
                    "currencyCode": "GBP"
                  }
                },
                "buyerIdentity": {
                  "email": "a.user@shopify.com",
                  "phone": "+447915123456",
                  "customer": {
                    "id": "gid://shopify/Customer/12345",
                    "firstName": "Andrew",
                    "lastName": "Person",
                    "email": "a.user@shopify.com",
                    "phone": "+447915123456"
                  },
                  "countryCode": "GB"
                },
                "deliveryGroups": [
                  {
                    "deliveryAddress": {
                      "address1": "100 Street Avenue",
                      "address2": "Unit 5",
                      "city": "Swansea",
                      "province": "Wales",
                      "country": "United Kingdom",
                      "countryCodeV2": "GB",
                      "zip": "SA1 1AB",
                      "firstName": "Andrew",
                      "lastName": "Person",
                      "phone": "+447915123456",
                      "company": "Shopify"
                    },
                    "deliveryOptions": [
                      {
                        "code": "standard",
                        "title": "Standard Shipping",
                        "description": "Arrives in 3-5 business days",
                        "handle": "standard-shipping",
                        "estimatedCost": {
                          "amount": "5.99",
                          "currencyCode": "GBP"
                        },
                        "deliveryMethodType": "SHIPPING"
                      }
                    ],
                    "selectedDeliveryOption": {
                      "code": "standard",
                      "title": "Standard Shipping",
                      "description": "Arrives in 3-5 business days",
                      "handle": "standard-shipping",
                      "estimatedCost": {
                        "amount": "5.99",
                        "currencyCode": "GBP"
                      },
                      "deliveryMethodType": "SHIPPING"
                    },
                    "groupType": "ONE_TIME_PURCHASE"
                  }
                ],
                "discountCodes": [
                  {
                    "code": "SUMMER",
                    "applicable": true
                  }
                ],
                "appliedGiftCards": [
                  {
                    "amountUsed": {
                      "amount": "10.00",
                      "currencyCode": "GBP"
                    },
                    "balance": {
                      "amount": "15.00",
                      "currencyCode": "GBP"
                    },
                    "lastCharacters": "ABCD",
                    "presentmentAmountUsed": {
                      "amount": "10.00",
                      "currencyCode": "GBP"
                    }
                  }
                ],
                "discountAllocations": [
                  {
                    "discountedAmount": {
                      "amount": "2.00",
                      "currencyCode": "GBP"
                    },
                    "discountApplication": {
                      "allocationMethod": "ACROSS",
                      "targetSelection": "ALL",
                      "targetType": "SHIPPING_LINE",
                      "value": {
                        "amount": "2.00",
                        "currencyCode": "GBP"
                      }
                    },
                    "targetType": "SHIPPING_LINE"
                  }
                ],
                "delivery": {
                  "addresses": [
                    {
                      "address": {
                        "address1": "100 Street Avenue",
                        "address2": "Unit 5",
                        "city": "Swansea",
                        "company": "Shopify",
                        "countryCode": "GB",
                        "firstName": "Andrew",
                        "lastName": "Person",
                        "phone": "+447915123456",
                        "provinceCode": "WLS",
                        "zip": "SA1 1AB"
                      }
                    }
                  ]
                }
              }
            }
        """.trimIndent()
    }
}
