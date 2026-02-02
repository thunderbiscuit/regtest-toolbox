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

/**
 * A client for interacting with Bitcoin Core via JSON-RPC.
 *
 * Provides methods for common Bitcoin Core operations including block generation,
 * wallet management, and transaction handling. Designed primarily for regtest environments.
 *
 * @param host Bitcoin Core RPC host (default: localhost)
 * @param port Bitcoin Core RPC port (default: 18443 for regtest)
 * @param username RPC authentication username
 * @param password RPC authentication password
 * @param walletName Optional wallet name for wallet-specific operations. If provided,
 *                   wallet operations will use the `/wallet/<name>` endpoint.
 *
 * @sample
 * ```kotlin
 * val client = BitcoinClient(
 *     username = "regtest",
 *     password = "password",
 *     walletName = "mywallet"
 * )
 * val address = client.getNewAddress()
 * client.generateBlocks(101, address)
 * client.close()
 * ```
 */
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

    /**
     * Generate (mine) blocks to the specified address.
     *
     * @param numBlocks Number of blocks to generate
     * @param address Address to receive the coinbase rewards
     * @return List of generated block hashes
     * @throws Exception if the RPC call fails
     */
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

    /**
     * Get the current block count (chain height).
     *
     * @return The height of the most-work fully-validated chain
     * @throws Exception if the RPC call fails
     */
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

    /**
     * Create a new wallet.
     *
     * @param name The name for the new wallet
     * @return Response containing the wallet name and any warnings
     * @throws Exception if wallet creation fails (e.g., wallet already exists)
     */
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

    /**
     * Generate a new address for receiving payments.
     *
     * @return A new Bitcoin address from the wallet
     * @throws Exception if the RPC call fails or no wallet is loaded
     */
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

    /**
     * Send an amount to a given address.
     *
     * @param address The recipient Bitcoin address
     * @param amount The amount to send in BTC
     * @param feeRate Fee rate in sat/vB (default: 1.0). Set to null to use Bitcoin Core's fee estimation.
     * @return The transaction ID of the sent transaction
     * @throws Exception if sending fails (e.g., insufficient funds)
     */
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

    /**
     * Get the wallet's total spendable balance.
     *
     * @return The total confirmed balance in BTC
     * @throws Exception if the RPC call fails
     */
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

    /**
     * Get detailed balance information including trusted, pending, and immature funds.
     *
     * @return Detailed balance breakdown by category
     * @throws Exception if the RPC call fails
     */
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

    /**
     * Rescan the blockchain for wallet transactions.
     *
     * This can be useful after importing keys or if the wallet state is out of sync.
     *
     * @param startHeight Optional block height to start rescanning from (default: genesis)
     * @throws Exception if the RPC call fails
     */
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

    /**
     * Send funds using the newer `send` RPC with explicit fee rate.
     *
     * This is an alternative to [sendToAddress] that uses the `send` RPC method,
     * which provides more control over transaction construction.
     *
     * @param address The recipient Bitcoin address
     * @param amount The amount to send in BTC
     * @param feeRate Fee rate in sat/vB
     * @return The transaction ID of the sent transaction
     * @throws Exception if sending fails
     */
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

    /**
     * Get information about the loaded wallet.
     *
     * @return Wallet information including name, version, balance, and transaction count
     * @throws Exception if the RPC call fails
     */
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

    /**
     * Get information about a Bitcoin address.
     *
     * @param address The address to query
     * @return Address information including ownership and solvability
     * @throws Exception if the RPC call fails
     */
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

    /**
     * List unspent transaction outputs (UTXOs) in the wallet.
     *
     * @return List of UTXOs with their details (txid, vout, address, amount, confirmations)
     * @throws Exception if the RPC call fails
     */
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

    /**
     * Close the HTTP client and release resources.
     *
     * Should be called when the client is no longer needed.
     */
    fun close() {
        client.close()
    }
}
