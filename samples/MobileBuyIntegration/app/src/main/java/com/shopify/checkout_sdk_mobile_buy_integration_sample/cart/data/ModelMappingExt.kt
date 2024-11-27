package com.shopify.checkout_sdk_mobile_buy_integration_sample.cart.data

import com.shopify.buy3.Storefront


internal fun Storefront.Cart.toLocal(): CartState.Cart {
    return CartState.Cart(
        cartID = id.toString(),
        cartLines = this.lines.nodes.mapNotNull { cartLine -> cartLine.toLocal() },
        cartTotals = CartTotals(
            totalAmount = CartAmount(
                currency = cost.totalAmount.currencyCode.name,
                price = cost.totalAmount.amount.toDouble(),
            ),
            totalAmountEstimated = cost.totalAmountEstimated,
            totalQuantity = totalQuantity
        ),
        checkoutUrl = checkoutUrl,
    )
}

internal fun Storefront.BaseCartLine.toLocal(): CartLine? {
    return (this.merchandise as? Storefront.ProductVariant)?.let {
        CartLine(
            id = this.id.toString(),
            imageURL = it.product.featuredImage.url,
            imageAltText = it.product.featuredImage.altText ?: "",
            title = it.product.title,
            vendor = it.product.vendor,
            quantity = this.quantity,
            pricePerQuantity = this.cost.amountPerQuantity.amount.toDouble(),
            currencyPerQuantity = this.cost.amountPerQuantity.currencyCode.name,
            totalPrice = this.cost.totalAmount.amount.toDouble(),
            totalCurrency = this.cost.totalAmount.currencyCode.name,
        )
    }
}
