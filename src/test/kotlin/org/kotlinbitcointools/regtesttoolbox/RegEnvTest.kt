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
            val env = RegEnv.create(username = "regtest", password = "password")

            try {
                // Get an address from the wallet
                val address = env.getNewAddress()
                assertTrue(address.isNotEmpty(), "Address should not be empty")
                assertTrue(address.startsWith("bcrt1"), "Should be a regtest bech32 address")

                // Mine blocks to an external address, then send to our wallet
                // First we need funds in the wallet, so let's use a second env to fund this one
                val funder = RegEnv.create(username = "regtest", password = "password")

                try {
                    // Fund the funder wallet by mining to its own address
                    val funderAddress = funder.getNewAddress()
                    funder.mineToAddress(101, funderAddress)

                    val funderBalance = funder.getBalance()
                    assertTrue(funderBalance > 0, "Funder should have balance after mining")

                    // Send from funder to env
                    val txid = funder.send(address, 0.00012345)
                    assertTrue(txid.isNotEmpty(), "Transaction ID should not be empty")
                    assertEquals(64, txid.length, "Txid should be 64 hex characters")

                    // Mine a block to confirm
                    funder.mine(1)

                    // Check that env received the funds
                    val envBalance = env.getBalance()
                    assertTrue(envBalance >= 0.00012345, "Env should have received funds")
                } finally {
                    funder.close()
                }
            } finally {
                env.close()
            }
        }
    }

    @Test
    fun testGetBalances() {
        runBlocking {
            val env = RegEnv.create(username = "regtest", password = "password")

            try {
                val balances = env.getBalances()

                // On a fresh wallet, all balances should be 0
                assertTrue(balances.mine.trusted >= 0, "Trusted balance should be non-negative")
                assertTrue(balances.mine.immature >= 0, "Immature balance should be non-negative")
                assertTrue(balances.mine.untrusted_pending >= 0, "Pending balance should be non-negative")
                assertTrue(balances.lastprocessedblock.height >= 0, "Block height should be non-negative")
            } finally {
                env.close()
            }
        }
    }

    @Test
    fun testListUnspent() {
        runBlocking {
            val env = RegEnv.create(username = "regtest", password = "password")

            try {
                // Fresh wallet should have no UTXOs
                val utxos = env.listUnspent()
                assertTrue(utxos.isEmpty(), "Fresh wallet should have no UTXOs")
            } finally {
                env.close()
            }
        }
    }
}
