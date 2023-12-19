package com.shopify.checkoutkit.messages

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
internal class RawAnalyticsEvent(
    internal val name: String,
    internal val event: JsonObject,
)

public interface AnalyticsEvent {
    public val id: String?
    public val name: String?
    public val type: String?
    public val timestamp: String?
}

@Serializable
public data class CartViewedEvent(
    public override val id: String?,
    public override val name: String?,
    public override val type: String?,
    public override val timestamp: String?,
    public val data: PixelEventsCartViewedData,
): AnalyticsEvent

@Serializable
public data class PixelEventsCartViewedData(
    public val cart: Cart? = null,
)

@Serializable
public data class Cart(
    public val id: String? = null,
    public val cost: CartCost? = null,
    public val lines: List<CartLine>? = null,
    public val totalQuantity: Int? = null,
)

@Serializable
public data class CartCost(
    public val totalAmount: MoneyV2? = null,
)

@Serializable
public data class CartLine(
    public val cost: CartLineCost? = null,
    public val merchandise: ProductVariant? = null,
    public val quantity: Int? = null,
)

@Serializable
public data class CartLineCost(
    public val totalAmount: MoneyV2? = null,
)

// TODO - BigDecimal over Double
@Serializable
public data class MoneyV2(
    val amount: Double? = null,
    val currencyCode: String? = null,
)

@Serializable
public data class ProductVariant(
    public val id: String? = null,
    public val price: MoneyV2? = null,
    public val image: Image? = null,
    public val product: Product? = null,
    public val sku: String? = null,
    public val title: String? = null,
    public val untranslatedTitle: String? = null,
)

@Serializable
public data class Image(
    public val src: String? = null,
)

@Serializable
public data class Product(
    public val id: String? = null,
    public val title: String? = null,
    public val type: String? = null,
    public val untranslatedTitle: String? = null,
    public val url: String? = null,
    public val vendor: String? = null,
)
