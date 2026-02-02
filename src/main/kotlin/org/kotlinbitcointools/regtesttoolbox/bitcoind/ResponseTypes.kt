/*
 * Copyright 2026 Kotlin Bitcoin Tools contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.kotlinbitcointools.regtesttoolbox.bitcoind

import kotlinx.serialization.Serializable

/** List of block hashes as hex strings. */
typealias BlockHashes = List<String>

/** Block height as an integer. */
typealias BlockHeight = Int

/** Bitcoin address as a string. */
typealias Address = String

/** Transaction ID as a 64-character hex string. */
typealias Txid = String

/** Balance in BTC as a decimal. */
typealias Balance = Double

/** List of unspent transaction outputs. */
typealias ListUnspentResponse = List<Utxo>

/**
 * Response from the `createwallet` RPC.
 *
 * @property name The name of the created wallet
 * @property warning Any warnings generated during wallet creation
 */
@Serializable
data class CreateWalletResponse(
    val name: String,
    val warning: String = "",
)

/**
 * Response from the `send` RPC.
 *
 * @property txid The transaction ID of the sent transaction
 * @property complete Whether the transaction is complete (fully signed)
 */
@Serializable
data class SendResponse(
    val txid: String,
    val complete: Boolean,
)

/**
 * Balance breakdown for wallet-owned funds.
 *
 * @property trusted Confirmed spendable balance (sufficient confirmations)
 * @property untrusted_pending Unconfirmed balance from external transactions
 * @property immature Coinbase rewards not yet mature (need 100 confirmations)
 */
@Serializable
data class MineBalances(
    val trusted: Double,
    val untrusted_pending: Double,
    val immature: Double,
)

/**
 * Information about the last block processed by the wallet.
 *
 * @property hash Block hash as hex string
 * @property height Block height
 */
@Serializable
data class LastProcessedBlock(
    val hash: String,
    val height: Int,
)

/**
 * Response from the `getbalances` RPC with detailed balance information.
 *
 * @property mine Balance breakdown for wallet-owned funds
 * @property lastprocessedblock The last block the wallet has synced to
 */
@Serializable
data class GetBalancesResponse(
    val mine: MineBalances,
    val lastprocessedblock: LastProcessedBlock,
)

/**
 * Response from the `getwalletinfo` RPC.
 *
 * @property walletname The wallet name
 * @property walletversion The wallet version
 * @property balance The total confirmed balance in BTC
 * @property txcount Total number of transactions in the wallet
 */
@Serializable
data class GetWalletInfoResponse(
    val walletname: String,
    val walletversion: Int,
    val balance: Double,
    val txcount: Int,
)

/**
 * Response from the `getaddressinfo` RPC.
 *
 * @property address The address being queried
 * @property ismine Whether the address belongs to this wallet
 * @property iswatchonly Whether the address is watch-only
 * @property solvable Whether the wallet can sign for this address
 * @property ischange Whether this is a change address
 */
@Serializable
data class GetAddressInfoResponse(
    val address: String,
    val ismine: Boolean,
    val iswatchonly: Boolean,
    val solvable: Boolean,
    val ischange: Boolean = false,
)

/**
 * An unspent transaction output (UTXO).
 *
 * @property txid Transaction ID containing this output
 * @property vout Output index within the transaction
 * @property address The address holding this output
 * @property amount Value in BTC
 * @property confirmations Number of confirmations
 */
@Serializable
data class Utxo(
    val txid: String,
    val vout: Int,
    val address: String,
    val amount: Double,
    val confirmations: Int,
)
