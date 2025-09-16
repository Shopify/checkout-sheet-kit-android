# Native Address Picker Implementation

This document describes the changes made to implement native address and payment details pickers for the Shopify checkout-sheet-kit-android library.

## Overview

The native picker feature allows clients to provide custom address/payment selection screens while maintaining library-controlled navigation and preserving WebView bridge connections. This is an opt-in alternative to the standard `ShopifyCheckoutSheetKit.present()` method.

## Key Features

- **Library-controlled navigation**: The SDK manages transitions between checkout and custom screens
- **WebView state preservation**: Bridge connection maintained during navigation to prevent data loss
- **Type-safe APIs**: Strongly typed events and responses using kotlinx serialization
- **Title customization**: Dynamic toolbar title changes (e.g., "Buy Now!" → "Select Address")
- **Fragment support**: Implementation supports Android Fragments; designed for future extensibility

## Implementation Details

### Core Components

#### 1. ShopifyCheckoutController (`ShopifyCheckoutController.kt`)
- New controller class with Android-idiomatic Builder pattern API
- Works with any `ComponentActivity` for broad compatibility  
- Handles address change intents by triggering custom screen navigation
- Immutable configuration with validation and comprehensive documentation

#### 2. CheckoutScreen Sealed Class (`CheckoutScreen.kt`)
```kotlin
sealed class CheckoutScreen {
    data class Fragment(
        val view: androidx.fragment.app.Fragment, 
        val config: CheckoutScreenConfig = CheckoutScreenConfig()
    ) : CheckoutScreen()
}

data class CheckoutScreenConfig(
    val title: String? = null,
    @StringRes val titleRes: Int? = null
) {
    companion object {
        @JvmStatic
        fun withTitle(@StringRes titleRes: Int): CheckoutScreenConfig
        
        @JvmStatic
        fun withTitle(title: String): CheckoutScreenConfig
    }
}
```
Note: Designed for future extensibility. Additional screen types (Activity, Composable) will be added in future versions.

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
- Uses manual fragment management (direct onCreateView/onViewCreated calls)

#### 6. NavigationAwareCheckoutDialog (`NavigationAwareCheckoutDialog.kt`)
- Navigation-aware checkout dialog extending CheckoutDialog
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
class MainActivity : ComponentActivity() { ... }

// Create controller using Builder pattern
val controller = ShopifyCheckoutController.Builder(checkoutUrl, eventProcessor)
    .setAddressScreenProvider { event ->
        val fragment = AddressSelectionFragment().apply {
            onAddressSelected = { address ->
                event.respondWith(address.toDeliveryAddressChangePayload())
            }
            onCancel = { event.cancel() }
        }
        CheckoutScreen.Fragment(
            view = fragment,
            config = CheckoutScreenConfig.withTitle(R.string.address_selection_title)
        )
    }
    .build()

// Present the controller
controller.present(this)
```

## Technical Decisions

### Threading
- JavaScript bridge calls happen on background threads
- UI operations dispatched to main thread using `Handler(Looper.getMainLooper())`

### Resource Management
- Avoided `FragmentContainerView` due to cross-module resource ID conflicts
- Used `FrameLayout` with manual fragment view management
- Direct fragment instantiation without FragmentManager overhead

### Fragment Lifecycle
- Manual `onCreateView`/`onViewCreated` calls for proper lifecycle
- Simple view cleanup on navigation back
- Context resolution via container/inflater instead of requireContext()

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
6. **Android Idioms**: Builder pattern with fluent API and immutable configuration

### Android-Idiomatic Builder Pattern

The controller uses a Builder pattern following Android SDK conventions:

```kotlin
// Android-idiomatic approach
val controller = ShopifyCheckoutController.Builder(checkoutUrl, eventProcessor)
    .setAddressScreenProvider { event ->
        // Configure address screen
        // Using string resource (recommended)
        CheckoutScreen.Fragment(
            view = fragment, 
            config = CheckoutScreenConfig.withTitle(R.string.address_title)
        )
        
        // Using direct string (for dynamic titles)
        CheckoutScreen.Fragment(
            view = fragment,
            config = CheckoutScreenConfig.withTitle("Dynamic Title")
        )
    }
    .build()

// Future extensibility
val controller = ShopifyCheckoutController.Builder(checkoutUrl, eventProcessor)
    .setAddressScreenProvider { event -> ... }
    .setPaymentScreenProvider { event -> ... }  // Future enhancement
    .build()
