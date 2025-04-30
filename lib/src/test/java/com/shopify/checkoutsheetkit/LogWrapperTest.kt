package com.shopify.checkoutsheetkit

import android.util.Log
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
class LogWrapperTest {
    private lateinit var log: LogWrapper

    @Before
    fun beforeEach() {
        log = LogWrapper()
        ShopifyCheckoutSheetKit.configure {
            it.logLevel = LogLevel.ERROR
        }
    }

    @After
    fun afterEach() {
        ShopifyCheckoutSheetKit.configure {
            it.logLevel = LogLevel.ERROR
        }
    }

    @Test
    fun `should emit debug logs when LogLevel is DEBUG`() {
        ShopifyCheckoutSheetKit.configure {
            it.logLevel = LogLevel.DEBUG
        }

        log.d("TAG", "Debug message")
        assertThat(ShadowLog.getLogs().any {
            it.type == Log.DEBUG && it.tag == "TAG" && it.msg == "Debug message"
        }).isTrue()
    }

    @Test
    fun `should suppress debug logs when LogLevel is WARN`() {
        ShopifyCheckoutSheetKit.configure {
            it.logLevel = LogLevel.WARN
        }

        log.d("TAG", "Debug message")
        assertThat(ShadowLog.getLogs().any {
            it.type == Log.DEBUG && it.tag == "TAG" && it.msg == "Debug message"
        }).isFalse()
    }

    @Test
    fun `should suppress debug logs when LogLevel is ERROR`() {
        ShopifyCheckoutSheetKit.configure {
            it.logLevel = LogLevel.WARN
        }

        log.d("TAG", "Debug message")
        assertThat(ShadowLog.getLogs().any {
            it.type == Log.DEBUG && it.tag == "TAG" && it.msg == "Debug message"
        }).isFalse()
    }

    @Test
    fun `should emit warn logs when LogLevel is DEBUG`() {
        ShopifyCheckoutSheetKit.configure {
            it.logLevel = LogLevel.DEBUG
        }

        log.w("TAG", "Warn message")
        assertThat(ShadowLog.getLogs().any {
            it.type == Log.WARN && it.tag == "TAG" && it.msg == "Warn message"
        }).isTrue()
    }

    @Test
    fun `should emit warn logs when LogLevel is WARN`() {
        ShopifyCheckoutSheetKit.configure {
            it.logLevel = LogLevel.WARN
        }

        log.w("TAG", "Warn message")
        assertThat(ShadowLog.getLogs().any {
            it.type == Log.WARN && it.tag == "TAG" && it.msg == "Warn message"
        }).isTrue()
    }

    @Test
    fun `should suppress warn logs when LogLevel is ERROR`() {
        ShopifyCheckoutSheetKit.configure {
            it.logLevel = LogLevel.ERROR
        }

        log.w("TAG", "Warn message")
        assertThat(ShadowLog.getLogs().any {
            it.type == Log.WARN && it.tag == "TAG" && it.msg == "Warn message"
        }).isFalse()
    }

    @Test
    fun `should emit error logs when LogLevel is DEBUG`() {
        ShopifyCheckoutSheetKit.configure {
            it.logLevel = LogLevel.DEBUG
        }

        log.e("TAG", "Error message")
        assertThat(ShadowLog.getLogs().any {
            it.type == Log.ERROR && it.tag == "TAG" && it.msg == "Error message"
        }).isTrue()
    }

    @Test
    fun `should emit error logs when LogLevel is WARN`() {
        ShopifyCheckoutSheetKit.configure {
            it.logLevel = LogLevel.WARN
        }

        log.e("TAG", "Error message")
        assertThat(ShadowLog.getLogs().any {
            it.type == Log.ERROR && it.tag == "TAG" && it.msg == "Error message"
        }).isTrue()
    }

    @Test
    fun `should emit error logs when LogLevel is ERROR`() {
        ShopifyCheckoutSheetKit.configure {
            it.logLevel = LogLevel.ERROR
        }
        
        log.e("TAG", "Error message")
        assertThat(ShadowLog.getLogs().any {
            it.type == Log.ERROR && it.tag == "TAG" && it.msg == "Error message"
        }).isTrue()
    }
}
