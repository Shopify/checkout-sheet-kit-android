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
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

/**
 * Convention plugin for Android library modules.
 * 
 * Provides common configuration for all Android library modules including:
 * - Android library plugin setup with standardized SDK versions
 * - Kotlin compilation settings with explicit API mode for non-test code
 * - Detekt static analysis configuration
 * - Common test dependencies (JUnit, Robolectric, Mockito, AssertJ)
 * - Lint configuration with warnings as errors
 * - Publishing configuration for sources and javadoc JARs
 * - Test options with resource inclusion and detailed logging
 * 
 * Apply this plugin to Android library modules that don't use Jetpack Compose.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("io.gitlab.arturbosch.detekt")
            }

            extensions.configure<LibraryExtension> {
                namespace = "com.shopify.checkoutsheetkit"
                compileSdk = libs.findVersion("compileSdk").get().requiredVersion.toInt()

                defaultConfig {
                    minSdk = libs.findVersion("minSdk").get().requiredVersion.toInt()

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

                    val versionName = libs.findVersion("checkoutSheetKit").get().requiredVersion
                    buildConfigField("String", "SDK_VERSION", "\"$versionName\"")
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_1_8
                    targetCompatibility = JavaVersion.VERSION_1_8
                }

                buildFeatures {
                    buildConfig = true
                }

                lint {
                    targetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()
                    checkDependencies = true
                    warningsAsErrors = true
                    baseline = file("lint-baseline.xml")
                }

                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = false
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt")
                        )
                        consumerProguardFiles("proguard-rules.pro")
                    }
                }

                testOptions {
                    unitTests.isIncludeAndroidResources = true
                    unitTests.all {
                        it.testLogging {
                            events("passed", "skipped", "failed", "standardOut", "standardError")
                            it.outputs.upToDateWhen { false }
                            showStandardStreams = true
                        }
                    }
                }

                publishing {
                    singleVariant("release") {
                        withSourcesJar()
                        withJavadocJar()
                    }
                }
            }

            tasks.withType(KotlinJvmCompile::class.java).configureEach {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                    if (!name.contains("Test")) {
                        freeCompilerArgs.add("-Xexplicit-api=strict")
                    }
                }
            }

            configureDependencies()
            configureDetekt()
        }
    }

    private fun Project.configureDependencies() {
        dependencies {
            // Common test dependencies for all Android library modules
            add("testImplementation", libs.findBundle("test-common").get())
            add("androidTestImplementation", libs.findBundle("androidtest-common").get())
        }
    }

    private fun Project.configureDetekt() {
        extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
            buildUponDefaultConfig = true
            config.setFrom("$rootDir/detekt.config.yml")
        }
    }
}
