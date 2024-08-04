package com.ringofrings.ringofrings.core.utils.alchemy.data

data class TokenBalances(
    val id: Int = 1,
    val jsonrpc : String = "2.0",
    val result: TokenBalanceResult,
)

data class TokenBalanceResult(
    val address: String,
    val tokenBalances: List<TokenBalance>
)
data class TokenBalance(
    val contractAddress: String,
    val tokenBalance: String
)