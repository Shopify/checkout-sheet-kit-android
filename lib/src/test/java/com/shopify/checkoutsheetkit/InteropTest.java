package com.shopify.checkoutsheetkit;

import static org.assertj.core.api.Assertions.assertThat;

import android.app.Activity;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;

import com.shopify.checkoutsheetkit.pixelevents.CheckoutStartedData;
import com.shopify.checkoutsheetkit.pixelevents.CheckoutStartedEvent;
import com.shopify.checkoutsheetkit.pixelevents.PixelEvent;
import com.shopify.checkoutsheetkit.pixelevents.PixelEventDecoder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import kotlinx.serialization.json.Json;

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
            public void onWebPixelEvent(@NonNull PixelEvent event) {

            }

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

    // java tests lack access to internal kotlin classes in the project
    @SuppressWarnings("all")
    @Test
    public void canAccessFieldsOnPixelEvents() {
        String orderId = "123";
        String eventString = "{" +
            "\"name\": \"checkout_started\"," +
            "\"event\": {" +
                "\"type\": \"standard\"," +
                "\"id\": \"sh-88153c5a-8F2D-4CCA-3231-EF5C032A4C3B\"," +
                "\"name\": \"checkout_started\"," +
                "\"timestamp\": \"2023-12-20T16:39:23+0000\"," +
                "\"data\": {" +
                    "\"checkout\": {" +
                        "\"order\": {" +
                        "\"id\": \"" + orderId + "\"" +
                        "}" +
                    "}" +
                "}" +
            "}" +
        "}";

        WebToSdkEvent webEvent = new WebToSdkEvent("analytics", eventString);
        Json json = Json.Default;

        PixelEventDecoder decoder = new PixelEventDecoder(
            json
        );

        PixelEvent event = decoder.decode(webEvent);

        assertThat(event).isInstanceOf(CheckoutStartedEvent.class);
        CheckoutStartedEvent checkoutStartedEvent = (CheckoutStartedEvent) event;
        CheckoutStartedData checkoutStartedData = checkoutStartedEvent.getData();
        String checkoutStartedOrderId = checkoutStartedData.getCheckout().getOrder().getId();

        assertThat(checkoutStartedOrderId).isEqualTo(orderId);
     }
}
