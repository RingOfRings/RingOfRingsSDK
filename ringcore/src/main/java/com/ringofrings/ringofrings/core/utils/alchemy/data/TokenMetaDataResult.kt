package com.ringofrings.ringofrings.core.utils.alchemy.data

data class TokenMetaDataResult(
    val id: Int = 1,
    val jsonrpc : String = "2.0",
    val result: MetaDataDetailResult,
)

data class MetaDataDetailResult(
    val decimals: Int?,
    val logo: String?,
    val name: String?,
    val symbol: String?
)

data class TokenAmountResult(
    val id: Int = 1,
    val jsonrpc : String = "2.0",
    val result: String,
)