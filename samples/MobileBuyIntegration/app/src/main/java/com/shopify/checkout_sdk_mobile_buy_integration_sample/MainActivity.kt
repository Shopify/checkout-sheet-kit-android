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
package com.shopify.checkout_sdk_mobile_buy_integration_sample

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient.FileChooserParams
import android.webkit.WebView.setWebContentsDebuggingEnabled
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import timber.log.Timber
import timber.log.Timber.DebugTree


class MainActivity : ComponentActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var showFileChooserLauncher: ActivityResultLauncher<FileChooserParams>

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var fileChooserParams: FileChooserParams? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWebContentsDebuggingEnabled(true)
        setContent {
            CheckoutSdkApp()
        }
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            val fileChooserParams = this.fileChooserParams
            if (isGranted && fileChooserParams != null) {
                this.showFileChooserLauncher.launch(fileChooserParams)
                this.fileChooserParams = null
            }
            // N.B. a file chooser intent (without camera) could be launched here if the permission was denied
        }
        showFileChooserLauncher = registerForActivityResult(FileChooserResultContract()) { uri: Uri? ->
            filePathCallback?.onReceiveValue(if (uri != null) arrayOf(uri) else null)
            filePathCallback = null
        }

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

    // Show a file chooser when prompted by the event processor
    fun onShowFileChooser(filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams): Boolean {
        this.filePathCallback = filePathCallback
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Permissions not yet granted, request before launching chooser
            this.fileChooserParams = fileChooserParams
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            // Permissions already granted, launch chooser
            showFileChooserLauncher.launch(fileChooserParams)
            this.fileChooserParams = null
        }
        return true
    }
}
