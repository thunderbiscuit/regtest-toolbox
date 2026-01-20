package org.kotlinbitcointools.regtestkit

import kotlinx.coroutines.runBlocking
import org.kotlinbitcointools.regtestkit.bitcoind.BitcoinClient
import kotlin.test.Test
import kotlin.test.assertEquals

class BitcoinClientTest {
    @Test
    fun testGenerateBlocks() = runBlocking {
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
