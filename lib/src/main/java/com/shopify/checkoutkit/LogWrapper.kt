package com.shopify.checkoutkit

import android.util.Log

/**
 * Wrap Log class static methods to allow testing
 */
public class LogWrapper {
    public fun w(tag: String, msg: String) {
        Log.w(tag, msg)
    }

    public fun e(tag: String, msg: String) {
        Log.e(tag, msg)
    }
}
