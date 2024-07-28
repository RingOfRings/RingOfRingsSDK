package com.hyperring.ringofrings.core.utils.alchemy

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class AlchemyApi(baseUrl: String = SEPORIA_BASE_URL) {
    val service: AlchemyService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(AlchemyService::class.java)
    }

    companion object {
        private const val SEPORIA_BASE_URL = "https://eth-sepolia.g.alchemy.com"
//        https://eth-sepolia.g.alchemy.com/v2/wcdDFTfh6RhkUf14WB61TTU8tPt_ww-r
    }
}