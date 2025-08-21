plugins {
    id("checkout.android.library")
    id("checkout.publishing")
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    implementation(libs.findLibrary("kotlinx-serialization-json").get())
    implementation(libs.findLibrary("androidx-appcompat").get())
    implementation(libs.findLibrary("androidx-webkit").get())
}

publishing {
    publications {
        named<MavenPublication>("release") {
            pom {
                name.set("CheckoutSheetKit")
                description.set("Shopify's Checkout Sheet Kit makes it simple to render checkouts inside your mobile app.")
                artifactId = "checkout-sheet-kit"
            }
        }
    }
}
