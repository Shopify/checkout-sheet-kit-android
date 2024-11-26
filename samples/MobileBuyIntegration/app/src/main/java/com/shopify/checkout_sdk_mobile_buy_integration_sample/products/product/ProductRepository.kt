package com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product

import com.shopify.buy3.Storefront.Product
import com.shopify.graphql.support.ID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.coroutines.suspendCoroutine

class ProductRepository(
    private val client: ProductsStorefrontApiClient,
) {

    suspend fun getProduct(productId: ID): Flow<UIProduct> {
        return suspendCoroutine { continuation ->
            client.fetchProduct(productId = productId, numVariants = 1, { result ->
                val product = result.data?.product
                if (product == null) {
                    continuation.resumeWith(Result.failure(RuntimeException("Failed to fetch product")))
                } else {
                    continuation.resumeWith(
                        Result.success(
                            flowOf(product.toUIProduct())
                        )
                    )
                }
            }, { exception ->
                continuation.resumeWith(Result.failure(exception))
            })
        }
    }

    suspend fun getProducts(numProducts: Int, numVariants: Int, cursor: String?): Flow<UIProducts> {
        return suspendCoroutine { continuation ->
            client.fetchProducts(numProducts = numProducts, numVariants = numVariants, cursor = cursor, { response ->
                val products = response.data?.products
                if (products == null) {
                    continuation.resumeWith(Result.failure(RuntimeException("Failed to fetch products")))
                } else {
                    val uiProducts = UIProducts(
                        products = products.edges.map { it.node.toUIProduct() },
                        pageInfo = PageInfo(
                            startCursor = products.pageInfo.startCursor,
                            endCursor = products.pageInfo.endCursor,
                        )
                    )
                    continuation.resumeWith(Result.success(flowOf(uiProducts)))
                }
            }, { exception ->
                continuation.resumeWith(Result.failure(exception))
            })
        }
    }
}

fun Product.toUIProduct(): UIProduct {
    val variants = this.variants
    val firstVariant = variants?.nodes?.firstOrNull()
    val uiProduct = UIProduct(
        id = id.toString(),
        title = title,
        description = description,
        image = if (featuredImage == null) UIProductImage() else UIProductImage(
            width = featuredImage.width,
            height = featuredImage.height,
            url = featuredImage.url,
            altText = featuredImage.altText ?: "Product image",
        ),
        priceRange = UIProductPriceRange(
            minVariantPrice = UIProductPriceAmount(
                currencyCode = priceRange.minVariantPrice.currencyCode.name,
                amount = priceRange.minVariantPrice.amount.toDouble()
            ),
            maxVariantPrice = UIProductPriceAmount(
                currencyCode = priceRange.maxVariantPrice.currencyCode.name,
                amount = priceRange.maxVariantPrice.amount.toDouble()
            )
        ),
        variants =
        if (firstVariant != null) {
            mutableListOf(
                UIProductVariant(
                    id = firstVariant.id.toString(),
                    price = firstVariant.price.amount,
                    currencyName = firstVariant.price.currencyCode.name,
                )
            )
        } else {
            mutableListOf()
        }
    )
    return uiProduct
}

data class UIProducts(
    val products: List<UIProduct> = mutableListOf(),
    val pageInfo: PageInfo = PageInfo(),
)

data class PageInfo(
    val startCursor: String? = null,
    val endCursor: String? = null,
)

data class UIProduct(
    val id: String = "",
    val title: String = "",
    val description: String? = "",
    val image: UIProductImage? = UIProductImage(),
    val selectedVariant: Int? = 0,
    val priceRange: UIProductPriceRange = UIProductPriceRange(
        minVariantPrice = UIProductPriceAmount(),
        maxVariantPrice = UIProductPriceAmount()
    ),
    val variants: MutableList<UIProductVariant>? = mutableListOf(UIProductVariant())
)

data class UIProductVariant(
    val price: String = "",
    val currencyName: String = "",
    val id: String = "",
)

data class UIProductPriceRange(
    val maxVariantPrice: UIProductPriceAmount,
    val minVariantPrice: UIProductPriceAmount,
)

data class UIProductPriceAmount(
    val currencyCode: String = "",
    val amount: Double = 0.0,
)

data class UIProductImage(
    val width: Int = 0,
    val height: Int = 0,
    val altText: String? = null,
    val url: String? = null,
)
