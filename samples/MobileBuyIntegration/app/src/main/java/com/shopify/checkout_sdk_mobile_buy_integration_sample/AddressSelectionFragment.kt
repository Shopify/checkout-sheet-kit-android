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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * Demo fragment for address selection in the controller navigation flow.
 * In a real app, this would show a proper address picker UI.
 */
class AddressSelectionFragment : Fragment() {
    
    var onAddressSelected: ((ClientDefinedAddress) -> Unit)? = null
    var onCancel: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        
        // Create a simple linear layout programmatically for demo
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            setBackgroundColor(0xFFFFFFFF.toInt())
            
            // Subtitle
            addView(TextView(context).apply {
                text = "Choose from these demo addresses:"
                textSize = 16f
                setPadding(0, 0, 0, 24)
            })
            
            // Address options
            val addresses = listOf(
                ClientDefinedAddress(
                    firstName = "John",
                    lastName = "Doe", 
                    address1 = "123 Main Street",
                    city = "Toronto",
                    province = "ON",
                    country = "CA",
                    zip = "M5V 3A8"
                ),
                ClientDefinedAddress(
                    firstName = "Jane",
                    lastName = "Smith",
                    address1 = "456 Oak Avenue",
                    city = "Vancouver", 
                    province = "BC",
                    country = "CA",
                    zip = "V6B 1A1"
                ),
                ClientDefinedAddress(
                    firstName = "Bob",
                    lastName = "Wilson",
                    address1 = "789 Pine Road",
                    city = "Calgary",
                    province = "AB", 
                    country = "CA",
                    zip = "T2P 1J9"
                )
            )
            
            addresses.forEach { address ->
                addView(Button(context).apply {
                    text = "${address.firstName} ${address.lastName}\n${address.address1}\n${address.city}, ${address.province} ${address.zip}"
                    setPadding(24, 24, 24, 24)
                    
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.setMargins(0, 0, 0, 16)
                    this.layoutParams = layoutParams
                    
                    setOnClickListener {
                        onAddressSelected?.invoke(address)
                    }
                })
            }
            
            // Cancel button
            addView(Button(context).apply {
                text = "Cancel"
                setBackgroundColor(0xFFCCCCCC.toInt())
                
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(0, 32, 0, 0)
                this.layoutParams = layoutParams
                
                setOnClickListener {
                    onCancel?.invoke()
                }
            })
        }
    }
}
