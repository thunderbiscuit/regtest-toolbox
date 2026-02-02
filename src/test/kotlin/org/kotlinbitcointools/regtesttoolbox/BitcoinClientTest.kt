/*
 * Copyright 2026 Kotlin Bitcoin Tools contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.kotlinbitcointools.regtesttoolbox

import kotlinx.coroutines.runBlocking
import org.kotlinbitcointools.regtesttoolbox.bitcoind.BitcoinClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BitcoinClientTest {
    @Test
    fun testGenerateBlocks() {
        runBlocking {
            val client = BitcoinClient(
                host = "localhost",
                port = 18443,
                username = "regtest",
                password = "password"
            )

            try {
                val address = "bcrt1q6gau5mg4ceupfhtyywyaj5ge45vgptvawgg3aq"
                val blockHashes = client.generateBlocks(3, address)

                println("Generated ${blockHashes.size} blocks")
                blockHashes.forEach { hash ->
                    println("Block hash: $hash")
                }

                assertEquals(blockHashes.size, 3, "Should generate 3 blocks")
            } finally {
                client.close()
            }
        }
    }

    @Test
    fun testGetBlockCount() {
        runBlocking {
            val client = BitcoinClient(
                host = "localhost",
                port = 18443,
                username = "regtest",
                password = "password"
            )

            try {
                val blockCount = client.getBlockCount()
                println("Current block count: $blockCount")
                assertTrue(blockCount >= 0, "Block count should be non-negative")
            } finally {
                client.close()
            }
        }
    }

    @Test
    fun testSendCoins() {
        runBlocking {
            // Create a wallet with a unique name
            val walletName = "testwallet_${System.currentTimeMillis()}"
            //
            // val client = BitcoinClient(
            //     host = "localhost",
            //     port = 18443,
            //     username = "regtest",
            //     password = "password",
            //     walletName = walletName,
            // )
            //
            // val walletResponse = client.createWallet(walletName)
            // println("Created wallet: ${walletResponse.name}")
            // assertEquals(walletName, walletResponse.name, "Wallet name should match")
            // client.close()

            // Create a wallet-specific client for wallet operations
            val walletClient = BitcoinClient(
                host = "localhost",
                port = 18443,
                username = "regtest",
                password = "password",
                walletName = walletName
            )
            val walletResponse = walletClient.createWallet(walletName)
            println("Created wallet: ${walletResponse.name}")
            assertEquals(walletName, walletResponse.name, "Wallet name should match")

            try {
                // Get a new address from the wallet
                val address = walletClient.getNewAddress()
                println("Generated address: $address")
                assertTrue(address.isNotEmpty(), "Address should not be empty")

                // Mine blocks to fund the wallet (need 101 blocks for coinbase maturity)
                val blockHashes = walletClient.generateBlocks(101, address)
                println("Mined ${blockHashes.size} blocks to fund wallet")
                assertEquals(101, blockHashes.size, "Should generate 101 blocks")

                // Check balance
                val balance = walletClient.getBalance()
                println("Wallet balance: $balance BTC")
                assertTrue(balance > 0, "Balance should be positive after mining")

                // Send coins to another address
                val recipientAddress = "bcrt1q6gau5mg4ceupfhtyywyaj5ge45vgptvawgg3aq"
                val txid = walletClient.sendToAddress(recipientAddress, 0.0001, 2.0)
                println("Sent 0.0001 BTC, txid: $txid")
                assertTrue(txid.isNotEmpty(), "Transaction ID should not be empty")
                assertEquals(64, txid.length, "Txid should be 64 characters (hex)")
            } finally {
                walletClient.close()
            }
        }
    }

    /**
     * Tests wallet creation, address generation, mining, and sending coins with a specific fee rate.
     *
     * NOTE: This test requires a fresh regtest chain (< 4950 blocks) to have non-zero block rewards.
     * On regtest, halvings occur every 150 blocks, so after ~33 halvings (4950 blocks) the
     * coinbase reward becomes effectively 0.
     */
    @Test
    fun testSendWithFeeRate() {
        runBlocking {
            val walletName = "testwallet_feerate_${System.currentTimeMillis()}"

            val client = BitcoinClient(
                host = "localhost",
                port = 18443,
                username = "regtest",
                password = "password",
                walletName = walletName,
            )

            try {
                // Check if chain is too long for mining rewards
                // On regtest, halvings occur every 150 blocks; after ~4950 blocks rewards are 0
                val blockCount = client.getBlockCount()
                if (blockCount > 4950) {
                    println("SKIPPED: Chain has $blockCount blocks; rewards exhausted")
                    return@runBlocking
                }

                // Create wallet
                val walletResponse = client.createWallet(walletName)
                println("Created wallet: ${walletResponse.name}")
                assertEquals(walletName, walletResponse.name)

                // Get a new address
                val address = client.getNewAddress()
                println("Generated address: $address")
                assertTrue(address.isNotEmpty())

                // Mine blocks to fund the wallet (101 blocks for coinbase maturity)
                val blockHashes = client.generateBlocks(101, address)
                println("Mined ${blockHashes.size} blocks")
                assertEquals(101, blockHashes.size)

                // Check balance
                val balance = client.getBalance()
                println("Wallet balance: $balance BTC")
                assertTrue(balance > 0, "Balance should be positive after mining")

                // Send coins with a specific fee rate (4.0 sat/vB)
                val recipientAddress = "bcrt1q6gau5mg4ceupfhtyywyaj5ge45vgptvawgg3aq"
                val feeRate = 4.0
                val txid = client.send(recipientAddress, 1.0, feeRate)
                println("Sent 1.0 BTC with fee rate $feeRate sat/vB, txid: $txid")
                assertTrue(txid.isNotEmpty())
                assertEquals(64, txid.length, "Txid should be 64 hex characters")
            } finally {
                client.close()
            }
        }
    }
}
