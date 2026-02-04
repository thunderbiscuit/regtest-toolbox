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
    fun testConnectTo() {
        runBlocking {
            val env = RegEnv.connectTo(walletName = "faucet", username = "regtest", password = "password")

            try {
                // Faucet should be a pre-funded wallet
                val balance = env.getBalance()
                assertTrue(balance > 0.0, "Faucet should have funds")
            } finally {
                env.close()
            }
        }
    }

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
            val env1 = RegEnv.connectTo(walletName = "faucet", username = "regtest", password = "password")
            val env2 = RegEnv.create(username = "regtest", password = "password")

            try {
                // Faucet should have funds
                val faucetBalance = env1.getBalance()
                assertTrue(faucetBalance > 0.0, "Faucet should have balance")

                // Get a receiving address
                val receiverAddress = env2.getNewAddress()
                assertTrue(receiverAddress.isNotEmpty(), "Address should not be empty")
                assertTrue(receiverAddress.startsWith("bcrt1"), "Should be a regtest bech32 address")

                // Send funds from faucet
                val txid = env1.send(receiverAddress, 1.0)
                assertTrue(txid.isNotEmpty(), "Transaction ID should not be empty")
                assertEquals(64, txid.length, "Txid should be 64 hex characters")

                // Mine a block to confirm
                env1.mine(1)

                // Check that receiver got the funds
                val receiverBalance = env2.getBalance()
                assertTrue(receiverBalance >= 1.0, "Receiver should have received funds")
            } finally {
                env1.close()
                env2.close()
            }
        }
    }

    @Test
    fun testGetBalances() {
        runBlocking {
            val env1 = RegEnv.connectTo(walletName = "faucet", username = "regtest", password = "password")
            val env2 = RegEnv.create(username = "regtest", password = "password")

            try {
                // Fund env from faucet
                val address = env2.getNewAddress()
                env1.send(address, 10.0)
                env1.mine(1)

                val balances = env2.getBalances()

                assertTrue(balances.mine.trusted >= 10.0, "Trusted balance should include received funds")
                assertTrue(balances.mine.immature >= 0, "Immature balance should be non-negative")
                assertTrue(balances.mine.untrusted_pending >= 0, "Pending balance should be non-negative")
            } finally {
                env1.close()
                env2.close()
            }
        }
    }

    @Test
    fun testListUnspent() {
        runBlocking {
            val env1 = RegEnv.connectTo(walletName = "faucet", username = "regtest", password = "password")
            val env2 = RegEnv.create(username = "regtest", password = "password")

            try {
                // Fund env from faucet
                val address = env2.getNewAddress()
                env1.send(address, 5.0)
                env1.mine(1)

                val utxos = env2.listUnspent()
                assertTrue(utxos.isNotEmpty(), "Wallet should have at least one UTXO")
                assertTrue(utxos[0].amount >= 5.0, "UTXO should have the received amount")
            } finally {
                env1.close()
                env2.close()
            }
        }
    }
}
