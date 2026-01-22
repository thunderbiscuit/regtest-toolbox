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
