package org.kotlinbitcointools.regtestkit.bitcoind

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RpcRequest(
    val jsonrpc: String = "1.0",
    val id: String = "kotlin-client",
    val method: String,
    val params: List<JsonElement>
)

@Serializable
data class RpcResponse<T>(
    val result: T? = null,
    val error: RpcError? = null,
    val id: String? = null
)

@Serializable
data class RpcError(
    val code: Int,
    val message: String
)
