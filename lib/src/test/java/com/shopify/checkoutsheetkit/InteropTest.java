package com.shopify.checkoutsheetkit;

import static org.assertj.core.api.Assertions.assertThat;

import android.app.Activity;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;

import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEvent;
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEventDecoder;
import com.shopify.checkoutsheetkit.pixelevents.PixelEvent;
import com.shopify.checkoutsheetkit.pixelevents.PixelEventDecoder;
import com.shopify.checkoutsheetkit.pixelevents.StandardPixelEvent;
import com.shopify.checkoutsheetkit.pixelevents.StandardPixelEventData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import kotlinx.serialization.json.Json;
import kotlinx.serialization.json.JsonKt;

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
            public void onCheckoutCompleted(@NonNull CheckoutCompletedEvent checkoutCompletedEvent) {

            }

            @Override
            public void onCheckoutFailed(@NonNull CheckoutException error, boolean isRecoverable) {

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

        WebToSdkEvent webEvent = new WebToSdkEvent("webPixels", eventString);
        Json json = Json.Default;

        PixelEventDecoder decoder = new PixelEventDecoder(
            json
        );

        PixelEvent event = decoder.decode(webEvent);

        assertThat(event).isInstanceOf(StandardPixelEvent.class);
        StandardPixelEvent checkoutStartedEvent = (StandardPixelEvent) event;
        StandardPixelEventData checkoutStartedData = checkoutStartedEvent.getData();
        String checkoutStartedOrderId = checkoutStartedData.getCheckout().getOrder().getId();

        assertThat(checkoutStartedOrderId).isEqualTo(orderId);
     }

    @SuppressWarnings("all")
    @Test
    public void canAccessFieldsOnCheckoutCompletedEvent() {
        WebToSdkEvent webEvent = new WebToSdkEvent("completed", EXAMPLE_EVENT);
        Json json = JsonKt.Json(Json.Default, b -> {
            b.setIgnoreUnknownKeys(true);
            return null;
        });
        CheckoutCompletedEventDecoder decoder = new CheckoutCompletedEventDecoder(json);

        CheckoutCompletedEvent event = decoder.decode(webEvent);

        assertThat(event.getOrderDetails().getId())
                .isEqualTo("gid://shopify/OrderIdentity/9697125302294");
        assertThat(event.getOrderDetails().getCart().getLines().get(0).getPrice().getAmount())
                .isEqualTo(8.0);
    }

    private final String EXAMPLE_EVENT = "{\n" +
     "      \"orderDetails\": {\n" +
     "        \"id\": \"gid://shopify/OrderIdentity/9697125302294\",\n" +
     "        \"cart\": {\n" +
     "          \"token\": \"123abc\",\n" +
     "          \"lines\": [\n" +
     "            {\n" +
     "              \"image\": {\n" +
     "                \"sm\": \"https://cdn.shopify.com/s/files/1/0692/3996/3670/files/41bc5767-d56f-432c-ac5f-6b9eeee3ba0e.truncated...\",\n" +
     "                \"md\": \"https://cdn.shopify.com/s/files/1/0692/3996/3670/files/41bc5767-d56f-432c-ac5f-6b9eeee3ba0e.truncated...\",\n" +
     "                \"lg\": \"https://cdn.shopify.com/s/files/1/0692/3996/3670/files/41bc5767-d56f-432c-ac5f-6b9eeee3ba0e.truncated...\"\n" +
     "              },\n" +
     "              \"quantity\": 1,\n" +
     "              \"title\": \"The Box: How the Shipping Container Made the World Smaller and the World Economy Bigger\",\n" +
     "              \"price\": {\n" +
     "                \"amount\": 8,\n" +
     "                \"currencyCode\": \"GBP\"\n" +
     "              },\n" +
     "              \"merchandiseId\": \"gid://shopify/ProductVariant/43835075002390\",\n" +
     "              \"productId\": \"gid://shopify/Product/8013997834262\"\n" +
     "            }\n" +
     "          ],\n" +
     "          \"price\": {\n" +
     "            \"total\": {\n" +
     "              \"amount\": 13.99,\n" +
     "              \"currencyCode\": \"GBP\"\n" +
     "            },\n" +
     "            \"subtotal\": {\n" +
     "              \"amount\": 8,\n" +
     "              \"currencyCode\": \"GBP\"\n" +
     "            },\n" +
     "            \"taxes\": {\n" +
     "              \"amount\": 0,\n" +
     "              \"currencyCode\": \"GBP\"\n" +
     "            },\n" +
     "            \"shipping\": {\n" +
     "              \"amount\": 5.99,\n" +
     "              \"currencyCode\": \"GBP\"\n" +
     "            }\n" +
     "          }\n" +
     "        },\n" +
     "        \"email\": \"a.user@shopify.com\",\n" +
     "        \"shippingAddress\": {\n" +
     "          \"city\": \"Swansea\",\n" +
     "          \"countryCode\": \"GB\",\n" +
     "          \"postalCode\": \"SA1 1AB\",\n" +
     "          \"address1\": \"100 Street Avenue\",\n" +
     "          \"firstName\": \"Andrew\",\n" +
     "          \"lastName\": \"Person\",\n" +
     "          \"name\": \"Andrew\",\n" +
     "          \"zoneCode\": \"WLS\",\n" +
     "          \"phone\": \"+447915123456\",\n" +
     "          \"coordinates\": {\n" +
     "            \"latitude\": 54.5936785,\n" +
     "            \"longitude\": -3.013167399999999\n" +
     "          }\n" +
     "        },\n" +
     "        \"billingAddress\": {\n" +
     "          \"city\": \"Swansea\",\n" +
     "          \"countryCode\": \"GB\",\n" +
     "          \"postalCode\": \"SA1 1AB\",\n" +
     "          \"address1\": \"100 Street Avenue\",\n" +
     "          \"firstName\": \"Andrew\",\n" +
     "          \"lastName\": \"Person\",\n" +
     "          \"zoneCode\": \"WLS\",\n" +
     "          \"phone\": \"+447915123456\"\n" +
     "        },\n" +
     "        \"paymentMethods\": [\n" +
     "          {\n" +
     "            \"type\": \"wallet\",\n" +
     "            \"details\": {\n" +
     "              \"amount\": \"13.99\",\n" +
     "              \"currency\": \"GBP\",\n" +
     "              \"name\": \"SHOP_PAY\"\n" +
     "            }\n" +
     "          }\n" +
     "        ],\n" +
     "        \"deliveries\": [\n" +
     "          {\n" +
     "            \"method\": \"SHIPPING\",\n" +
     "            \"details\": {\n" +
     "              \"location\": {\n" +
     "                \"city\": \"Swansea\",\n" +
     "                \"countryCode\": \"GB\",\n" +
     "                \"postalCode\": \"SA1 1AB\",\n" +
     "                \"address1\": \"100 Street Avenue\",\n" +
     "                \"firstName\": \"Andrew\",\n" +
     "                \"lastName\": \"Person\",\n" +
     "                \"name\": \"Andrew\",\n" +
     "                \"zoneCode\": \"WLS\",\n" +
     "                \"phone\": \"+447915123456\",\n" +
     "                \"coordinates\": {\n" +
     "                  \"latitude\": 54.5936785,\n" +
     "                  \"longitude\": -3.013167399999999\n" +
     "                }\n" +
     "              }\n" +
     "            }\n" +
     "          }\n" +
     "        ]\n" +
     "      }\n" +
     "    }";
}
