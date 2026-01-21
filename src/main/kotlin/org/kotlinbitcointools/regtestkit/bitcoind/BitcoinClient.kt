/*
 * Copyright 2026 Kotlin Bitcoin Tools contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.kotlinbitcointools.regtestkit.bitcoind

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.basicAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

class BitcoinClient(
    host: String = "localhost",
    port: Int = 18443,
    private val username: String,
    private val password: String,
) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = false
                    isLenient = false
                }
            )
        }
    }

    private val baseUrl = "http://$host:$port"

    suspend fun generateBlocks(numBlocks: Int, address: String): BlockHashes {
        val request = RpcRequest(
            method = "generatetoaddress",
            params = listOf(
                JsonPrimitive(numBlocks),
                JsonPrimitive(address)
            )
        )

        val response = client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            basicAuth(username, password)
            setBody(request)
        }

        val rpcResponse: RpcResponse<BlockHashes> = response.body()
        return rpcResponse.result ?: throw Exception("Failed to generate blocks: ${rpcResponse.error}")
    }

    suspend fun getBlockCount(): BlockHeight {
        val request = RpcRequest(
            method = "getblockcount",
            params = emptyList()
        )

        val response = client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            basicAuth(username, password)
            setBody(request)
        }

        val rpcResponse: RpcResponse<BlockHeight> = response.body()
        return rpcResponse.result ?: throw Exception("Failed to get block count: ${rpcResponse.error}")
    }

    fun close() {
        client.close()
    }
}
