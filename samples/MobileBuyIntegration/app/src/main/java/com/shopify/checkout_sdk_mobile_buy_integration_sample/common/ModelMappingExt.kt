package com.shopify.checkout_sdk_mobile_buy_integration_sample.common

data class ID(val id: String)

fun ID.toGraphQLId(): com.shopify.graphql.support.ID {
    return com.shopify.graphql.support.ID(this.id)
}

fun com.shopify.graphql.support.ID.toLocal(): ID {
    return ID(this.toString())
}
