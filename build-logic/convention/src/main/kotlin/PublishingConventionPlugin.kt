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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.plugins.signing.SigningExtension

/**
 * Convention plugin for Maven Central publishing configuration.
 * 
 * Sets up standardized publishing for library modules to be released on Maven Central:
 * - Configures maven-publish plugin with release publication
 * - Sets up POM metadata (licenses, developers, SCM information)
 * - Configures Sonatype OSSRH staging repository 
 * - Handles GPG signing for published artifacts (optional based on properties)
 * - Uses environment variables for repository credentials (OSSRH_USERNAME, OSSRH_PASSWORD)
 * - Supports in-memory PGP key signing with signingKeyId, signingKey, signingPassword properties
 * 
 * Apply this plugin to library modules that should be published to Maven Central.
 * Requires proper signing configuration and repository credentials for releases.
 */
class PublishingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("maven-publish")
                apply("signing")
            }

            val versionName = libs.findVersion("checkoutSheetKit").get().requiredVersion

            extensions.configure<PublishingExtension> {
                publications {
                    create("release", MavenPublication::class.java) {
                        pom {
                            url.set("https://github.com/Shopify/checkout-sheet-kit-android")
                            groupId = "com.shopify"
                            version = versionName

                            licenses {
                                license {
                                    name.set("MIT")
                                    url.set("https://opensource.org/licenses/MIT")
                                }
                            }

                            developers {
                                developer {
                                    name.set("Shopify Inc.")
                                }
                            }

                            scm {
                                connection.set("https://github.com/Shopify/checkout-sheet-kit-android.git")
                                developerConnection.set("https://github.com/Shopify/checkout-sheet-kit-android.git")
                                url.set("https://github.com/Shopify/checkout-sheet-kit-android.git")
                            }
                        }

                        afterEvaluate {
                            from(components.findByName("release"))
                        }
                    }
                }

                repositories {
                    maven {
                        name = "ossrh-staging-api"
                        url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                        credentials {
                            username = System.getenv("OSSRH_USERNAME")
                            password = System.getenv("OSSRH_PASSWORD")
                        }
                    }
                }
            }

            extensions.configure<SigningExtension> {
                val signingKeyId = findProperty("signingKeyId") as String?
                val signingKey = findProperty("signingKey") as String?
                val signingPassword = findProperty("signingPassword") as String?

                // Only configure signing if all required properties are present
                if (signingKeyId != null && signingKey != null && signingPassword != null) {
                    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
                    val publishing = extensions.getByType(PublishingExtension::class.java)
                    sign(publishing.publications)
                }
            }
        }
    }
}
