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
}
