package org.kotlinbitcointools.regtesttoolbox.samples

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.kotlinbitcointools.regtesttoolbox.bitcoind.BitcoinClient

class Samples {
    @Test
    fun bitcoinDaemonClient() {
        runBlocking {
            val client = BitcoinClient(
                username = "regtest",
                password = "password",
                walletName = "faucet"
            )
            val address = client.getNewAddress()
            client.generateBlocks(1, address)
            client.close()
        }
    }
}
