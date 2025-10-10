package com.shopify.checkoutsheetkit;

import android.net.Uri;
import org.assertj.core.api.Assertions;

public class CheckoutAssertions extends Assertions {
    public static CheckoutExceptionAssert assertThat(CheckoutException actual) {
        return new CheckoutExceptionAssert(actual);
    }

    public static EmbedParamAssert assertThat(Uri actual) {
        return new EmbedParamAssert(actual);
    }
}
