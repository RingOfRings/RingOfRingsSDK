package com.hyperring.ringofrings.core.utils.alchemy.data

data class BalancesJsonBody(
    val id: Int = 1,
    val jsonrpc : String = "2.0",
    val method: String = "alchemy_getTokenBalances",
    val params: List<String>
)
//    {
//        "id": 1,
//        "jsonrpc": "2.0",
//        "method": "alchemy_getTokenBalances",
//        "params": [
//        "0x607f4c5bb672230e8672085532f7e901544a7375"
//        ]
//    }