/*
 * Copyright 2026 Kotlin Bitcoin Tools contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.kotlinbitcointools.regtesttoolbox.bitcoind

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A JSON-RPC request to Bitcoin Core.
 *
 * @property jsonrpc JSON-RPC protocol version (always "1.0" for Bitcoin Core)
 * @property id Request identifier for matching responses
 * @property method The RPC method name (e.g., "getblockcount", "sendtoaddress")
 * @property params Method parameters as JSON elements
 */
@Serializable
data class RpcRequest(
    val jsonrpc: String = "1.0",
    val id: String = "kotlin-client",
    val method: String,
    val params: List<JsonElement>,
)

/**
 * A JSON-RPC response from Bitcoin Core.
 *
 * @param T The expected result type
 * @property result The successful result, or null if an error occurred
 * @property error Error details if the request failed, or null on success
 * @property id The request identifier echoed back
 */
@Serializable
data class RpcResponse<T>(
    val result: T? = null,
    val error: RpcError? = null,
    val id: String? = null,
)

/**
 * An error returned by Bitcoin Core RPC.
 *
 * @property code Numeric error code (e.g., -32600 for invalid request, -6 for insufficient funds)
 * @property message Human-readable error description
 */
@Serializable
data class RpcError(
    val code: Int,
    val message: String,
)
