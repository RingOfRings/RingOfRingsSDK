package com.hyperring.ringofrings.core.utils.alchemy.data

data class BalancesJsonBody(
    val id: Int = 1,
    val jsonrpc : String = "2.0",
    val method: String = "alchemy_getTokenBalances",
    val params: List<String>
)

data class TokenMetadataJsonBody(
    val id: Int = 1,
    val jsonrpc : String = "2.0",
    val method: String = "alchemy_getTokenMetadata",
    val params: List<String>
)

data class TokenBalanceJsonBody(
    val id: Int = 1,
    val jsonrpc : String = "2.0",
    val method: String = "eth_getBalance",
    val params: List<String>
)