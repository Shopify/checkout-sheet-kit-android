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
package com.shopify.checkout_sdk_sample

import kotlinx.serialization.json.Json
import java.nio.charset.Charset

class StorefrontClient(
    private val url: String = "https://${BuildConfig.storefrontDomain}/api/2023-07/graphql",
    private val accessToken: String = BuildConfig.storefrontAccessToken,
    private val decoder: Json = Json { ignoreUnknownKeys = true }
) {

    fun fetchFirstNProducts(
        numProducts: Int,
        numVariants: Int,
        successCallback: (QueryRoot) -> Unit,
        failureCallback: ((Exception) -> Unit)?,
    ) {
        val query = """
            query FetchProducts {
                products(first: $numProducts) {
                    nodes {
                        title
                        description
                        vendor
                        featuredImage {
                            url
                            altText
                            height
                            width
                        }
                        variants(first: $numVariants) {
                            nodes {
                                id
                                title
                                price {
                                    amount
                                    currencyCode
                                }
                            }
                        }
                    }
                }
            }
        """.trimIndent()

        perform(query, successCallback, failureCallback ?: {})
    }

    fun createCart(
        variantId: String,
        successCallback: (MutationRoot) -> Unit,
        failureCallback: ((Exception) -> Unit)? = {},
    ) {
        val mutation = """
            mutation CartCreate {
                cartCreate(input: { lines: { merchandiseId: "$variantId" } }) {
                    cart {
                        checkoutUrl
                    }
                }
            }
        """.trimIndent()

        perform(mutation, successCallback, failureCallback ?: {})
    }

    private inline fun <reified T> perform(
        body: String,
        successCallback: (T) -> Unit,
        failureCallback: (Exception) -> Unit,
    ) {
        val response = khttp.post(
            url = url,
            headers = mapOf(
                "Accept" to "application/json",
                "Content-Type" to "application/graphql",
                "X-Shopify-Storefront-Access-Token" to accessToken,
            ),
            data = body.byteInputStream(charset = Charset.forName("UTF-8")),
        )

        if (response.statusCode == 200) {
            try {
                val decoded = decoder.decodeFromString<GraphQLResponse<T>>(
                    response.text
                )
                successCallback(decoded.data)
            } catch (e: Exception) {
                failureCallback.invoke(RuntimeException("Decoding GraphQL response failed", e))
            }
        } else {
            failureCallback.invoke(RuntimeException("GraphQL call failed ${response.statusCode}"))
        }
    }
}
