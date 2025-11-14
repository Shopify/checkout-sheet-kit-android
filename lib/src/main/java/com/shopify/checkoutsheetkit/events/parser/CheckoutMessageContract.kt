package com.shopify.checkoutsheetkit.events.parser

internal object CheckoutMessageContract {
    const val VERSION_FIELD = "jsonrpc"
    const val METHOD_FIELD = "method"
    const val PARAMS_FIELD = "params"
    const val ID_FIELD = "id"

    const val VERSION = "2.0"

    const val METHOD_ADDRESS_CHANGE_REQUESTED = "checkout.addressChangeRequested"
    const val METHOD_COMPLETE  = "checkout.complete"
    const val METHOD_START  = "checkout.start"
}
