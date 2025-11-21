# Adding New Events to Checkout Sheet Kit Android

This guide walks through adding new events for WebView-to-Android communication.
We've implemented this pattern several times - see [`AddressChangeRequested`](lib/src/main/java/com/shopify/checkoutsheetkit/rpc/events/AddressChangeRequested.kt) as a reference.

## Quick Checklist

When adding a new event, you need to modify these files:

- [ ] **Event Definition**: Create new event class in `lib/src/main/java/com/shopify/checkoutsheetkit/rpc/events/YourEvent.kt`
- [ ] **Registry**: Add to `requestTypes` list in [`RPCRequestRegistry.kt`](lib/src/main/java/com/shopify/checkoutsheetkit/rpc/RPCRequestRegistry.kt#L45-L49)
- [ ] **Handler Interface**: Add handler method to [`CheckoutEventProcessor.kt`](lib/src/main/java/com/shopify/checkoutsheetkit/CheckoutEventProcessor.kt)
- [ ] **Dispatch Logic**: Add case in [`CheckoutBridge.kt`](lib/src/main/java/com/shopify/checkoutsheetkit/CheckoutBridge.kt) `postMessage()` method
- [ ] **Tests**: Add decoding test in `lib/src/test/java/com/shopify/checkoutsheetkit/`

## Step 1: Define Your Event

Create a new file following the pattern in [`AddressChangeRequested.kt`](lib/src/main/java/com/shopify/checkoutsheetkit/rpc/events/AddressChangeRequested.kt):

## Step 2: Register in RPCRequestRegistry

Add your event to the `requestTypes` list in [`RPCRequestRegistry.kt`](lib/src/main/java/com/shopify/checkoutsheetkit/rpc/RPCRequestRegistry.kt#L45-L49):

```kotlin
public val requestTypes: List<TypeErasedRPCDecodable> = listOf(
    AddressChangeRequested.Companion,
    CheckoutStart.Companion,
    CheckoutComplete.Companion,
    YourEvent.Companion  // Add here
)
```

## Step 3: Add Handler to CheckoutEventProcessor

Add your handler method alongside existing ones like `onAddressChangeRequested` in [`CheckoutEventProcessor.kt`](lib/src/main/java/com/shopify/checkoutsheetkit/CheckoutEventProcessor.kt):

```kotlin
public interface CheckoutEventProcessor {
    // ... existing methods ...

    public fun onYourEvent(event: YourEvent)
}
```

Also update `NoopEventProcessor` and `DefaultCheckoutEventProcessor` with default implementations.

## Step 4: Add Dispatch in CheckoutBridge

Add a case for your event in the `when` statement in [`CheckoutBridge.kt`](lib/src/main/java/com/shopify/checkoutsheetkit/CheckoutBridge.kt) `postMessage()`:

```kotlin
when (rpcRequest) {
    is AddressChangeRequested -> {
        log.d(LOG_TAG, "Received checkout.addressChangeRequested message")
        onMainThread {
            eventProcessor.onAddressChangeRequested(rpcRequest)
        }
    }
    is YourEvent -> {  // Add this case
        log.d(LOG_TAG, "Received ${rpcRequest.method} message")
        onMainThread {
            eventProcessor.onYourEvent(rpcRequest)
        }
    }
    // ... other cases
}
```

## Step 5: Update Parser (if needed)

For special handling, add a case in [`CheckoutMessageParser.kt`](lib/src/main/java/com/shopify/checkoutsheetkit/CheckoutMessageParser.kt):

```kotlin
fun parse(rawMessage: String): CheckoutMessage? {
    return when (val decoded = RPCRequestRegistry.decode(rawMessage)) {
        is AddressChangeRequested -> CheckoutMessage.Request(decoded)
        is YourEvent -> CheckoutMessage.Request(decoded)  // Add if needed
        // ... other cases
    }
}
```

## Step 6: Implementation in Apps

Apps using the SDK implement the handler:

```kotlin
class MyEventProcessor : DefaultCheckoutEventProcessor(context) {
    override fun onYourEvent(event: YourEvent) {
        // Handle the event
        val response = YourResponsePayload(/* ... */)
        event.respondWith(response)  // For request-response events
    }
}
```

## Event Types

- **Request-Response Events** (like `AddressChangeRequested`): Have an `id`, can respond with `event.respondWith(payload)`
- **Notification Events** (like `CheckoutStart`, `CheckoutComplete`): No `id` (pass `null`), one-way communication

## WebView Integration

The WebView sends events via:

```javascript
MobileCheckoutSdk.postMessage(
  JSON.stringify({
    jsonrpc: "2.0",
    id: "unique-id", // Omit for notification events
    method: "checkout.yourEventName",
    params: {
      field1: "value1",
      field2: "value2",
    },
  }),
);
```

## Testing

Add a decoding test:

```kotlin
@Test
fun `test decode YourEvent from JSON`() {
    val json = """
        {
            "jsonrpc": "2.0",
            "id": "test-123",
            "method": "checkout.yourEventName",
            "params": { "field1": "value1" }
        }
    """.trimIndent()

    val decoded = RPCRequestRegistry.decode(json)
    assertNotNull(decoded)
    assertTrue(decoded is YourEvent)
}
```

## Examples

See these existing events for reference:

- [`AddressChangeRequested`](lib/src/main/java/com/shopify/checkoutsheetkit/rpc/events/AddressChangeRequested.kt) - Request-response pattern
- [`CheckoutStart`](lib/src/main/java/com/shopify/checkoutsheetkit/rpc/CheckoutStart.kt) - Notification pattern
- [`CheckoutComplete`](lib/src/main/java/com/shopify/checkoutsheetkit/rpc/CheckoutComplete.kt) - Notification with enum status
