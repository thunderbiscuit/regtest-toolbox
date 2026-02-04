/*
 * Copyright 2026 Kotlin Bitcoin Tools contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE.txt file.
 */

package org.kotlinbitcointools.regtesttoolbox

import kotlinx.coroutines.runBlocking
import org.kotlinbitcointools.regtesttoolbox.regenv.RegEnv
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegEnvTest {
    @Test
    fun testMineBlocks() {
        runBlocking {
            val env = RegEnv.create(username = "regtest", password = "password")

            try {
                val initialBlockCount = env.getBlockCount()
                val blockHashes = env.mine(3)

                assertEquals(3, blockHashes.size, "Should mine 3 blocks")
                assertEquals(initialBlockCount + 3, env.getBlockCount(), "Block count should increase by 3")
            } finally {
                env.close()
            }
        }
    }

    @Test
    fun testGetNewAddressAndSend() {
        runBlocking {
            val sender = RegEnv.create(username = "regtest", password = "password", funded = true)
            val receiver = RegEnv.create(username = "regtest", password = "password")

            try {
                // Wallet is auto-funded, verify balance
                val senderBalance = sender.getBalance()
                assertTrue(senderBalance > 0.0, "Sender should have auto-funded balance")

                // Get a receiving address
                val receiverAddress = receiver.getNewAddress()
                assertTrue(receiverAddress.isNotEmpty(), "Address should not be empty")
                assertTrue(receiverAddress.startsWith("bcrt1"), "Should be a regtest bech32 address")

                // Send funds
                val txid = sender.send(receiverAddress, 1.0)
                assertTrue(txid.isNotEmpty(), "Transaction ID should not be empty")
                assertEquals(64, txid.length, "Txid should be 64 hex characters")

                // Mine a block to confirm
                sender.mine(1)

                // Check that receiver got the funds
                val receiverBalance = receiver.getBalance()
                assertTrue(receiverBalance >= 1.0, "Receiver should have received funds")
            } finally {
                sender.close()
                receiver.close()
            }
        }
    }

    @Test
    fun testGetBalances() {
        runBlocking {
            val env = RegEnv.create(username = "regtest", password = "password", funded = true)

            try {
                val balances = env.getBalances()

                // Wallet is auto-funded with some BTC (1 mature coinbase reward)
                assertTrue(balances.mine.trusted > 0.0, "Trusted balance should include coinbase reward")
                assertTrue(balances.mine.immature >= 0, "Immature balance should be non-negative")
                assertTrue(balances.mine.untrusted_pending >= 0, "Pending balance should be non-negative")
                assertTrue(balances.lastprocessedblock.height >= 101, "Block height should be at least 101")
            } finally {
                env.close()
            }
        }
    }

    @Test
    fun testListUnspent() {
        runBlocking {
            val env = RegEnv.create(username = "regtest", password = "password", funded = true)

            try {
                // Wallet is auto-funded with 1 coinbase UTXO
                val utxos = env.listUnspent()
                assertTrue(utxos.isNotEmpty(), "Wallet should have at least one UTXO from auto-funding")
                assertTrue(utxos[0].amount > 0.0, "Coinbase UTXO should be at least 50 BTC")
            } finally {
                env.close()
            }
        }
    }
}
