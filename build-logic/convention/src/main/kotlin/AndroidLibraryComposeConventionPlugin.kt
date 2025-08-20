/*
 * MIT License
 *
 * Copyright 2023-present, Shopify Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Convention plugin for Android library modules that use Jetpack Compose.
 * 
 * Extends the base Android library configuration with Compose-specific setup:
 * - Inherits all base Android library settings via checkout.android.library plugin
 * - Enables Jetpack Compose compiler and build features
 * - Adds Compose BOM for dependency alignment
 * - Includes core Compose dependencies (runtime, activity-compose)
 * - Configures Compose-specific test dependencies (compose-ui-test-junit4)
 * - Sets compose-specific namespace
 * 
 * Apply this plugin to Android library modules that provide Compose UI components.
 */
class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                // Inherit all base Android library configuration (detekt, lint, tests, etc.)
                apply("checkout.android.library")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<LibraryExtension> {
                namespace = "com.shopify.checkoutsheetkit.compose"

                buildFeatures {
                    compose = true
                }
            }

            configureDependencies()
        }
    }

    private fun Project.configureDependencies() {
        dependencies {
            val bom = libs.findLibrary("compose-bom").get()
            add("implementation", platform(bom))
            add("implementation", libs.findLibrary("compose-runtime").get())
            add("implementation", libs.findLibrary("activity-compose").get())

            // Test dependencies specific to Compose
            add("testImplementation", libs.findLibrary("compose-ui-test-junit4").get())
            add("androidTestImplementation", platform(bom))
            add("androidTestImplementation", libs.findLibrary("compose-ui-test-junit4").get())
        }
    }
}
