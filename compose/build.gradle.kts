plugins {
    id("checkout.android.library.compose")
    id("checkout.publishing")
}

dependencies {
    // Core Checkout Sheet Kit dependency
    api(project(":lib"))
}

publishing {
    publications {
        named<MavenPublication>("release") {
            pom {
                name.set("CheckoutSheetKit Compose")
                description.set("Jetpack Compose wrapper for Shopify's Checkout Sheet Kit")
                artifactId = "checkout-sheet-kit-compose"
            }
        }
    }
}

