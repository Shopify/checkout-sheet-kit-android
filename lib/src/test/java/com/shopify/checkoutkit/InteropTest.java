package com.shopify.checkoutkit;

import static org.assertj.core.api.Assertions.assertThat;

import android.app.Activity;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class InteropTest {

    private Activity activity = null;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(ComponentActivity.class).get();
    }

    @Test
    public void canInstantiateCustomEventProcessorWithDefaultArg() {
        DefaultCheckoutEventProcessor processor = new DefaultCheckoutEventProcessor(activity) {
            @Override
            public void onCheckoutCompleted() {

            }

            @Override
            public void onCheckoutFailed(@NonNull CheckoutException error) {

            }

            @Override
            public void onCheckoutCanceled() {

            }
        };

        assertThat(processor).isNotNull();
    }
}
