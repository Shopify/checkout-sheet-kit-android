package com.shopify.checkoutsheetkit;

import static org.assertj.core.api.Assertions.assertThat;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;

import com.shopify.checkoutsheetkit.errorevents.CheckoutErrorDecoder;
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEvent;
import com.shopify.checkoutsheetkit.lifecycleevents.CheckoutCompletedEventDecoder;
import com.shopify.checkoutsheetkit.pixelevents.PixelEvent;
import com.shopify.checkoutsheetkit.pixelevents.PixelEventDecoder;
import com.shopify.checkoutsheetkit.pixelevents.StandardPixelEvent;
import com.shopify.checkoutsheetkit.pixelevents.StandardPixelEventData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import java.util.function.Function;

import kotlinx.serialization.json.Json;
import kotlinx.serialization.json.JsonKt;

@RunWith(RobolectricTestRunner.class)
public class InteropTest {
    private Configuration initialConfiguration = null;

    @Before
    public void setUp() {
        initialConfiguration = ShopifyCheckoutSheetKit.getConfiguration();
    }

    @After
    public void tearDown() {
        ShopifyCheckoutSheetKit.configure(config -> {
            config.setColorScheme(initialConfiguration.getColorScheme());
            config.setPreloading(initialConfiguration.getPreloading());
            config.setErrorRecovery(initialConfiguration.getErrorRecovery());
        });
    }

    @Test
    public void canInstantiateCustomEventProcessorWithDefaultArg() {
        try (ActivityController<ComponentActivity> controller = Robolectric.buildActivity(ComponentActivity.class)) {
            DefaultCheckoutEventProcessor processor = new DefaultCheckoutEventProcessor(controller.get()) {
                @Override
                public void onWebPixelEvent(@NonNull PixelEvent event) {

                }

                @Override
                public void onCheckoutCompleted(@NonNull CheckoutCompletedEvent checkoutCompletedEvent) {

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

        PixelEventDecoder decoder = new PixelEventDecoder(json);

        PixelEvent event = decoder.decode(webEvent);

        assertThat(event).isInstanceOf(StandardPixelEvent.class);
        StandardPixelEvent checkoutStartedEvent = (StandardPixelEvent) event;
        StandardPixelEventData checkoutStartedData = checkoutStartedEvent.getData();
        String checkoutStartedOrderId = checkoutStartedData.getCheckout().getOrder().getId();

        assertThat(checkoutStartedOrderId).isEqualTo(orderId);
     }

    @SuppressWarnings("all")
    @Test
    public void canAccessFieldsOnExceptions() {
        String eventString = "[{" +
            "\"group\": \"expired\"," +
            "\"reason\": \"Checkout has expired\"," +
            "\"code\": \"cart_completed\"" +
        "}]";

        WebToSdkEvent webEvent = new WebToSdkEvent("error", eventString);
        Json json = JsonKt.Json(Json.Default, b -> {
            b.setIgnoreUnknownKeys(true);
            return null;
        });
        CheckoutErrorDecoder decoder = new CheckoutErrorDecoder(json);

        CheckoutException exception = decoder.decode(webEvent);

        assertThat(exception.getClass()).isEqualTo(CheckoutExpiredException.class);
        assertThat(exception.getErrorCode()).isEqualTo("cart_completed");
        assertThat(exception.getErrorDescription()).isEqualTo("Checkout has expired");
        assertThat(exception.isRecoverable()).isEqualTo(false);
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

    @Test
    public void canConfigureCheckoutSheetKit() {
        @SuppressWarnings("unchecked")
        Function<String, Void> fn = Mockito.mock(Function.class);
        CheckoutSheetKitException exception = new CheckoutSheetKitException("Internal Error", "n/a", true);

        ShopifyCheckoutSheetKit.configure(configuration -> {
            configuration.setPreloading(new Preloading(false));
            configuration.setColorScheme(new ColorScheme.Dark());
            configuration.setErrorRecovery(new ErrorRecovery() {
                @Override
                public boolean shouldRecoverFromError(@NonNull CheckoutException checkoutException) {
                    return false;
                }

                @Override
                public void preRecoveryActions(@NonNull CheckoutException exception, @NonNull String checkoutUrl) {
                    fn.apply("called");
                }
            });
        });

        Configuration configuration = ShopifyCheckoutSheetKit.getConfiguration();

        assertThat(configuration.getColorScheme().getId()).isEqualTo("dark");
        assertThat(configuration.getPreloading().getEnabled()).isEqualTo(false);
        assertThat(configuration.getErrorRecovery().shouldRecoverFromError(exception)).isEqualTo(false);

        configuration.getErrorRecovery().preRecoveryActions(exception, "https://shopify.dev");
        Mockito.verify(fn).apply("called");
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
