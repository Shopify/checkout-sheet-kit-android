# Native Address Picker Implementation

This document describes the changes made to implement native address and payment details pickers for the Shopify checkout-sheet-kit-android library.

## Overview

The native picker feature allows clients to provide custom address/payment selection screens while maintaining library-controlled navigation and preserving WebView bridge connections. This is an opt-in alternative to the standard `ShopifyCheckoutSheetKit.present()` method.

## Key Features

- **Library-controlled navigation**: The SDK manages transitions between checkout and custom screens
- **WebView state preservation**: Bridge connection maintained during navigation to prevent data loss
- **Type-safe APIs**: Strongly typed events and responses using kotlinx serialization
- **Title customization**: Dynamic toolbar title changes (e.g., "Buy Now!" → "Select Address")
- **Fragment support**: Initial implementation supports Android Fragments with placeholders for Activities and Compose

## Implementation Details

### Core Components

#### 1. ShopifyCheckoutController (`ShopifyCheckoutController.kt`)
- New controller class providing `presentCheckoutWithController()` method
- Requires `FragmentActivity` for proper fragment management
- Handles address change intents by triggering custom screen navigation

#### 2. CheckoutScreen Sealed Class (`CheckoutScreen.kt`)
```kotlin
sealed class CheckoutScreen {
    data class FragmentScreen(
        val fragment: Fragment, 
        val config: CheckoutScreenConfig = CheckoutScreenConfig()
    ) : CheckoutScreen()
    
    // Placeholders for future implementation
    data class ActivityScreen(...)
    data class ComposableScreen(...)
}

data class CheckoutScreenConfig(
    val title: String? = null
)
```

#### 3. RespondableEvent System (`RespondableEvent.kt`)
- Abstract base class for events requiring user response
- Type-safe `respondWith(result: DeliveryAddressChangePayload)` method (non-null)
- `cancel()` method for user cancellation

#### 4. CheckoutAddressChangeIntentEvent (`CheckoutAddressChangeIntentDecoder.kt`)
- Concrete implementation of `RespondableEvent`
- Decoded from checkout WebView using kotlinx serialization
- Prevents double-response with internal state tracking

### Navigation Architecture

#### 5. CheckoutNavigationManager (`CheckoutNavigationManager.kt`)
- Manages transitions between WebView and custom screens
- Preserves WebView state using pause/resume lifecycle
- Handles title changes and restoration
- Uses hybrid fragment management (manual view creation + FragmentManager context)

#### 6. CheckoutControllerDialog (`CheckoutControllerDialog.kt`)
- Extended dialog with navigation capabilities
- Custom event processor with address change handling
- Enhanced layout (`dialog_checkout_controller.xml`) with navigation container
- Toolbar title manipulation instead of dialog title

### Event Processing

#### 7. Enhanced CheckoutBridge (`CheckoutBridge.kt`)
- New `ADDRESS_CHANGE_INTENT` message type
- Type-safe response handling with `DeliveryAddressChangePayload`
- Main thread dispatching for UI operations
- Automatic navigation callbacks (`onAddressResponseComplete`, `onAddressCancelled`)

#### 8. CheckoutWebViewEventProcessor Extensions
- Optional `addressChangeHandler` parameter using strategy pattern
- Extensible design avoiding code duplication
- Overridable navigation callback methods

## Sample Integration

The MobileBuyIntegration sample was updated to demonstrate the controller approach:

```kotlin
// Convert FragmentActivity requirement
class MainActivity : FragmentActivity() { ... }

// Using the controller
val controller = ShopifyCheckoutController { event ->
    val screen = CheckoutScreen.FragmentScreen(
        fragment = AddressSelectionFragment().apply {
            onAddressSelected = { address ->
                event.respondWith(address.toDeliveryAddressChangePayload())
            }
        },
        config = CheckoutScreenConfig(title = "Select Address")
    )
    screen
}

ShopifyCheckoutSheetKit.presentCheckoutWithController(
    checkoutUrl = checkoutUrl,
    context = this,
    controller = controller
)
```

## Technical Decisions

### Threading
- JavaScript bridge calls happen on background threads
- UI operations dispatched to main thread using `Handler(Looper.getMainLooper())`

### Resource Management
- Avoided `FragmentContainerView` due to cross-module resource ID conflicts
- Used `FrameLayout` with manual fragment view management
- Hybrid approach: FragmentManager for context + manual view creation

### Fragment Lifecycle
- Temporary fragments tagged as `"temp_fragment"`
- Manual `onCreateView`/`onViewCreated` calls for proper lifecycle
- Fragment cleanup on navigation back

### Title Management
- Direct toolbar manipulation (`findViewById<Toolbar>`) instead of dialog title
- Original title preservation and restoration
- Callback-based title change system

## API Design Principles

1. **Opt-in Feature**: Maintains backward compatibility with existing `present()` method
2. **Library Control**: Navigation managed by SDK, not client callbacks
3. **Type Safety**: Non-null `DeliveryAddressChangePayload` requirement
4. **Extensibility**: Sealed class design for future screen types
5. **Resource Preservation**: WebView bridge maintained during navigation

## Files Modified/Created

### New Files
- `ShopifyCheckoutController.kt`
- `CheckoutScreen.kt`  
- `RespondableEvent.kt`
- `CheckoutAddressChangeIntentDecoder.kt`
- `CheckoutNavigationManager.kt`
- `CheckoutControllerDialog.kt`
- `res/layout/dialog_checkout_controller.xml`

### Modified Files
- `CheckoutBridge.kt` - Added address change intent handling
- `CheckoutWebViewEventProcessor.kt` - Added optional address handler
- `CheckoutDialog.kt` - Enhanced for inheritance
- Sample app files for demonstration

## Future Enhancements

1. **ActivityScreen Support**: Launch intents for activity-based pickers
2. **ComposableScreen Support**: Jetpack Compose integration
3. **Payment Details Picker**: Similar pattern for payment methods
4. **Enhanced Configuration**: More UI customization options
5. **Animation Support**: Custom transition animations

## Testing

All existing unit tests pass, ensuring backward compatibility. The implementation follows existing patterns and conventions within the codebase.