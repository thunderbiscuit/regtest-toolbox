/*
 * Copyright 2026 Kotlin Bitcoin Tools contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.kotlinbitcointools.regtesttoolbox.bitcoind

import kotlinx.serialization.Serializable

typealias BlockHashes = List<String>
typealias BlockHeight = Int
typealias Address = String
typealias Txid = String
typealias Balance = Double
typealias ListUnspentResponse = List<Utxo>

@Serializable
data class CreateWalletResponse(
    val name: String,
    val warning: String = "",
)

@Serializable
data class SendResponse(
    val txid: String,
    val complete: Boolean,
)

@Serializable
data class MineBalances(
    val trusted: Double,
    val untrusted_pending: Double,
    val immature: Double,
)

@Serializable
data class LastProcessedBlock(
    val hash: String,
    val height: Int,
)

@Serializable
data class GetBalancesResponse(
    val mine: MineBalances,
    val lastprocessedblock: LastProcessedBlock,
)

@Serializable
data class GetWalletInfoResponse(
    val walletname: String,
    val walletversion: Int,
    val balance: Double,
    val txcount: Int,
)

@Serializable
data class GetAddressInfoResponse(
    val address: String,
    val ismine: Boolean,
    val iswatchonly: Boolean,
    val solvable: Boolean,
    val ischange: Boolean = false,
)

@Serializable
data class Utxo(
    val txid: String,
    val vout: Int,
    val address: String,
    val amount: Double,
    val confirmations: Int,
)