```

**Benefits:**
- **Immutable Configuration**: No accidental modifications after creation
- **Input Validation**: Required parameters validated at build time
- **Method Chaining**: Fluent API with IDE autocomplete support
- **Extensible**: Easy to add new configuration options without breaking changes
- **Thread Safe**: No mutable state after construction
- **Android Familiar**: Consistent with `AlertDialog.Builder`, `Notification.Builder`, etc.

## Files Modified/Created

### New Files
- `ShopifyCheckoutController.kt` - Main controller API with Android-idiomatic Builder pattern; manages address screen providers and handles address change events
- `CheckoutScreen.kt` - Sealed class defining Fragment type with UI configuration; designed for future extensibility
- `RespondableEvent.kt` - Abstract base for events requiring user response; provides type-safe response/cancellation methods
- `CheckoutAddressChangeIntentDecoder.kt` - JSON decoder for address change events from WebView; creates respondable event instances with callbacks
- `CheckoutNavigationManager.kt` - Manages transitions between WebView and custom screens; handles WebView pause/resume, title changes, and manual fragment lifecycle
- `NavigationAwareCheckoutDialog.kt` - Navigation-aware checkout dialog; extends CheckoutDialog with custom screen support and enhanced event processing
- `res/layout/dialog_checkout_controller.xml` - Enhanced layout with navigation container for hosting custom screens alongside WebView

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

## Architecture Decisions

### Fragment Management Approach

During implementation, we evaluated two approaches for fragment lifecycle management:

#### Option 1: Full FragmentManager Integration (Initial Implementation)
```kotlin
// Requires FragmentActivity
fun present(activity: FragmentActivity): CheckoutSheetKitDialog? {
    val fragmentManager = activity.supportFragmentManager
    // Full fragment lifecycle support
}
```

**Pros:**
- Complete fragment lifecycle callbacks (onStart, onResume, onPause, onStop, onDestroy)
- Automatic state saving/restoration
- Fragment Result API support
- Standard Android fragment behavior

**Cons:**
- Requires `FragmentActivity` - limits compatibility
- Complex for React Native wrappers (would need custom FragmentActivity)
- Overkill for simple address/payment pickers

#### Option 2: Manual Fragment Management (Final Implementation)
```kotlin
// Works with any ComponentActivity
fun present(activity: ComponentActivity): CheckoutSheetKitDialog? {
    // Manual onCreateView()/onViewCreated() calls
    val fragmentView = fragment.onCreateView(inflater, container, null)
    fragment.onViewCreated(fragmentView, null)
}
```

**Pros:**
- Broader compatibility - works with `ComponentActivity`
- React Native friendly - no FragmentActivity requirement
- Simpler API - no casting needed
- Sufficient for simple picker UI patterns

**Cons:**
- Limited lifecycle callbacks (only onCreateView/onViewCreated)
- No automatic state persistence across configuration changes
- Fragments must use `container?.context ?: inflater.context` instead of `requireContext()`

### Decision Rationale

We chose **Option 2 (Manual Fragment Management)** because:

1. **Primary Use Case Analysis**: Address/payment pickers are typically simple, short-lived UI components that don't need complex lifecycle management
2. **Broader Ecosystem Support**: React Native and other frameworks can integrate without custom FragmentActivity implementations
3. **Pragmatic Tradeoff**: The lost functionality (full lifecycle) is rarely needed for selection screens
4. **Future Flexibility**: We can add a second API with full FragmentManager support if needed

### Fragment Compatibility Guidelines

For fragments to work with manual management:

```kotlin
class CompatibleFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ✅ Use container/inflater context
        val context = container?.context ?: inflater.context
        
        // ❌ Avoid requireContext() - may fail without FragmentManager
        // val context = requireContext()
        
        return createView(context)
    }
    
    // ✅ Simple UI creation and event handling work fine
    // ❌ Avoid complex lifecycle-dependent operations
}
```

### When Full FragmentManager May Be Needed

Consider using traditional FragmentManager approach for:

- **Complex Lifecycle Needs**: Fragments with background tasks, sensors, location services
- **State Persistence**: Multi-step forms requiring state across configuration changes  
- **Fragment Communication**: Heavy use of Fragment Result API or parent/child fragment communication
- **Animation Management**: Complex view animations tied to fragment lifecycle

For these cases, we recommend using the standard `ShopifyCheckoutSheetKit.present()` method with custom checkout logic rather than the controller approach.
