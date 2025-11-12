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
package com.shopify.checkoutsheetkit

import android.os.Looper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.LooperMode
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ThreadExtensionsTest {

    @Test
    fun `onMainThread executes immediately when already on main thread`() {
        var executed = false
        onMainThread {
            executed = true
            assertThat(Looper.myLooper()).isEqualTo(Looper.getMainLooper())
        }
        assertThat(executed).isTrue()
    }

    @Test
    fun `onMainThread posts to main looper when called from background thread`() {
        var executedOnMainThread: Boolean? = null
        val shadowLooper = ShadowLooper.shadowMainLooper()

        // Verify no tasks are initially queued
        assertThat(shadowLooper.isIdle).isTrue()

        // Create background thread and call onMainThread from it
        val thread = Thread {
            onMainThread {
                executedOnMainThread = Looper.myLooper() == Looper.getMainLooper()
            }
        }
        thread.start()
        thread.join() // Wait for thread to complete

        // Task should be queued on main looper but not yet executed
        assertThat(shadowLooper.isIdle).isFalse()
        assertThat(executedOnMainThread).isNull()

        // Run pending tasks on main looper
        shadowLooper.runToEndOfTasks()

        // Now callback should have executed on main thread
        assertThat(executedOnMainThread).isNotNull()
        assertThat(executedOnMainThread).isTrue()
        assertThat(shadowLooper.isIdle).isTrue()
    }
}
