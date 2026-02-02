package org.kotlinbitcointools.regtesttoolbox.regenv

import org.kotlinbitcointools.regtesttoolbox.bitcoind.BitcoinClient
import org.kotlinbitcointools.regtesttoolbox.bitcoind.GetBalancesResponse
import org.kotlinbitcointools.regtesttoolbox.bitcoind.Utxo

/**
 * A high-level regtest environment wrapper that simplifies common testing operations.
 *
 * Example usage:
 * ```kotlin
 * val env = RegEnv.create(username = "regtest", password = "password")
 * val address = env.getNewAddress()
 * env.mineToAddress(101, address)  // Fund the wallet (101 blocks for maturity)
 * val txid = env.send("bcrt1q...", 1.0)
 * env.mine(1)  // Confirm the transaction (rewards go to burn address)
 * env.close()
 * ```
 */
class RegEnv private constructor(
    private val client: BitcoinClient,
    val walletName: String,
) {
    private var burnAddress: String = "bcrt1q000regtest000regtest000regtest000ferjz"

    /**
     * Mine blocks to a burn address (no wallet receives the coinbase rewards).
     * Use this to advance the chain without affecting wallet balances.
     *
     * @param blocks Number of blocks to mine (default: 1)
     * @return List of block hashes
     */
    suspend fun mine(blocks: Int = 1): List<String> {
        return client.generateBlocks(blocks, burnAddress)
    }

    /**
     * Mine blocks to a specific address.
     * Use this to fund a wallet with coinbase rewards.
     *
     * @param blocks Number of blocks to mine
     * @param address Address to receive the coinbase rewards
     * @return List of block hashes
     */
    suspend fun mineToAddress(blocks: Int, address: String): List<String> {
        return client.generateBlocks(blocks, address)
    }

    /**
     * Send funds to an address.
     *
     * @param address Recipient address
     * @param amount Amount in BTC
     * @param feeRate Fee rate in sat/vB (default: 1.0)
     * @return Transaction ID
     */
    suspend fun send(address: String, amount: Double, feeRate: Double = 1.0): String {
        return client.sendToAddress(address, amount, feeRate)
    }

    /**
     * Get the wallet's spendable balance.
     */
    suspend fun getBalance(): Double {
        return client.getBalance()
    }

    /**
     * Get detailed balance information including immature and pending funds.
     */
    suspend fun getBalances(): GetBalancesResponse {
        return client.getBalances()
    }

    /**
     * Get a new address from the wallet.
     */
    suspend fun getNewAddress(): String {
        return client.getNewAddress()
    }

    /**
     * List unspent transaction outputs.
     */
    suspend fun listUnspent(): List<Utxo> {
        return client.listUnspent()
    }

    /**
     * Get the current block height.
     */
    suspend fun getBlockCount(): Int {
        return client.getBlockCount()
    }

    /**
     * Close the underlying client connection.
     */
    fun close() {
        client.close()
    }

    companion object {
        /**
         * Create a new RegEnv with an auto-generated wallet.
         *
         * @param host Bitcoin Core RPC host (default: localhost)
         * @param port Bitcoin Core RPC port (default: 18443 for regtest)
         * @param username RPC username
         * @param password RPC password
         * @param walletName Optional wallet name (auto-generated if not provided)
         * @return A new RegEnv instance with wallet created
         */
        suspend fun create(
            host: String = "localhost",
            port: Int = 18443,
            username: String,
            password: String,
            walletName: String = "regenv_${System.currentTimeMillis()}",
        ): RegEnv {
            // First create the wallet using a client without wallet path
            val setupClient = BitcoinClient(
                host = host,
                port = port,
                username = username,
                password = password,
                walletName = null,
            )

            try {
                setupClient.createWallet(walletName)
            } finally {
                setupClient.close()
            }

            // Now create a wallet-specific client
            val walletClient = BitcoinClient(
                host = host,
                port = port,
                username = username,
                password = password,
                walletName = walletName,
            )

            return RegEnv(walletClient, walletName)
        }
    }
}
