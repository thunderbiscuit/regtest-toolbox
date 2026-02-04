# Regtest Toolbox

A Kotlin library providing utilities for interacting with Bitcoin Core nodes. Designed for integration testing and development workflows.

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("org.kotlinbitcointools:regtest-toolbox:0.1.0")
}
```

## Usage

```kotlin
import org.kotlinbitcointools.regtesttoolbox.bitcoind.BitcoinClient

val client = BitcoinClient(
    host = "localhost",
    port = 18443,
    username = "regtest",
    password = "password"
)

// Generate 10 blocks to an address
val blockHashes: List<String> = client.generateBlocks(
    numBlocks = 10,
    address = "bcrt1q6gau5mg4ceupfhtyywyaj5ge45vgptvawgg3aq"
)

// Get current block height
val blockCount: Int = client.getBlockCount()

client.close()
```

## Regtest Block Rewards

On regtest, the block subsidy halves every 150 blocks (compared to 210,000 on mainnet). If your regtest chain is too long, coinbase rewards may be negligible or zero.

| Block Height | Block Reward |
|--------------|--------------|
| 0-149        | 50 BTC       |
| 150-299      | 25 BTC       |
| 300-449      | 12.5 BTC     |
| 450-599      | 6.25 BTC     |
| 600-749      | 3.125 BTC    |
| 750-899      | 1.5625 BTC   |
| 900-1049     | 0.78125 BTC  |
| 1050-1199    | 0.390625 BTC |
| 1200-1349    | 0.195 BTC    |
| 1350-1499    | 0.0977 BTC   |
| 1500+        | < 0.05 BTC   |

After ~3000 blocks (20 halvings), rewards are less than 1 satoshi.
