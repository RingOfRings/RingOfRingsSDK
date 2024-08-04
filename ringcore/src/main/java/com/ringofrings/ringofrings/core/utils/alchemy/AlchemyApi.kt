package com.ringofrings.ringofrings.core.utils.alchemy

import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal


/***
 * Alchemy API Manager
 * Default Base Url is seporia.
 */
class AlchemyApi(baseUrl: String = SEPORIA_BASE_URL) {
    val service: AlchemyService
    init {
        initBaseURL(baseUrl)
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(AlchemyService::class.java)
    }

    companion object {
        val DEFAULT_GAS_PRICE: BigDecimal= BigDecimal(20)
        private const val SEPORIA_BASE_URL = "https://eth-sepolia.g.alchemy.com"
        var baseUrl: String = SEPORIA_BASE_URL
        var _chainId: Long = 11155111L
        fun initBaseURL(baseUrl: String) {
            Companion.baseUrl = baseUrl
        }

        fun getWeb3j(alchemyKey: String) : Web3j {
            val web3j: Web3j = Web3j.build(HttpService("$baseUrl/v2/$alchemyKey"))
            return web3j
        }


        fun getChainId(): Long {
            if(baseUrl.contains("sepolia")) {
                return 11155111L
            } else if(_chainId == 11155111L) {
                throw Exception("chain id is null Exception")
            }
            return _chainId
        }


        fun setChainId(newChainId: Long) {
            _chainId = newChainId
        }
    }
}