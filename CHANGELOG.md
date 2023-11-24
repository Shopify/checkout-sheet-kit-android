# Changelog

## 0.3.0 - November 24, 2023

- **Breaking:** Allow setting sRGB colors as well as color resource IDs in ColorSchemes.

  ```kotlin
    // Previously
    webViewBackground = Color(R.color.checkoutDarkBg)

    // Now
    webViewBackground = Color.ResourceId(R.color.checkoutDarkBg)
    // Or
    webViewBackground = Color.SRGB(-0xff0001)
  ```
  
- Implemented onRenderProcessGone to prevent app crashes when the render process is killed to reclaim memory
- JavaDoc improvements
- Add function to retrieve the currently applied ShopifyCheckoutKit configuration

Note: breaking changes may be made during Dev Preview with minor version increments, but will receive a major version increments 
after release for General Availability.
