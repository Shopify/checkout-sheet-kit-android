package com.shopify.checkout_sdk_sample.data.source.network.adapters

import com.apollographql.apollo.api.Adapter
import com.apollographql.apollo.api.CustomScalarAdapters
import com.apollographql.apollo.api.json.JsonReader
import com.apollographql.apollo.api.json.JsonWriter
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL

/**
 * Adapter for GraphQL Scalar URL to java.net.URL
 */
class URLAdapter : Adapter<URL> {
    override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): URL {
        val urlString = reader.nextString()
        return try {
            URI(urlString!!).toURL()
        } catch (e: URISyntaxException) {
            throw IllegalStateException("Invalid URI format: $urlString", e)
        } catch (e: MalformedURLException) {
            throw IllegalStateException("Invalid URL format: $urlString", e)
        }
    }

    override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: URL) {
        writer.value(value.toString())
    }
}
