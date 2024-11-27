package com.shopify.checkout_sdk_mobile_buy_integration_sample.products.product.data

data class Products(
    val products: List<Product> = mutableListOf(),
    val pageInfo: PageInfo = PageInfo(),
)

data class PageInfo(
    val startCursor: String? = null,
    val endCursor: String? = null,
)

data class Product(
    val id: String = "",
    val title: String = "",
    val description: String? = "",
    val image: ProductImage? = ProductImage(),
    val selectedVariant: Int? = 0,
    val priceRange: ProductPriceRange = ProductPriceRange(
        minVariantPrice = ProductPriceAmount(),
        maxVariantPrice = ProductPriceAmount()
    ),
    val variants: MutableList<ProductVariant>? = mutableListOf(ProductVariant())
)

data class ProductVariant(
    val price: String = "",
    val currencyName: String = "",
    val id: String = "",
)

data class ProductPriceRange(
    val maxVariantPrice: ProductPriceAmount,
    val minVariantPrice: ProductPriceAmount,
)

data class ProductPriceAmount(
    val currencyCode: String = "",
    val amount: Double = 0.0,
)

data class ProductImage(
    val width: Int = 0,
    val height: Int = 0,
    val altText: String? = null,
    val url: String? = null,
)
