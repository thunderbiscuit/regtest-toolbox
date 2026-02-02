/*
 * Copyright 2026 Kotlin Bitcoin Tools contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.kotlinbitcointools.regtesttoolbox.bitcoind

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
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

class BitcoinClient(
    host: String = "localhost",
    port: Int = 18443,
    private val username: String,
    private val password: String,
    private val walletName: String? = null,
) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = false
                }
            )
        }
    }

    private val baseUrl = "http://$host:$port"
    private val walletUrl = walletName?.let { "$baseUrl/wallet/$it" } ?: baseUrl

    suspend fun generateBlocks(numBlocks: Int, address: String): BlockHashes {
        val request = RpcRequest(
            method = "generatetoaddress",
            params = listOf(
                JsonPrimitive(numBlocks),
                JsonPrimitive(address)
            )
        )

        val response = client.post(walletUrl) {
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

    suspend fun createWallet(name: String): CreateWalletResponse {
        val request = RpcRequest(
            method = "createwallet",
            params = listOf(JsonPrimitive(name))
        )

        val response = client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            basicAuth(username, password)
            setBody(request)
        }

        val rpcResponse: RpcResponse<CreateWalletResponse> = response.body()
        return rpcResponse.result ?: throw Exception("Failed to create wallet: ${rpcResponse.error}")
    }

    suspend fun getNewAddress(): Address {
        val request = RpcRequest(
            method = "getnewaddress",
            params = emptyList()
        )

        val response = client.post(walletUrl) {
            contentType(ContentType.Application.Json)
            basicAuth(username, password)
            setBody(request)
        }

        val rpcResponse: RpcResponse<Address> = response.body()
        return rpcResponse.result ?: throw Exception("Failed to get new address: ${rpcResponse.error}")
    }

    suspend fun sendToAddress(address: String, amount: Double, feeRate: Double? = 1.0): Txid {
        val params = buildList {
            add(JsonPrimitive(address))
            add(JsonPrimitive(amount))
            if (feeRate != null) {
                // Parameters 3-9 must be null to reach fee_rate (parameter 10)
                // comment, comment_to, subtractfeefromamount, replaceable, conf_target, estimate_mode, avoid_reuse
                repeat(7) { add(JsonNull) }
                add(JsonPrimitive(feeRate))
            }
        }

        val request = RpcRequest(
            method = "sendtoaddress",
            params = params
        )

        val response = client.post(walletUrl) {
            contentType(ContentType.Application.Json)
            basicAuth(username, password)
            setBody(request)
        }

        val rpcResponse: RpcResponse<Txid> = response.body()
        return rpcResponse.result ?: throw Exception("Failed to send to address: ${rpcResponse.error}")
    }

    suspend fun getBalance(): Balance {
        val request = RpcRequest(
            method = "getbalance",
            params = emptyList()
        )

        val response = client.post(walletUrl) {
            contentType(ContentType.Application.Json)
            basicAuth(username, password)
            setBody(request)
        }

        val rpcResponse: RpcResponse<Balance> = response.body()
        return rpcResponse.result ?: throw Exception("Failed to get balance: ${rpcResponse.error}")
    }

    suspend fun getBalances(): GetBalancesResponse {
        val request = RpcRequest(
            method = "getbalances",
            params = emptyList()
        )

        val response = client.post(walletUrl) {
            contentType(ContentType.Application.Json)
            basicAuth(username, password)
            setBody(request)
        }

        val rpcResponse: RpcResponse<GetBalancesResponse> = response.body()
        return rpcResponse.result ?: throw Exception("Failed to get balances: ${rpcResponse.error}")
    }

    suspend fun rescanBlockchain(startHeight: Int? = null) {
        val params = if (startHeight != null) {
            listOf(JsonPrimitive(startHeight))
        } else {
            emptyList()
        }

        val request = RpcRequest(
            method = "rescanblockchain",
            params = params
        )

        val response = client.post(walletUrl) {
            contentType(ContentType.Application.Json)
            basicAuth(username, password)
            setBody(request)
        }

        // Just check for errors, we don't need the response data
        val rpcResponse: RpcResponse<Map<String, Int>> = response.body()
        if (rpcResponse.error != null) {
            throw Exception("Failed to rescan blockchain: ${rpcResponse.error}")
        }
    }

    suspend fun send(address: String, amount: Double, feeRate: Double): Txid {
        val outputs = buildJsonArray {
            add(
                buildJsonObject {
                    put(address, JsonPrimitive(amount))
                }
            )
        }

        val request = RpcRequest(
            method = "send",
            params = listOf(
                outputs,
                JsonNull,
                JsonNull,
                JsonPrimitive(feeRate)
            )
        )

        val response = client.post(walletUrl) {
            contentType(ContentType.Application.Json)
            basicAuth(username, password)
            setBody(request)
        }

        val rpcResponse: RpcResponse<SendResponse> = response.body()
        return rpcResponse.result?.txid ?: throw Exception("Failed to send: ${rpcResponse.error}")
    }

    suspend fun getWalletInfo(): GetWalletInfoResponse {
        val request = RpcRequest(
            method = "getwalletinfo",
            params = emptyList()
        )

        val response = client.post(walletUrl) {
            contentType(ContentType.Application.Json)
            basicAuth(username, password)
            setBody(request)
        }

        val rpcResponse: RpcResponse<GetWalletInfoResponse> = response.body()
        return rpcResponse.result ?: throw Exception("Failed to get wallet info: ${rpcResponse.error}")
    }

    suspend fun getAddressInfo(address: String): GetAddressInfoResponse {
        val request = RpcRequest(
            method = "getaddressinfo",
            params = listOf(JsonPrimitive(address))
        )

        val response = client.post(walletUrl) {
            contentType(ContentType.Application.Json)
            basicAuth(username, password)
            setBody(request)
        }

        val rpcResponse: RpcResponse<GetAddressInfoResponse> = response.body()
        return rpcResponse.result ?: throw Exception("Failed to get address info: ${rpcResponse.error}")
    }

    suspend fun listUnspent(): ListUnspentResponse {
        val request = RpcRequest(
            method = "listunspent",
            params = emptyList()
        )

        val response = client.post(walletUrl) {
            contentType(ContentType.Application.Json)
            basicAuth(username, password)
            setBody(request)
        }

        val rpcResponse: RpcResponse<ListUnspentResponse> = response.body()
        return rpcResponse.result ?: throw Exception("Failed to list unspent: ${rpcResponse.error}")
    }

    fun close() {
        client.close()
    }
}
