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

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.webkit.WebChromeClient.FileChooserParams
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * For handling 3p apps that require a FileChooser / Camera in response to onShowFileChooser()
 */
class FileChooserResultContract : ActivityResultContract<FileChooserParams, Uri?>() {
    private var cameraImageUri: Uri? = null

    override fun createIntent(context: Context, input: FileChooserParams): Intent {
        val fileChooserIntent = input.createIntent()
        fileChooserIntent.addCategory(Intent.CATEGORY_OPENABLE)
        var mimeType = if (input.acceptTypes == null) DEFAULT_MIME_TYPE else input.acceptTypes[0]
        if (!ACCEPTABLE_MIME_TYPES.contains(mimeType)) {
            mimeType = DEFAULT_MIME_TYPE
        }
        fileChooserIntent.setType(mimeType)

        val photoFile = createImageFile(context)
        cameraImageUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)  // Ensures URI can be written
        }

        val chooserIntent = Intent.createChooser(fileChooserIntent, context.getText(R.string.filechooser_title))
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
        return chooserIntent
    }

    override fun parseResult(resultCode: Int, intent: Intent?) : Uri? {
        return if (resultCode == RESULT_OK) {
            intent?.data ?: cameraImageUri // Return the image URI captured by the camera
        } else {
            null
        }
    }

    private fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat(DATE_FORMAT_PATTERN, Locale.US).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("$IMG_FILE_PREFIX${timeStamp}_", IMG_FILE_SUFFIX, storageDir)
    }

    companion object {
        private val ACCEPTABLE_MIME_TYPES = arrayListOf("image/*", "video/*")
        private const val DEFAULT_MIME_TYPE = "*/*"
        private const val DATE_FORMAT_PATTERN = "yyyyMMdd_HHmmss"
        private const val IMG_FILE_PREFIX = "JPEG_"
        private const val IMG_FILE_SUFFIX = ".jpg"
    }
}
